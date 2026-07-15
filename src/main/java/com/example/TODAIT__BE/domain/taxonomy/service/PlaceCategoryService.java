package com.example.TODAIT__BE.domain.taxonomy.service;

import com.example.TODAIT__BE.domain.taxonomy.dto.response.PlaceCategoryListResponse;
import com.example.TODAIT__BE.domain.taxonomy.dto.response.PlaceCategoryResponse;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceCategoryService {

    private final PlaceCategoryRepository placeCategoryRepository;

    public PlaceCategoryListResponse getPlaceCategories() {
        List<PlaceCategoryResponse> placeCategories = placeCategoryRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(placeCategory -> new PlaceCategoryResponse(
                        placeCategory.getId(),
                        placeCategory.getCode(),
                        placeCategory.getName(),
                        placeCategory.getDescription(),
                        placeCategory.getSortOrder()
                ))
                .toList();

        return new PlaceCategoryListResponse(placeCategories);
    }
}
