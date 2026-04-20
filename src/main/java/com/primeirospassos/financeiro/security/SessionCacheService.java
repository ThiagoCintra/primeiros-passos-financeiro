package com.primeirospassos.financeiro.security;

import com.primeirospassos.financeiro.integration.auth.SessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SessionCacheService {

    private final long ttlSeconds;
    private final long fallbackMaxAgeSeconds;
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong opCounter = new AtomicLong();

    public SessionCacheService(
            @Value("${session.cache.ttl-seconds:30}") long ttlSeconds,
            @Value("${session.cache.fallback-max-age-seconds:300}") long fallbackMaxAgeSeconds) {
        this.ttlSeconds = ttlSeconds;
        this.fallbackMaxAgeSeconds = fallbackMaxAgeSeconds;
    }

    public Optional<SessionResponse> getValid(String sessionId) {
        CacheEntry entry = cache.get(sessionId);
        cleanupIfNeeded();
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(entry.session());
    }

    public Optional<SessionResponse> getAny(String sessionId) {
        CacheEntry entry = cache.get(sessionId);
        cleanupIfNeeded();
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.createdAt().plusSeconds(fallbackMaxAgeSeconds).isBefore(Instant.now())) {
            cache.remove(sessionId, entry);
            return Optional.empty();
        }
        return Optional.of(entry.session());
    }

    public void put(String sessionId, SessionResponse sessionResponse) {
        Instant now = Instant.now();
        cache.put(sessionId, new CacheEntry(sessionResponse, now, now.plusSeconds(ttlSeconds)));
        cleanupIfNeeded();
    }

    private void cleanupIfNeeded() {
        if (opCounter.incrementAndGet() % 100 != 0) {
            return;
        }
        Instant now = Instant.now();
        List<String> keysToRemove = new ArrayList<>();
        cache.forEach((key, value) -> {
            if (value.createdAt().plusSeconds(fallbackMaxAgeSeconds).isBefore(now)) {
                keysToRemove.add(key);
            }
        });
        keysToRemove.forEach(cache::remove);
    }

    private record CacheEntry(SessionResponse session, Instant createdAt, Instant expiresAt) {
    }
}
