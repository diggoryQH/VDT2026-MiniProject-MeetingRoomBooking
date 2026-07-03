package com.nandestech.meetingroom.service;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.Pasetos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PasetoTokenService {

    private final SecretKey secretKey;

    public PasetoTokenService(@Value("${paseto.secret:default_secret_key_needs_to_be_long_enough_32_bytes}") String secret) {
        try {
            // Hash the secret to ensure it's exactly 32 bytes for PASETO v2.local
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize PasetoTokenService", e);
        }
    }

    public String generateAccessToken(String username) {
        return Pasetos.V2.LOCAL.builder()
                .setSharedSecret(secretKey)
                .setSubject(username)
                .setExpiration(Instant.now().plus(15, ChronoUnit.MINUTES))
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Pasetos.V2.LOCAL.builder()
                .setSharedSecret(secretKey)
                .setSubject(username)
                .setExpiration(Instant.now().plus(7, ChronoUnit.DAYS))
                .compact();
    }

    public String verifyToken(String token) {
        try {
            Paseto paseto = Pasetos.parserBuilder()
                    .setSharedSecret(secretKey)
                    .build()
                    .parse(token);
            return paseto.getClaims().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
}
