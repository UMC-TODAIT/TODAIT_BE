package com.example.TODAIT__BE.domain.taxonomy.init;

import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

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
    public void run(ApplicationArguments args) {
        Optional<PlaceCategory> existing = placeCategoryRepository.findByCode(ETC_CODE);
        if (existing.isPresent()) {
            // 이미 존재하지만 비활성이면 활성화한다. 그렇지 않으면 활성 목록에서
            // 계속 누락되어 /api/place-categories·검색 enricher가 OTHER를 놓친다.
            reactivateIfInactive(existing.get());
            return;
        }
        createEtcCategory();
    }

    private void reactivateIfInactive(PlaceCategory etcCategory) {
        if (Boolean.TRUE.equals(etcCategory.getIsActive())) {
            return;
        }
        etcCategory.activate();
        placeCategoryRepository.save(etcCategory);
        log.info("[place-category] 비활성 상태였던 '기타(OTHER)' 카테고리를 활성화했습니다.");
    }

    private void createEtcCategory() {
        int nextSortOrder = placeCategoryRepository
                .findFirstByOrderBySortOrderDesc()
                .map(category -> category.getSortOrder() + 1)
                .orElse(1);

        try {
            placeCategoryRepository.save(
                    PlaceCategory.of(ETC_CODE, ETC_NAME, ETC_DESCRIPTION, nextSortOrder, true)
            );
            log.info("[place-category] '기타(OTHER)' 카테고리를 생성했습니다. sortOrder={}", nextSortOrder);
        } catch (DataIntegrityViolationException e) {
            Optional<PlaceCategory> existing =
                    placeCategoryRepository.findByCode(ETC_CODE);

            if (existing.isPresent()) {
                reactivateIfInactive(existing.get());
                log.info(
                        "[place-category] '기타(OTHER)' 카테고리가 다른 인스턴스에서 이미 생성되었습니다."
                );
                return;
            }

            throw e;
        }
    }
}
