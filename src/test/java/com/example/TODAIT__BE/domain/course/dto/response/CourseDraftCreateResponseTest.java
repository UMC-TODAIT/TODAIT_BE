package com.example.TODAIT__BE.domain.course.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CreateResponse;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.global.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CreateTest {

    @Test
    void serializesCreate() throws Exception {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();

        CreateResponse response =
                new CreateResponse(
                        1L,
                        CourseDraftStatus.MOOD_SELECTING,
                        LocalDateTime.of(2026, 7, 16, 10, 0)
                );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"courseDraftId\":1");
        assertThat(json).contains("\"draftStatus\":\"MOOD_SELECTING\"");
        assertThat(json).contains("\"createdAt\":\"2026-07-16T10:00:00\"");
        assertThat(json).doesNotContain("expiresAt");
        assertThat(json).doesNotContain("\"status\"");
    }
}
