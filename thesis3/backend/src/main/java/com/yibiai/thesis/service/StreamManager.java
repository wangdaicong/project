package com.yibiai.thesis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreamManager {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, Sinks.Many<String>> sinks = new ConcurrentHashMap<>();

    private Sinks.Many<String> getOrCreateSink(String sessionId) {
        return sinks.computeIfAbsent(sessionId, k -> Sinks.many().replay().limit(2000));
    }

    public Flux<String> connect(String sessionId) {
        Sinks.Many<String> sink = getOrCreateSink(sessionId);
        return Mono.fromSupplier(() -> {
                    try {
                        String json = objectMapper.writeValueAsString(Map.of(
                                "type", "connected",
                                "session_id", sessionId
                        ));
                        return "data: " + json + "\n\n";
                    } catch (JsonProcessingException e) {
                        return ": connected\n\n";
                    }
                })
                .concatWith(sink.asFlux());
    }

    public void disconnect(String sessionId) {
        Sinks.Many<String> sink = sinks.remove(sessionId);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    public void broadcast(String sessionId, Map<String, Object> payload) {
        Sinks.Many<String> sink = getOrCreateSink(sessionId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            sink.tryEmitNext("data: " + json + "\n\n");
        } catch (JsonProcessingException e) {
            sink.tryEmitNext("data: {\"type\":\"error\",\"message\":\"serialize_failed\"}\n\n");
        }
    }

    public void heartbeat(String sessionId) {
        Sinks.Many<String> sink = sinks.get(sessionId);
        if (sink == null) {
            return;
        }
        sink.tryEmitNext(": keep-alive\n\n");
    }
}
