package com.ieum.ansimdonghaeng.domain.auth.store;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenStore {

    private record TokenEntry(String email, LocalDateTime expiresAt) {}

    private final Map<String, TokenEntry> store = new ConcurrentHashMap<>();

    public String createToken(String email, int expirationMinutes) {
        String token = UUID.randomUUID().toString();
        store.put(token, new TokenEntry(email, LocalDateTime.now().plusMinutes(expirationMinutes)));
        return token;
    }

    public Optional<String> getEmailIfValid(String token) {
        TokenEntry entry = store.get(token);
        if (entry == null || !entry.expiresAt().isAfter(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(entry.email());
    }

    public void invalidate(String token) {
        store.remove(token);
    }
}