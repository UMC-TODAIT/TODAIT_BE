package com.example.TODAIT__BE.domain.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.PartTree;

class CourseDraftRepositoryTest {

    @Test
    void currentDraftLookupUsesMemberStatusAndLatestUpdatedOrdering() {
        PartTree partTree = new PartTree(
                "findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc",
                CourseDraft.class
        );

        assertThat(partTree.isLimiting()).isTrue();
        assertThat(partTree.getMaxResults()).isEqualTo(1);
        assertThat(partTree.getParts())
                .extracting(part -> part.getProperty().toDotPath())
                .containsExactly("member.id", "status");
        assertThat(partTree.getSort())
                .isEqualTo(Sort.by(
                        Sort.Order.desc("updatedAt"),
                        Sort.Order.desc("id")
                ));
    }
}
