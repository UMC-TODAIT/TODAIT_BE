package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceDataSource;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlaceSourceRepository extends JpaRepository<PlaceSource, Long> {

    Optional<PlaceSource> findByDataSourceAndSourcePlaceId(PlaceDataSource dataSource, String sourcePlaceId);

    Optional<PlaceSource> findByPlaceIdAndIsPrimaryTrue(Long placeId);

    @Query("""
            select ps
            from PlaceSource ps
            join fetch ps.place p
            join fetch p.area
            join fetch p.placeCategory
            join ps.dataSource ds
            where ds.code = :dataSourceCode
              and ds.isActive = true
              and ps.sourcePlaceId in :sourcePlaceIds
            """)
    List<PlaceSource> findRegisteredPlaceSources(
            @Param("dataSourceCode")
            String dataSourceCode,

            @Param("sourcePlaceIds")
            Collection<String> sourcePlaceIds
    );

    @Query("""
        select distinct ps.place.id
        from PlaceSource ps
        join ps.dataSource ds
        where ps.place.id in :placeIds
          and ds.code = :dataSourceCode
          and ds.isActive = true
        """)
    Set<Long> findPlaceIdsHavingActiveDataSource(
            @Param("placeIds")
            Collection<Long> placeIds,

            @Param("dataSourceCode")
            String dataSourceCode
    );

    @Query("""
        select distinct ps.place.id
        from PlaceSource ps
        where ps.place.id in :placeIds
        """)
    Set<Long> findPlaceIdsHavingAnySource(
            @Param("placeIds") Collection<Long> placeIds
    );
}
