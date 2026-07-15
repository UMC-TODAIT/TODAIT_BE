package com.example.TODAIT__BE.domain.taxonomy.repository;

import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodTagRepository extends JpaRepository<MoodTag, Long> {

    Optional<MoodTag> findByCode(String code);

    boolean existsByCode(String code);

    List<MoodTag> findAllByIsActiveTrueOrderBySortOrderAsc();
}
