package com.primeirospassos.financeiro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMIT = 5;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentMap<String, Deque<Long>> requestsByKey = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/financeiro/pagamentos".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = UserContextHolder.get()
                .map(user -> user.userId() + ":" + user.sessionId())
                .orElseGet(() -> request.getRemoteAddr());

        long now = Instant.now().getEpochSecond();
        Deque<Long> timestamps = requestsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - WINDOW_SECONDS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= LIMIT) {
                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Rate limit excedido para criação de pagamentos\"}");
                return;
            }

            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }
}
