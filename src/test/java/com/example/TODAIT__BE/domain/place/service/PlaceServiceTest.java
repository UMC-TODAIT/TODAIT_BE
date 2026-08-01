package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(placeRepository);
    }

    @Test
    void searchPlacesRejectsBlankKeyword() {
        assertThatThrownBy(() -> placeService.searchPlaces(" "))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.INVALID_SEARCH_KEYWORD);

        verifyNoInteractions(placeRepository);
    }
}
