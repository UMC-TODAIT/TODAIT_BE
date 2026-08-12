package com.example.TODAIT__BE.domain.taxonomy.init;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PlaceCategoryDataInitializerTest {

    @Mock
    private PlaceCategoryRepository placeCategoryRepository;

    @InjectMocks
    private PlaceCategoryDataInitializer initializer;

    @Test
    void createsEtcCategoryAsLastSortOrderWhenMissing() {
        given(placeCategoryRepository.findByCode("OTHER"))
                .willReturn(Optional.empty());
        given(placeCategoryRepository.findFirstByOrderBySortOrderDesc())
                .willReturn(Optional.of(
                        PlaceCategory.of("BAR", "바", null, 4, true)));

        initializer.run(null);

        ArgumentCaptor<PlaceCategory> captor =
                ArgumentCaptor.forClass(PlaceCategory.class);
        verify(placeCategoryRepository).save(captor.capture());
        PlaceCategory saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("OTHER");
        assertThat(saved.getName()).isEqualTo("기타");
        assertThat(saved.getSortOrder()).isEqualTo(5); // 기존 마지막(4) 다음
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getDescription())
                .isEqualTo("카페, 액티비티, 음식점, 바 중 어디에도 해당하지 않는 장소 종류");
    }

    @Test
    void usesSortOrderOneWhenNoCategoriesExist() {
        given(placeCategoryRepository.findByCode("OTHER"))
                .willReturn(Optional.empty());
        given(placeCategoryRepository.findFirstByOrderBySortOrderDesc())
                .willReturn(Optional.empty());

        initializer.run(null);

        ArgumentCaptor<PlaceCategory> captor =
                ArgumentCaptor.forClass(PlaceCategory.class);
        verify(placeCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(1);
    }

    @Test
    void swallowsUniqueViolationWhenCreatedConcurrently() {
        // 동시 기동으로 존재 확인 통과 후 다른 인스턴스가 먼저 저장한 상황
        given(placeCategoryRepository.findByCode("OTHER"))
                .willReturn(Optional.empty());
        given(placeCategoryRepository.findFirstByOrderBySortOrderDesc())
                .willReturn(Optional.of(PlaceCategory.of("BAR", "바", null, 4, true)));
        given(placeCategoryRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate key: OTHER"));

        // 유니크 충돌이 전파되어 기동이 실패하지 않아야 한다.
        assertThatCode(() -> initializer.run(null)).doesNotThrowAnyException();
    }

    @Test
    void reactivatesEtcCategoryWhenExistingButInactive() {
        PlaceCategory inactive = PlaceCategory.of("OTHER", "기타", "desc", 5, false);
        given(placeCategoryRepository.findByCode("OTHER"))
                .willReturn(Optional.of(inactive));

        initializer.run(null);

        ArgumentCaptor<PlaceCategory> captor =
                ArgumentCaptor.forClass(PlaceCategory.class);
        verify(placeCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isTrue();
        // 재활성화 경로에서는 신규 sortOrder 계산이 필요 없다.
        verify(placeCategoryRepository, never()).findFirstByOrderBySortOrderDesc();
    }

    @Test
    void doesNothingWhenEtcCategoryAlreadyActive() {
        given(placeCategoryRepository.findByCode("OTHER"))
                .willReturn(Optional.of(
                        PlaceCategory.of("OTHER", "기타", null, 5, true)));

        initializer.run(null);

        verify(placeCategoryRepository, never()).save(any());
        verify(placeCategoryRepository, never()).findFirstByOrderBySortOrderDesc();
    }
}
