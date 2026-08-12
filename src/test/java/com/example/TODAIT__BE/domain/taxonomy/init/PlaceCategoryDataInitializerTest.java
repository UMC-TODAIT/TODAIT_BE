package com.example.TODAIT__BE.domain.taxonomy.init;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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
    void doesNothingWhenEtcCategoryAlreadyExists() {
        given(placeCategoryRepository.findByCode("OTHER"))
                .willReturn(Optional.of(
                        PlaceCategory.of("OTHER", "기타", null, 5, true)));

        initializer.run(null);

        verify(placeCategoryRepository, never()).save(any());
        verify(placeCategoryRepository, never()).findFirstByOrderBySortOrderDesc();
    }
}
