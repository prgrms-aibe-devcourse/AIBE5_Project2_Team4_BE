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
        LocalDateTime now = LocalDateTime.now();
        removeExpiredTokensAndExistingEmail(email, now);

        String token = UUID.randomUUID().toString();
        store.put(token, new TokenEntry(email, now.plusMinutes(expirationMinutes)));
        return token;
    }

    public Optional<String> consumeValidToken(String token) {
        TokenEntry entry = store.get(token);
        if (entry == null) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        if (!entry.expiresAt().isAfter(now)) {
            store.remove(token, entry);
            removeExpiredTokens(now);
            return Optional.empty();
        }

        if (!store.remove(token, entry)) {
            return Optional.empty();
        }
        return Optional.of(entry.email());
    }

    private void removeExpiredTokensAndExistingEmail(String email, LocalDateTime now) {
        store.entrySet().removeIf(entry ->
                !entry.getValue().expiresAt().isAfter(now) || entry.getValue().email().equals(email)
        );
    }

    private void removeExpiredTokens(LocalDateTime now) {
        store.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }
}
