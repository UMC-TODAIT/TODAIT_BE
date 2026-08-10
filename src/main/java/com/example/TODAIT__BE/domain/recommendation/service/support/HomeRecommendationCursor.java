package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.recommendation.code.HomeRecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public record HomeRecommendationCursor(
        LocalDate rotationDate,
        long offset
) {

    private static final String DELIMITER = ":";
    private static final String SIGNING_ALGORITHM = "HmacSHA256";

    public static HomeRecommendationCursor first(LocalDate rotationDate) {
        return new HomeRecommendationCursor(rotationDate, 0L);
    }

    public static HomeRecommendationCursor decodeOrFirst(
            String cursor,
            LocalDate defaultRotationDate,
            String secret
    ) {
        if (cursor == null || cursor.isBlank()) {
            return first(defaultRotationDate);
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );
            String[] parts = decoded.split(DELIMITER, -1);
            if (parts.length != 3) {
                throw new IllegalArgumentException();
            }

            LocalDate rotationDate = LocalDate.parse(parts[0]);
            long offset = Long.parseLong(parts[1]);
            if (offset < 0) {
                throw new IllegalArgumentException();
            }

            String raw = raw(rotationDate, offset);
            if (!isValidSignature(raw, parts[2], secret)) {
                throw new IllegalArgumentException();
            }

            return new HomeRecommendationCursor(rotationDate, offset);
        } catch (RuntimeException e) {
            throw new RecommendationException(
                    HomeRecommendationErrorCode.INVALID_CURSOR,
                    e
            );
        }
    }

    public String encode(String secret) {
        String raw = raw(rotationDate, offset);
        String payload = raw + DELIMITER + sign(raw, secret);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private static String raw(LocalDate rotationDate, long offset) {
        return rotationDate + DELIMITER + offset;
    }

    private static boolean isValidSignature(
            String raw,
            String signature,
            String secret
    ) {
        byte[] expected = sign(raw, secret).getBytes(StandardCharsets.UTF_8);
        byte[] actual = signature.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private static String sign(String raw, String secret) {
        try {
            Mac mac = Mac.getInstance(SIGNING_ALGORITHM);
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    SIGNING_ALGORITHM
            ));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "HMAC signing algorithm is not available.",
                    e
            );
        } catch (java.security.InvalidKeyException e) {
            throw new IllegalStateException(
                    "Home recommendation cursor secret is invalid.",
                    e
            );
        }
    }
}
