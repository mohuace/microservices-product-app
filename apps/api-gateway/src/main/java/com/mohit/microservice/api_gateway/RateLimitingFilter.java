package com.mohit.microservice.api_gateway;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final int REQUEST_LIMIT = 30;
    private static final long WINDOW_MILLIS = 60_000L;
    private static final String DEFAULT_CLIENT_ID = "unknown-client";

    private final ConcurrentMap<String, Deque<Long>> requestHistory = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String clientId = resolveClientId(exchange);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestHistory.computeIfAbsent(clientId, key -> new ConcurrentLinkedDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.removeFirst();
            }

            if (timestamps.size() >= REQUEST_LIMIT) {
                return rejectRequest(exchange);
            }

            timestamps.addLast(now);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveClientId(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String forwardedFor = headers.getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        if (exchange.getRequest().getRemoteAddress() != null && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return DEFAULT_CLIENT_ID;
    }

    private Mono<Void> rejectRequest(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", "60");

        String body = "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again in a moment.\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
