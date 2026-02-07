package com.yibiai.thesis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DeepSeekService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model}")
    private String model;

    public DeepSeekService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    private String normalizedApiKey() {
        String key = apiKey;
        if (key == null) {
            key = "";
        }
        key = key.trim();
        if (key.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            key = key.substring("Bearer ".length()).trim();
        }
        if (key.startsWith("${") || key.contains("DEEPSEEK_API_KEY")) {
            throw new RuntimeException("DeepSeek API Key 未解析：当前读取到的值疑似占位符（${DEEPSEEK_API_KEY}）。请确认后端已成功加载 thesis3/.env（或 thesis3/.env.properties）并重启后端。若 .env 已设置，请检查 spring.config.import 配置。");
        }
        if (key.isEmpty()) {
            throw new RuntimeException("DeepSeek API Key 未配置：请在 thesis3/.env 或系统环境变量中设置 DEEPSEEK_API_KEY（值为 sk-...，不要包含 Bearer 前缀）");
        }
        return key;
    }

    public Mono<String> chat(String systemPrompt, String userPrompt) {
        return Mono.defer(() -> {
            String key = normalizedApiKey();
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 8192);
            requestBody.put("temperature", 0.7);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);

            return webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "DeepSeek API错误: HTTP " + resp.statusCode().value() + " - " + body
                                    )))
                    )
                    .bodyToMono(JsonNode.class)
                    .map(response -> {
                        JsonNode choices = response.get("choices");
                        if (choices != null && choices.isArray() && choices.size() > 0) {
                            JsonNode message = choices.get(0).get("message");
                            if (message != null && message.has("content")) {
                                return message.get("content").asText();
                            }
                        }
                        throw new RuntimeException("DeepSeek API返回格式异常: " + response.toString());
                    })
                    .onErrorMap(WebClientResponseException.class, e ->
                            new RuntimeException("DeepSeek API请求失败: HTTP " + e.getStatusCode().value() + " - " + e.getResponseBodyAsString(), e)
                    );
        });
    }

    public record Message(String role, String content) {}

    public Flux<String> chatStream(String systemPrompt, String userPrompt) {
        return chatStream(List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ));
    }

    public Flux<String> chatStream(List<Message> messages) {
        return Flux.defer(() -> {
            String key = normalizedApiKey();
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 8192);
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", true);

            ArrayNode msgArr = requestBody.putArray("messages");
            for (Message msg : messages) {
                ObjectNode m = msgArr.addObject();
                m.put("role", msg.role());
                m.put("content", msg.content());
            }

            ThinkFilterState thinkFilterState = new ThinkFilterState();

            Flux<String> contentFlux = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .filter(data -> !data.equals("[DONE]"))
                    .map(data -> {
                        try {
                            if (data.startsWith("data: ")) {
                                data = data.substring(6);
                            }
                            if (data.equals("[DONE]")) {
                                return "";
                            }
                            JsonNode json = objectMapper.readTree(data);
                            JsonNode choices = json.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    return delta.get("content").asText();
                                }
                            }
                        } catch (Exception e) {
                            return "";
                        }
                        return "";
                    })
                    .filter(content -> !content.isEmpty());

            return contentFlux
                    .handle((String chunk, reactor.core.publisher.SynchronousSink<String> sink) ->
                            emitWithoutThinkingTags(chunk, thinkFilterState, sink)
                    )
                    .concatWith(Mono.defer(() -> {
                        String tail = thinkFilterState.flushTail();
                        if (tail.isEmpty()) {
                            return Mono.empty();
                        }
                        return Mono.just(tail);
                    }));
        });
    }

    private static final int THINKING_TAG_BUFFER_SIZE = 20;

    private static class ThinkFilterState {
        private final StringBuilder buffer = new StringBuilder();
        private boolean inThinkingTag = false;

        String flushTail() {
            if (inThinkingTag) {
                return "";
            }
            String out = buffer.toString();
            buffer.setLength(0);
            return out;
        }
    }

    private void emitWithoutThinkingTags(String chunk, ThinkFilterState state, reactor.core.publisher.SynchronousSink<String> sink) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        state.buffer.append(chunk);

        while (true) {
            String lower = state.buffer.toString().toLowerCase();

            if (!state.inThinkingTag) {
                int thinkIdx = lower.indexOf("<think>");
                int thinkingIdx = lower.indexOf("<thinking>");
                int startIdx;
                int startLen;
                if (thinkIdx >= 0 && (thinkingIdx < 0 || thinkIdx < thinkingIdx)) {
                    startIdx = thinkIdx;
                    startLen = "<think>".length();
                } else if (thinkingIdx >= 0) {
                    startIdx = thinkingIdx;
                    startLen = "<thinking>".length();
                } else {
                    startIdx = -1;
                    startLen = 0;
                }

                if (startIdx >= 0) {
                    String before = state.buffer.substring(0, startIdx);
                    if (!before.isEmpty()) {
                        sink.next(before);
                    }
                    state.buffer.delete(0, startIdx + startLen);
                    state.inThinkingTag = true;
                    continue;
                }

                if (state.buffer.length() > THINKING_TAG_BUFFER_SIZE) {
                    int emitLen = state.buffer.length() - THINKING_TAG_BUFFER_SIZE;
                    String out = state.buffer.substring(0, emitLen);
                    if (!out.isEmpty()) {
                        sink.next(out);
                    }
                    state.buffer.delete(0, emitLen);
                }
                break;
            } else {
                int endThinkIdx = lower.indexOf("</think>");
                int endThinkingIdx = lower.indexOf("</thinking>");
                int endIdx;
                int endLen;
                if (endThinkIdx >= 0 && (endThinkingIdx < 0 || endThinkIdx < endThinkingIdx)) {
                    endIdx = endThinkIdx;
                    endLen = "</think>".length();
                } else if (endThinkingIdx >= 0) {
                    endIdx = endThinkingIdx;
                    endLen = "</thinking>".length();
                } else {
                    endIdx = -1;
                    endLen = 0;
                }

                if (endIdx >= 0) {
                    state.buffer.delete(0, endIdx + endLen);
                    state.inThinkingTag = false;
                    continue;
                }
                state.buffer.setLength(0);
                break;
            }
        }
    }
}
