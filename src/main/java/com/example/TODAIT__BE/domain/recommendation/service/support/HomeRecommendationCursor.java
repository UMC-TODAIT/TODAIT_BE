package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

public record HomeRecommendationCursor(
        LocalDate rotationDate,
        long offset
) {

    private static final String DELIMITER = ":";

    public static HomeRecommendationCursor first(LocalDate rotationDate) {
        return new HomeRecommendationCursor(rotationDate, 0L);
    }

    public static HomeRecommendationCursor decodeOrFirst(
            String cursor,
            LocalDate defaultRotationDate
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
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }

            LocalDate rotationDate = LocalDate.parse(parts[0]);
            long offset = Long.parseLong(parts[1]);
            if (offset < 0) {
                throw new IllegalArgumentException();
            }

            return new HomeRecommendationCursor(rotationDate, offset);
        } catch (RuntimeException e) {
            throw new RecommendationException(
                    RecommendationErrorCode.INVALID_CURSOR
            );
        }
    }

    public String encode() {
        String raw = rotationDate + DELIMITER + offset;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
