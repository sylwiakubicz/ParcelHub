package com.parcelhub.locker_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class PickupCodeHasher {

    private final String secret;

    public PickupCodeHasher(@Value("${locker.pickup-code.secret}") String secret) {
        this.secret = secret;
    }

    public String hash(UUID shipmentId, String lockerId, String pickupCode) {
        try {
            String data = shipmentId + ":" + lockerId + ":" + pickupCode;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);

            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
