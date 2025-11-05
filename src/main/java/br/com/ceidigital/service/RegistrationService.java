package br.com.ceidigital.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationService {
    private static class TokenEntry { String token; Instant expiresAt; String phone; }
    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>(); // key: email
    private final Random random = new Random();

    public String generateAndStoreToken(String email, String phone, int ttlMinutes){
        String code = String.format("%06d", random.nextInt(1_000_000));
        TokenEntry e = new TokenEntry();
        e.token = code;
        e.expiresAt = Instant.now().plusSeconds(ttlMinutes * 60L);
        e.phone = phone;
        tokens.put(normalize(email), e);
        return code;
    }

    public boolean verify(String email, String token){
        TokenEntry e = tokens.get(normalize(email));
        if (e == null) return false;
        if (e.expiresAt.isBefore(Instant.now())) { tokens.remove(normalize(email)); return false; }
        boolean ok = e.token.equals(token);
        if (ok) tokens.remove(normalize(email));
        return ok;
    }

    public String getStoredPhone(String email){
        TokenEntry e = tokens.get(normalize(email));
        return e != null ? e.phone : null;
    }

    private String normalize(String email){ return email == null ? "" : email.trim().toLowerCase(); }
}
