package com.example.TODAIT__BE.domain.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CourseDraftRepositoryTest {

    @Test
    void currentDraftLookupUsesMemberStatusAndLatestUpdatedOrdering() throws NoSuchMethodException {
        Method method = CourseDraftRepository.class.getMethod(
                "findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc",
                Long.class,
                List.class
        );

        assertThat(method.getReturnType()).isEqualTo(Optional.class);
        assertThat(method.getGenericReturnType().getTypeName())
                .contains(CourseDraft.class.getSimpleName());
        assertThat(method.getGenericParameterTypes()[1].getTypeName())
                .contains(CourseDraftStatus.class.getSimpleName());
    }
}
