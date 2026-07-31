package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceImage;
import com.example.TODAIT__BE.domain.place.enums.PlaceImageType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceImageRepository
        extends JpaRepository<PlaceImage, Long> {

    List<PlaceImage> findAllByPlaceIdAndImageTypeOrderByDisplayOrderAscIdAsc(
            Long placeId,
            PlaceImageType imageType
    );

    @Query("""
            select pi.place.id as placeId,
                   pi.imageUrl as imageUrl
            from PlaceImage pi
            where pi.place.id in :placeIds
              and pi.isPrimary = true
            order by pi.place.id asc, pi.displayOrder asc
            """)
    List<PrimaryImageUrlView> findPrimaryImageUrlsByPlaceIds(
            @Param("placeIds") List<Long> placeIds
    );

    interface PrimaryImageUrlView {

        Long getPlaceId();

        String getImageUrl();
    }
}
