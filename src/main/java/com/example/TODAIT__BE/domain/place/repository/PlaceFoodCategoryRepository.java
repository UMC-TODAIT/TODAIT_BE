package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceFoodCategory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceFoodCategoryRepository extends JpaRepository<PlaceFoodCategory, Long> {

    @EntityGraph(attributePaths = "foodCategory")
    List<PlaceFoodCategory> findAllByPlaceIdOrderByIdAsc(Long placeId);

    @Query("""
        select pfc.place.id as placeId,
               pfc.foodCategory.id as foodCategoryId
        from PlaceFoodCategory pfc
        where pfc.place.id in :placeIds
          and pfc.foodCategory.isActive = true
        """)
    List<PlaceFoodCategoryIdView> findFoodCategoryIdsByPlaceIds(
            @Param("placeIds") List<Long> placeIds
    );

    interface PlaceFoodCategoryIdView {

        Long getPlaceId();

        Long getFoodCategoryId();
    }
}
