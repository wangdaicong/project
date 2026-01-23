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

    public Mono<String> chat(String systemPrompt, String userPrompt) {
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
                .header("Authorization", "Bearer " + apiKey)
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
    }

    public Flux<String> chatStream(String systemPrompt, String userPrompt) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 8192);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", true);

        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
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
                        // ignore parse errors
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty());
    }
}
