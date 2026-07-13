package com.example.TODAIT__BE.global.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomCodeGenerator {

    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int NUMBER_BOUND = 10;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateNumericCode() {
        return generateNumericCode(DEFAULT_CODE_LENGTH);
    }

    public String generateNumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be positive.");
        }

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(secureRandom.nextInt(NUMBER_BOUND));
        }

        return code.toString();
    }
}
