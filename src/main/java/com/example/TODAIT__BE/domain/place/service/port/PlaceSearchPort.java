package com.example.TODAIT__BE.domain.place.service.port;


public interface PlaceSearchPort {

    ExternalPlaceSearchResult searchByKeyword(
            String query,
            int page,
            int size
    );
}
