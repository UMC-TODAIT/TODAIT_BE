package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceSearchAreaResolver {

    private final AreaRepository areaRepository;

    public Map<String, Area> getActiveAreasByCode() {
        return areaRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .collect(Collectors.toMap(Area::getCode, Function.identity()));
    }

    public Area resolve(
            String areaCode,
            Map<String, Area> activeAreasByCode
    ) {
        if (areaCode == null || areaCode.isBlank()) {
            return null;
        }

        return activeAreasByCode.get(areaCode.trim());
    }
}