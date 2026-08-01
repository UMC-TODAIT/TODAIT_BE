package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest.ExternalPlace;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse.BasePlace;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.place.entity.DataSource;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.repository.DataSourceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import com.example.TODAIT__BE.domain.place.service.ExternalPlaceRegistrationService;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftBasePlaceService {

    private static final int BASE_VISIT_ORDER = 1;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final String DEFAULT_SOURCE_TYPE = "OPERATOR";

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final PlaceRepository placeRepository;
    private final PlaceSourceRepository placeSourceRepository;
    private final DataSourceRepository dataSourceRepository;
    private final AreaRepository areaRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final ExternalPlaceRegistrationService externalPlaceRegistrationService;

    @Transactional
    public CourseDraftBasePlaceSaveResponse saveBasePlace(
            Long courseDraftId,
            Long memberId,
            CourseDraftBasePlaceSaveRequest request
    ) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
        }

        if (courseDraft.getStatus() != CourseDraftStatus.BASE_PLACE_SELECTING) {
            throw new CourseException(CourseErrorCode.BASE_PLACE_DRAFT_STATUS_CONFLICT);
        }

        validateExactlyOneSource(request);

        ResolvedPlace resolvedPlace = request.placeId() != null
                ? resolveFromInternalPlace(request.placeId())
                : resolveFromExternalPlace(request.externalPlace());

        CourseDraftPlace baseDraftPlace = upsertBasePlace(courseDraft, resolvedPlace.place());

        courseDraft.changeStatus(CourseDraftStatus.PLACE_SELECTING);

        BasePlace basePlaceResponse =
                BasePlace.of(baseDraftPlace, resolvedPlace.sourceType(), resolvedPlace.isNewPlace());
        return CourseDraftBasePlaceSaveResponse.of(courseDraft.getId(), courseDraft.getStatus(), basePlaceResponse);
    }

    private void validateExactlyOneSource(CourseDraftBasePlaceSaveRequest request) {
        boolean hasPlaceId = request.placeId() != null;
        boolean hasExternalPlace = request.externalPlace() != null;

        if (hasPlaceId && hasExternalPlace) {
            throw new CourseException(CourseErrorCode.BASE_PLACE_SOURCE_CONFLICT);
        }
        if (!hasPlaceId && !hasExternalPlace) {
            throw new CourseException(CourseErrorCode.BASE_PLACE_SOURCE_MISSING);
        }
    }

    private ResolvedPlace resolveFromInternalPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceException(PlaceErrorCode.PLACE_NOT_FOUND));

        validateAvailablePlace(place);

        String sourceType = placeSourceRepository.findByPlaceIdAndIsPrimaryTrue(place.getId())
                .map(placeSource -> placeSource.getDataSource().getCode())
                .orElse(DEFAULT_SOURCE_TYPE);

        return new ResolvedPlace(place, sourceType, false);
    }

    private void validateAvailablePlace(Place place) {
        Area area = place.getArea();
        PlaceCategory placeCategory = place.getPlaceCategory();

        boolean available = Boolean.TRUE.equals(place.getIsActive())
                && place.getReviewStatus() == PlaceReviewStatus.APPROVED
                && place.getExposureStatus() == PlaceExposureStatus.ACTIVE
                && place.getDeletedAt() == null
                && area != null && Boolean.TRUE.equals(area.getIsActive())
                && placeCategory != null && Boolean.TRUE.equals(placeCategory.getIsActive())
                && place.getLatitude() != null
                && place.getLongitude() != null;

        if (!available) {
            throw new PlaceException(PlaceErrorCode.PLACE_NOT_AVAILABLE);
        }
    }

    private ResolvedPlace resolveFromExternalPlace(ExternalPlace externalPlace) {
        validateExternalPlaceRequiredFields(externalPlace);
        validateCoordinates(externalPlace.latitude(), externalPlace.longitude());

        DataSource dataSource = dataSourceRepository.findByCode(externalPlace.dataSourceCode())
                .orElseThrow(() -> new PlaceException(PlaceErrorCode.DATA_SOURCE_NOT_FOUND));

        Area area = areaRepository.findByCode(externalPlace.areaCode())
                .filter(Area::getIsActive)
                .orElseThrow(() -> new TaxonomyException(TaxonomyErrorCode.AREA_NOT_SUPPORTED));

        PlaceCategory placeCategory = placeCategoryRepository.findByCode(externalPlace.categoryCode())
                .filter(PlaceCategory::getIsActive)
                .orElseThrow(() -> new TaxonomyException(TaxonomyErrorCode.PLACE_CATEGORY_NOT_SUPPORTED));

        Optional<PlaceSource> existingSource =
                placeSourceRepository.findByDataSourceAndSourcePlaceId(dataSource, externalPlace.sourcePlaceId());
        if (existingSource.isPresent()) {
            Place existingPlace = existingSource.get().getPlace();
            validateAvailablePlace(existingPlace);
            return new ResolvedPlace(existingPlace, dataSource.getCode(), false);
        }

        return createExternalPlace(dataSource, area, placeCategory, externalPlace);
    }

    private ResolvedPlace createExternalPlace(
            DataSource dataSource,
            Area area,
            PlaceCategory placeCategory,
            ExternalPlace externalPlace
    ) {
        try {
            Place savedPlace = externalPlaceRegistrationService.register(
                    area,
                    placeCategory,
                    dataSource,
                    externalPlace.name(),
                    externalPlace.address(),
                    externalPlace.roadAddress(),
                    externalPlace.latitude(),
                    externalPlace.longitude(),
                    externalPlace.phone(),
                    externalPlace.subCategory(),
                    externalPlace.sourcePlaceId(),
                    externalPlace.sourceUrl()
            );
            return new ResolvedPlace(savedPlace, dataSource.getCode(), true);
        } catch (DataIntegrityViolationException e) {
            PlaceSource reloaded = placeSourceRepository
                    .findByDataSourceAndSourcePlaceId(dataSource, externalPlace.sourcePlaceId())
                    .orElseThrow(() -> e);
            return new ResolvedPlace(reloaded.getPlace(), dataSource.getCode(), false);
        }
    }

    private void validateExternalPlaceRequiredFields(ExternalPlace externalPlace) {
        boolean valid = isNotBlank(externalPlace.dataSourceCode())
                && isNotBlank(externalPlace.sourcePlaceId())
                && isNotBlank(externalPlace.name())
                && isNotBlank(externalPlace.address())
                && isNotBlank(externalPlace.areaCode())
                && isNotBlank(externalPlace.categoryCode())
                && externalPlace.latitude() != null
                && externalPlace.longitude() != null;

        if (!valid) {
            throw new CourseException(CourseErrorCode.BASE_PLACE_SOURCE_MISSING);
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        boolean valid = latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE
                && longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;

        if (!valid) {
            throw new PlaceException(PlaceErrorCode.INVALID_PLACE_COORDINATE);
        }
    }

    private CourseDraftPlace upsertBasePlace(CourseDraft courseDraft, Place place) {
        List<CourseDraftPlace> baseDraftPlaces =
                courseDraftPlaceRepository.findByCourseDraftAndPlaceRole(courseDraft, PlaceRole.BASE);

        if (baseDraftPlaces.isEmpty()) {
            return courseDraftPlaceRepository.save(CourseDraftPlace.builder()
                    .courseDraft(courseDraft)
                    .place(place)
                    .visitOrder(BASE_VISIT_ORDER)
                    .placeRole(PlaceRole.BASE)
                    .build());
        }

        CourseDraftPlace baseDraftPlace = baseDraftPlaces.get(0);
        baseDraftPlace.updatePlace(place);
        return baseDraftPlace;
    }

    private record ResolvedPlace(Place place, String sourceType, boolean isNewPlace) {
    }
}
