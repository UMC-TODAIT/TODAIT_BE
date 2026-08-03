package com.example.TODAIT__BE.domain.place.port.out;


public interface PlaceSearchPort {

    ExternalPlaceSearchResult searchByKeyword(
            String query,
            int page,
            int size
    );
}
