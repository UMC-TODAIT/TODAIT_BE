package com.example.TODAIT__BE.domain.place.service.port;

import java.util.List;

public record ExternalPlaceSearchResult(
        List<ExternalPlaceCandidate> candidates,
        boolean end
) {
}
