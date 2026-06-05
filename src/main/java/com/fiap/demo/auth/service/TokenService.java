package com.fiap.demo.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Service
public class TokenService {

    public static final int TOKEN_EXP_HOURS = 8;
    private final String secret;
    private final Algorithm algorithm;

    public TokenService(@Value("${AgroShield.jwt.secret}") String secret) {
        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(String cpf) {
        return JWT.create()
                .withSubject(cpf)
                .withExpiresAt(Instant.now().plus(TOKEN_EXP_HOURS, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    public boolean isExpired(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    public String extractCpf(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception ex) {
            return null;
        }
    }
}
