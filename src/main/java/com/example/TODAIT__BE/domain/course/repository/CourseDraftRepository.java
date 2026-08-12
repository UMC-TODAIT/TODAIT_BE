package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            select cd.id
            from CourseDraft cd
            where cd.status in :statuses
              and cd.expiresAt <= :now
            order by cd.expiresAt asc, cd.id asc
            """)
    List<Long> findExpiredTerminalDraftIds(
            @Param("statuses") List<CourseDraftStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from CourseDraft cd
            where cd.id in :ids
              and cd.status in :statuses
              and cd.expiresAt <= :now
            """)
    int deleteExpiredTerminalDraftsByIdIn(
            @Param("ids") List<Long> ids,
            @Param("statuses") List<CourseDraftStatus> statuses,
            @Param("now") LocalDateTime now
    );
}
