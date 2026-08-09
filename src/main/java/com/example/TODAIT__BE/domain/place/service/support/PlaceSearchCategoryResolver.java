package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceSearchCategoryResolver {

    private final PlaceCategoryRepository placeCategoryRepository;

    public Map<String, PlaceCategory> getActiveCategoriesByCode() {
        return placeCategoryRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .collect(Collectors.toMap(PlaceCategory::getCode, Function.identity()));
    }

    public PlaceCategory resolve(
            String placeCategoryCode,
            Map<String, PlaceCategory> activeCategoriesByCode
    ) {
        if (placeCategoryCode == null || placeCategoryCode.isBlank()) {
            return null;
        }

        return activeCategoriesByCode.get(placeCategoryCode.trim());
    }
}