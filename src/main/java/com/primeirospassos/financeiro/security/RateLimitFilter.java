package com.primeirospassos.financeiro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int limit;
    private final long windowSeconds;

    private final ConcurrentMap<String, Deque<Long>> requestsByKey = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    public RateLimitFilter(
            @Value("${rate-limit.pagamentos.limit:5}") int limit,
            @Value("${rate-limit.pagamentos.window-seconds:60}") long windowSeconds) {
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/financeiro/pagamentos".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = UserContextHolder.get()
                .map(user -> user.userId() + ":" + user.sessionId())
                .orElseGet(request::getRemoteAddr);

        long now = Instant.now().getEpochSecond();
        Deque<Long> timestamps = requestsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - windowSeconds) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= limit) {
                response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Rate limit excedido para criação de pagamentos\"}");
                return;
            }

            timestamps.addLast(now);
        }

        cleanupIfNeeded(now);

        filterChain.doFilter(request, response);
    }

    private void cleanupIfNeeded(long nowEpochSecond) {
        if (requestCounter.incrementAndGet() % 100 != 0) {
            return;
        }

        requestsByKey.forEach((key, timestamps) -> {
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() <= nowEpochSecond - windowSeconds) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    requestsByKey.remove(key, timestamps);
                }
            }
        });
    }
}
