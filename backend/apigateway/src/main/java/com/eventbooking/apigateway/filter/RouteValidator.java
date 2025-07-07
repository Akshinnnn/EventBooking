package com.eventbooking.apigateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;

@Slf4j
@Component
public class RouteValidator {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Open API endpoints with methods (GET, POST, PUT, DELETE)
    public static final HashMap<String, List<String>> openApiEndpoints = new HashMap<>() {
        {
            put("/api/auth/authenticate", List.of("POST"));
            put("/api/auth/register", List.of("POST"));
            put("/api/auth/login", List.of("POST"));
            put("/api/categories", List.of("GET"));
            put("/api/events", List.of("GET"));
            put("/api/events/{id}", List.of("GET"));
            put("/api/events/{id}/reviews", List.of("GET"));
        }
    };

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.info("Request Path: {}, Method: {}", path, method);

        for (Map.Entry<String, List<String>> entry : openApiEndpoints.entrySet()) {
            String endpoint = entry.getKey();
            List<String> methods = entry.getValue();

            if (pathMatcher.match(endpoint, path) && methods.contains(method)) {
                return false;
            }
        }

        return true;
    };
}