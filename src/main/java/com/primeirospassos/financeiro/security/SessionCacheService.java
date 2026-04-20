package com.primeirospassos.financeiro.security;

import com.primeirospassos.financeiro.integration.auth.SessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SessionCacheService {

    private final long ttlSeconds;
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SessionCacheService(@Value("${session.cache.ttl-seconds:30}") long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<SessionResponse> getValid(String sessionId) {
        CacheEntry entry = cache.get(sessionId);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(entry.session());
    }

    public Optional<SessionResponse> getAny(String sessionId) {
        CacheEntry entry = cache.get(sessionId);
        return entry == null ? Optional.empty() : Optional.of(entry.session());
    }

    public void put(String sessionId, SessionResponse sessionResponse) {
        cache.put(sessionId, new CacheEntry(sessionResponse, Instant.now().plusSeconds(ttlSeconds)));
    }

    private record CacheEntry(SessionResponse session, Instant expiresAt) {
    }
}
