package com.hazer.hazefishing.security;

import com.hazer.hazefishing.model.NFTRod;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class IntegrityValidator {

    public String computeRodHash(NFTRod rod) {
        String payload = rod.rodId() + ":" + rod.owner() + ":" + rod.serial() + ":" + rod.immutableSeed();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash rod", ex);
        }
    }

    public boolean verifyRod(NFTRod rod, String expectedHash) {
        return computeRodHash(rod).equals(expectedHash);
    }
}
