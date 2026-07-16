package com.example.TODAIT__BE.domain.course.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.global.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CourseDraftCreateResponseTest {

    @Test
    void serializesLocalDateTimeFields() throws Exception {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        CourseDraftCreateResponse response = new CourseDraftCreateResponse(
                1L,
                CourseDraftStatus.MOOD_SELECTING,
                LocalDateTime.of(2026, 7, 16, 12, 30),
                LocalDateTime.of(2026, 7, 16, 10, 0)
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"expiresAt\":\"2026-07-16T12:30:00\"");
        assertThat(json).contains("\"createdAt\":\"2026-07-16T10:00:00\"");
    }
}
