package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseDraftRepository extends JpaRepository<CourseDraft, Long> {

    Optional<CourseDraft> findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc(
            Long memberId,
            List<CourseDraftStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cd from CourseDraft cd where cd.id = :id")
    Optional<CourseDraft> findByIdForUpdate(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("update CourseDraft cd set cd.updatedAt = CURRENT_TIMESTAMP where cd.id = :courseDraftId")
    void touchUpdatedAt(@Param("courseDraftId") Long courseDraftId);
}
