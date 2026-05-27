package com.fiap.demo.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    public static final int TOKEN_EXP_HOURS = 8;

    public String generateToken(String cpf) {
        Instant expiration = Instant.now().plus(TOKEN_EXP_HOURS, ChronoUnit.HOURS);
        String payload = cpf + ":" + expiration.getEpochSecond();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isExpired(String token) {
        try {
            String payload = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = payload.split(":");
            if (parts.length != 2) {
                return true;
            }
            long expiresAt = Long.parseLong(parts[1]);
            return Instant.now().isAfter(Instant.ofEpochSecond(expiresAt));
        } catch (Exception ex) {
            return true;
        }
    }
}
