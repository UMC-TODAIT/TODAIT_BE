package com.example.TODAIT__BE.domain.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:course_place_repository_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CoursePlaceRepositoryTest {

    @Autowired
    private CoursePlaceRepository coursePlaceRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findHotPlaceCandidatesOrdersByOperatorPriorityAscending() {
        Member member = persistMember("member");
        Area area = persistArea();
        PlaceCategory placeCategory = persistPlaceCategory();

        Place priorityTwo = persistPlace(
                area,
                placeCategory,
                "priority-two",
                2,
                new BigDecimal("90.0000"),
                10L
        );
        Place priorityOne = persistPlace(
                area,
                placeCategory,
                "priority-one",
                1,
                new BigDecimal("10.0000"),
                1L
        );
        Place priorityThree = persistPlace(
                area,
                placeCategory,
                "priority-three",
                3,
                new BigDecimal("100.0000"),
                100L
        );

        persistRecommendedCoursePlace(member, area, priorityTwo, 1);
        persistRecommendedCoursePlace(member, area, priorityOne, 2);
        persistRecommendedCoursePlace(member, area, priorityThree, 3);

        entityManager.flush();
        entityManager.clear();

        List<Place> candidates =
                coursePlaceRepository.findHotPlaceCandidates(
                        CourseVisibility.PUBLIC,
                        CourseSourceType.SERVICE_CREATED,
                        PlaceReviewStatus.APPROVED,
                        PlaceExposureStatus.ACTIVE,
                        PageRequest.of(0, 10)
                );

        assertThat(candidates)
                .extracting(Place::getName)
                .containsExactly(
                        "priority-one",
                        "priority-two",
                        "priority-three"
                );
    }

    private Member persistMember(String nickname) {
        Member member = Member.builder()
                .email(nickname + "@todait.test")
                .nickname(nickname)
                .build();

        setAuditFields(member);
        entityManager.persist(member);

        return member;
    }

    private Area persistArea() {
        Area area = instantiate(Area.class);
        ReflectionTestUtils.setField(area, "code", "HONGDAE");
        ReflectionTestUtils.setField(area, "name", "홍대");
        ReflectionTestUtils.setField(area, "centerLatitude", 37.5563);
        ReflectionTestUtils.setField(area, "centerLongitude", 126.9236);
        ReflectionTestUtils.setField(area, "isActive", true);
        ReflectionTestUtils.setField(area, "sortOrder", 1);

        setAuditFields(area);
        entityManager.persist(area);

        return area;
    }

    private PlaceCategory persistPlaceCategory() {
        PlaceCategory placeCategory = instantiate(PlaceCategory.class);
        ReflectionTestUtils.setField(placeCategory, "code", "CAFE");
        ReflectionTestUtils.setField(placeCategory, "name", "카페");
        ReflectionTestUtils.setField(placeCategory, "sortOrder", 1);
        ReflectionTestUtils.setField(placeCategory, "isActive", true);

        setAuditFields(placeCategory);
        entityManager.persist(placeCategory);

        return placeCategory;
    }

    private Place persistPlace(
            Area area,
            PlaceCategory placeCategory,
            String name,
            int operatorPriority,
            BigDecimal popularityScore,
            long selectedCount
    ) {
        Place place = instantiate(Place.class);
        ReflectionTestUtils.setField(place, "area", area);
        ReflectionTestUtils.setField(place, "name", name);
        ReflectionTestUtils.setField(place, "address", name + " address");
        ReflectionTestUtils.setField(place, "latitude", 37.5560);
        ReflectionTestUtils.setField(place, "longitude", 126.9230);
        ReflectionTestUtils.setField(
                place,
                "operatorPriority",
                operatorPriority
        );
        ReflectionTestUtils.setField(place, "placeCategory", placeCategory);
        ReflectionTestUtils.setField(
                place,
                "exposureStatus",
                PlaceExposureStatus.ACTIVE
        );
        ReflectionTestUtils.setField(
                place,
                "reviewStatus",
                PlaceReviewStatus.APPROVED
        );
        ReflectionTestUtils.setField(place, "isActive", true);
        ReflectionTestUtils.setField(place, "selectedCount", selectedCount);
        ReflectionTestUtils.setField(
                place,
                "popularityScore",
                popularityScore
        );

        setAuditFields(place);
        entityManager.persist(place);

        return place;
    }

    private void persistRecommendedCoursePlace(
            Member member,
            Area area,
            Place place,
            int visitOrder
    ) {
        Course course = Course.builder()
                .member(member)
                .basePlace(place)
                .area(area)
                .title("추천 코스 " + visitOrder)
                .visibility(CourseVisibility.PUBLIC)
                .sourceType(CourseSourceType.SERVICE_CREATED)
                .placeCount(1)
                .build();
        setAuditFields(course);
        entityManager.persist(course);

        CoursePlace coursePlace = CoursePlace.builder()
                .course(course)
                .place(place)
                .visitOrder(visitOrder)
                .placeRole(PlaceRole.SELECTED)
                .placeNameSnapshot(place.getName())
                .addressSnapshot(place.getAddress())
                .latitudeSnapshot(place.getLatitude())
                .longitudeSnapshot(place.getLongitude())
                .categorySnapshot(place.getPlaceCategory().getName())
                .build();

        entityManager.persist(coursePlace);
    }

    private <T> T instantiate(Class<T> type) {
        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);

            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Failed to instantiate " + type.getSimpleName(),
                    exception
            );
        }
    }

    private void setAuditFields(Object entity) {
        LocalDateTime now = LocalDateTime.of(2026, 8, 5, 0, 0);
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);
    }
}
