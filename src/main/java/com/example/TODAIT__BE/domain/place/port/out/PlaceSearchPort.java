package com.example.TODAIT__BE.domain.place.port.out;

import java.util.List;

public interface PlaceSearchPort {

    List<ExternalPlaceCandidate> searchByKeyword(
            String query,
            int page,
            int size
    );
}
