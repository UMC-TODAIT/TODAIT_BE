package com.example.TODAIT__BE.domain.taxonomy.init;

import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱 기동 시 '기타(OTHER)' 장소 카테고리가 없으면 생성한다.
 *
 * <p>{@code /api/place-categories}는 place_category 테이블을 그대로 반환하므로,
 * 카페·액티비티·음식점·바 중 어디에도 해당하지 않는 장소를 위한 '기타' 카테고리를
 * 참조 데이터로 보장한다. 이미 존재하면 아무 것도 하지 않아 멱등하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceCategoryDataInitializer implements ApplicationRunner {

    private static final String ETC_CODE = "OTHER";
    private static final String ETC_NAME = "기타";
    private static final String ETC_DESCRIPTION =
            "카페, 액티비티, 음식점, 바 중 어디에도 해당하지 않는 장소 종류";

    private final PlaceCategoryRepository placeCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (placeCategoryRepository.findByCode(ETC_CODE).isPresent()) {
            return;
        }

        int nextSortOrder = placeCategoryRepository
                .findFirstByOrderBySortOrderDesc()
                .map(category -> category.getSortOrder() + 1)
                .orElse(1);

        placeCategoryRepository.save(
                PlaceCategory.of(ETC_CODE, ETC_NAME, ETC_DESCRIPTION, nextSortOrder, true)
        );
        log.info("[place-category] '기타(OTHER)' 카테고리를 생성했습니다. sortOrder={}", nextSortOrder);
    }
}
