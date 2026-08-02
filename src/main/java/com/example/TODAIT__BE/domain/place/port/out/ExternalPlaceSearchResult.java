package com.example.TODAIT__BE.domain.place.port.out;

import java.util.List;

public record ExternalPlaceSearchResult(
        List<ExternalPlaceCandidate> candidates,
        boolean end
) {
}
