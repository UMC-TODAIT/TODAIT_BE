package com.example.TODAIT__BE.domain.course.service;

import static com.example.TODAIT__BE.domain.course.service.validator.CourseDraftPlaceValidator.validateBasePlaceIntegrity;
import static com.example.TODAIT__BE.domain.course.service.validator.CourseDraftPlaceValidator.validateOrderingPlaceComposition;
import static com.example.TODAIT__BE.domain.course.service.validator.CourseDraftPlaceValidator.validateSavingPlaces;

import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.BasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.BasePlaceSaveRequest.ExternalPlace;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.FoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.MoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceOrderUpdateRequest.PlaceOrderItem;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.StatusUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.AbandonResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.BasePlace;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.BasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CreateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CurrentResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.DraftPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.MoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.OrderingEntryPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceAddResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.SavingEnterResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.StatusUpdateResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.code.ExternalPlaceRegistrationErrorCode;
import com.example.TODAIT__BE.domain.place.code.PlaceDetailErrorCode;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceDataSource;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceDataSourceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import com.example.TODAIT__BE.domain.place.service.ExternalPlaceRegistrationService;
import com.example.TODAIT__BE.domain.taxonomy.code.AreaErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.FoodCategoryErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.MoodTagErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.PlaceCategoryErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.FoodCategoryRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftService {

    private static final int MIN_MOOD_TAG_COUNT = 2;
    private static final int MAX_MOOD_TAG_COUNT = 6;
    private static final int MIN_FOOD_CATEGORY_COUNT = 1;
    private static final int BASE_VISIT_ORDER = 1;
    private static final int SELECTED_PLACE_START_ORDER = 2;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final String DEFAULT_SOURCE_TYPE = "OPERATOR";
    private static final List<CourseDraftStatus> PROGRESS_STATUSES = List.of(
            CourseDraftStatus.MOOD_SELECTING,
            CourseDraftStatus.FOOD_SELECTING,
            CourseDraftStatus.BASE_PLACE_SELECTING,
            CourseDraftStatus.PLACE_SELECTING,
            CourseDraftStatus.ORDERING,
            CourseDraftStatus.SAVING
    );

    private final CourseDraftRepository courseDraftRepository;
    private final MemberRepository memberRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final MoodTagRepository moodTagRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final PlaceRepository placeRepository;
    private final PlaceSourceRepository placeSourceRepository;
    private final PlaceDataSourceRepository dataSourceRepository;
    private final AreaRepository areaRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final ExternalPlaceRegistrationService externalPlaceRegistrationService;
    private final CourseDraftValidator courseDraftValidator;
    private final CourseDraftProperties courseDraftProperties;
    private final Clock clock;

    @Transactional
    public CreateResponse createCourseDraft(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberException(
                                MemberErrorCode.MEMBER_NOT_FOUND
                        )
                );

        CourseDraft courseDraft = CourseDraft.create(member);

        CourseDraft savedCourseDraft =
                courseDraftRepository.save(courseDraft);

        return CreateResponse.from(savedCourseDraft);
    }

    @Transactional(readOnly = true)
    public CurrentResponse getCurrentCourseDraft(Long memberId) {
        return courseDraftRepository
                .findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc(memberId, PROGRESS_STATUSES)
                .map(this::toCurrentResponse)
                .orElse(null);
    }

    @Transactional
    public MoodTagSaveResponse saveMoodTags(
            Long courseDraftId,
            Long memberId,
            MoodTagSaveRequest request
    ) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.MOOD_TAG_DRAFT_STATUS_CONFLICT,
                CourseDraftStatus.MOOD_SELECTING,
                CourseDraftStatus.FOOD_SELECTING
        );

        List<Long> moodTagIds = request.moodTagIds();

        if (moodTagIds == null
                || moodTagIds.size() < MIN_MOOD_TAG_COUNT
                || moodTagIds.size() > MAX_MOOD_TAG_COUNT) {
            throw new CourseException(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        if (new HashSet<>(moodTagIds).size() != moodTagIds.size()) {
            throw new CourseException(CourseDraftErrorCode.DUPLICATE_MOOD_TAG);
        }

        List<MoodTag> moodTags = validateAndGetMoodTags(moodTagIds);

        boolean moodTagsChanged = updateMoodTags(courseDraft, moodTags);
        deletePlacesIfPreferenceChanged(courseDraft, moodTagsChanged);

        if (courseDraft.getStatus() == CourseDraftStatus.MOOD_SELECTING) {
            courseDraft.changeStatus(CourseDraftStatus.FOOD_SELECTING);
        }

        return MoodTagSaveResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                moodTags
        );
    }

    @Transactional
    public FoodCategorySaveResponse saveFoodCategories(
            Long courseDraftId,
            Long memberId,
            FoodCategorySaveRequest request
    ) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.FOOD_CATEGORY_DRAFT_STATUS_CONFLICT,
                CourseDraftStatus.FOOD_SELECTING,
                CourseDraftStatus.BASE_PLACE_SELECTING
        );

        List<Long> foodCategoryIds = request.foodCategoryIds();

        if (foodCategoryIds == null || foodCategoryIds.size() < MIN_FOOD_CATEGORY_COUNT) {
            throw new CourseException(CourseDraftErrorCode.INVALID_FOOD_CATEGORY_COUNT);
        }

        if (new HashSet<>(foodCategoryIds).size() != foodCategoryIds.size()) {
            throw new CourseException(CourseDraftErrorCode.DUPLICATE_FOOD_CATEGORY);
        }

        List<FoodCategory> foodCategories = validateAndGetFoodCategories(foodCategoryIds);

        boolean foodCategoriesChanged = updateFoodCategories(courseDraft, foodCategories);
        deletePlacesIfPreferenceChanged(courseDraft, foodCategoriesChanged);

        courseDraft.changeStatus(CourseDraftStatus.BASE_PLACE_SELECTING);

        return FoodCategorySaveResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                foodCategories
        );
    }

    @Transactional
    public BasePlaceSaveResponse saveBasePlace(
            Long courseDraftId,
            Long memberId,
            BasePlaceSaveRequest request
    ) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatus(
                courseDraft,
                CourseDraftStatus.BASE_PLACE_SELECTING,
                CourseDraftErrorCode.BASE_PLACE_DRAFT_STATUS_CONFLICT
        );

        validateExactlyOneSource(request);

        ResolvedPlace resolvedPlace = request.placeId() != null
                ? resolveFromInternalPlace(request.placeId())
                : resolveFromExternalPlace(request.externalPlace());

        CourseDraftPlace baseDraftPlace = upsertBasePlace(courseDraft, resolvedPlace.place());

        courseDraft.changeStatus(CourseDraftStatus.PLACE_SELECTING);

        BasePlace basePlaceResponse =
                BasePlace.of(baseDraftPlace, resolvedPlace.sourceType(), resolvedPlace.isNewPlace());
        return BasePlaceSaveResponse.of(courseDraft.getId(), courseDraft.getStatus(), basePlaceResponse);
    }

    @Transactional
    public PlaceAddResponse addPlace(Long courseDraftId, Long memberId, PlaceAddRequest request) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatus(
                courseDraft,
                CourseDraftStatus.PLACE_SELECTING,
                CourseDraftErrorCode.PLACE_ADD_DRAFT_STATUS_CONFLICT
        );

        List<CourseDraftPlace> existingPlaces =
                courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(courseDraft);
        CourseDraftPlace basePlace = validateBasePlaceIntegrity(existingPlaces);

        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> new PlaceException(PlaceDetailErrorCode.PLACE_NOT_FOUND));
        validatePlaceAvailable(place);

        if (place.getId().equals(basePlace.getPlace().getId())) {
            throw new CourseException(CourseDraftErrorCode.BASE_PLACE_RESELECT_CONFLICT);
        }

        boolean alreadySelected = existingPlaces.stream()
                .anyMatch(draftPlace -> draftPlace.getPlace().getId().equals(place.getId()));
        if (alreadySelected) {
            throw new CourseException(CourseDraftErrorCode.SELECTED_PLACE_DUPLICATE);
        }

        int nextVisitOrder = existingPlaces.stream()
                .mapToInt(CourseDraftPlace::getVisitOrder)
                .max()
                .orElse(BASE_VISIT_ORDER) + 1;

        CourseDraftPlace savedPlace = courseDraftPlaceRepository.save(CourseDraftPlace.builder()
                .courseDraft(courseDraft)
                .place(place)
                .visitOrder(nextVisitOrder)
                .placeRole(PlaceRole.SELECTED)
                .build());

        int selectedPlaceCount = (int) existingPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.SELECTED)
                .count() + 1;
        int totalPlaceCount = existingPlaces.size() + 1;

        return PlaceAddResponse.of(
                courseDraft.getId(), courseDraft.getStatus(), savedPlace, selectedPlaceCount, totalPlaceCount
        );
    }

    @Transactional
    public OrderingEntryResponse enterOrdering(Long courseDraftId, Long memberId) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.ORDERING_ENTRY_STATUS_CONFLICT,
                CourseDraftStatus.PLACE_SELECTING,
                CourseDraftStatus.ORDERING
        );

        List<CourseDraftPlace> places = courseDraftPlaceRepository
                .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(courseDraftId);

        int selectedPlaceCount = validateOrderingPlaceComposition(places);

        if (courseDraft.getStatus() == CourseDraftStatus.PLACE_SELECTING) {
            courseDraft.changeStatus(CourseDraftStatus.ORDERING);
        }

        return new OrderingEntryResponse(
                courseDraft.getId(),
                courseDraft.getStatus(),
                places.size(),
                selectedPlaceCount,
                places.stream()
                        .map(OrderingEntryPlaceResponse::from)
                        .toList()
        );
    }

    @Transactional
    public PlaceOrderUpdateResponse updatePlaceOrder(
            Long courseDraftId,
            Long memberId,
            PlaceOrderUpdateRequest request
    ) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatus(
                courseDraft,
                CourseDraftStatus.ORDERING,
                CourseDraftErrorCode.PLACE_ORDER_DRAFT_STATUS_CONFLICT
        );

        List<PlaceOrderItem> placeOrders = request.placeOrders() != null ? request.placeOrders() : List.of();

        List<CourseDraftPlace> allPlaces =
                courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(courseDraft);

        List<CourseDraftPlace> targetPlaces = resolveTargetPlaces(allPlaces, placeOrders);
        validateVisitOrders(allPlaces, placeOrders);

        updateVisitOrders(targetPlaces, placeOrders);

        List<DraftPlaceResponse> responses = allPlaces.stream()
                .sorted(Comparator.comparing(CourseDraftPlace::getVisitOrder))
                .map(DraftPlaceResponse::from)
                .toList();

        return PlaceOrderUpdateResponse.of(courseDraft.getId(), responses);
    }

    @Transactional
    public SavingEnterResponse enterSaving(Long courseDraftId, Long memberId) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT,
                CourseDraftStatus.ORDERING,
                CourseDraftStatus.SAVING
        );

        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(courseDraft);
        validateSavingPlaces(draftPlaces);

        if (courseDraft.getStatus() == CourseDraftStatus.ORDERING) {
            courseDraft.changeStatus(CourseDraftStatus.SAVING);
        }

        List<DraftPlaceResponse> routePreview = draftPlaces.stream()
                .map(DraftPlaceResponse::from)
                .toList();

        return SavingEnterResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                routePreview
        );
    }

    @Transactional
    public StatusUpdateResponse updateStatus(
            Long courseDraftId,
            Long memberId,
            StatusUpdateRequest request
    ) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);

        CourseDraftStatus targetStatus = request.targetStatus();
        validateBackwardStatus(courseDraft.getStatus(), targetStatus);
        validateBeforeStatusChange(courseDraft, targetStatus);
        courseDraft.changeStatus(targetStatus);

        return StatusUpdateResponse.of(courseDraft);
    }

    @Transactional
    public AbandonResponse abandonCourseDraft(Long courseDraftId, Long memberId) {
        CourseDraft courseDraft = getCourseDraftForUpdate(courseDraftId);

        courseDraftValidator.validateOwner(courseDraft, memberId);
        if (courseDraft.getStatus() == CourseDraftStatus.COMPLETED
                || courseDraft.getStatus() == CourseDraftStatus.ABANDONED) {
            throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
        }

        courseDraft.abandon(LocalDateTime.now(clock).plusDays(courseDraftProperties.terminalRetentionDays()));

        return AbandonResponse.from(courseDraft);
    }

    private CourseDraft getCourseDraftForUpdate(Long courseDraftId) {
        return courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));
    }

    private CurrentResponse toCurrentResponse(CourseDraft courseDraft) {
        return CurrentResponse.of(
                courseDraft,
                courseDraftMoodTagRepository.findByCourseDraftOrderByIdAsc(courseDraft),
                courseDraftFoodCategoryRepository.findByCourseDraftOrderByIdAsc(courseDraft),
                courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(courseDraft)
        );
    }

    private void validateBackwardStatus(
            CourseDraftStatus currentStatus,
            CourseDraftStatus targetStatus
    ) {
        if (!currentStatus.canMoveBackTo(targetStatus)) {
            throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
        }
    }

    private void validateBeforeStatusChange(
            CourseDraft courseDraft,
            CourseDraftStatus targetStatus
    ) {
        switch (targetStatus) {
            case MOOD_SELECTING, FOOD_SELECTING, BASE_PLACE_SELECTING, PLACE_SELECTING -> {
            }
            case ORDERING -> {
                List<CourseDraftPlace> places = courseDraftPlaceRepository
                        .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(courseDraft.getId());
                validateOrderingPlaceComposition(places);
            }
            default -> throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
        }
    }

    private List<MoodTag> validateAndGetMoodTags(List<Long> moodTagIds) {
        List<MoodTag> foundMoodTags = moodTagRepository.findByIdInAndIsActiveTrue(moodTagIds);
        if (foundMoodTags.size() != moodTagIds.size()) {
            throw new TaxonomyException(MoodTagErrorCode.MOOD_TAG_NOT_FOUND);
        }

        Map<Long, MoodTag> moodTagsById = foundMoodTags.stream()
                .collect(Collectors.toMap(MoodTag::getId, Function.identity()));
        return moodTagIds.stream()
                .map(moodTagsById::get)
                .toList();
    }

    private boolean updateMoodTags(CourseDraft courseDraft, List<MoodTag> moodTags) {
        List<CourseDraftMoodTag> existingMoodTags = courseDraftMoodTagRepository.findByCourseDraft(courseDraft);
        Set<Long> requestedMoodTagIds = moodTags.stream()
                .map(MoodTag::getId)
                .collect(Collectors.toSet());
        Set<Long> existingMoodTagIds = existingMoodTags.stream()
                .map(courseDraftMoodTag -> courseDraftMoodTag.getMoodTag().getId())
                .collect(Collectors.toSet());
        boolean changed = !requestedMoodTagIds.equals(existingMoodTagIds);

        List<CourseDraftMoodTag> moodTagsToDelete = existingMoodTags.stream()
                .filter(courseDraftMoodTag -> !requestedMoodTagIds.contains(courseDraftMoodTag.getMoodTag().getId()))
                .toList();
        courseDraftMoodTagRepository.deleteAll(moodTagsToDelete);

        moodTags.stream()
                .filter(moodTag -> !existingMoodTagIds.contains(moodTag.getId()))
                .map(moodTag -> CourseDraftMoodTag.builder()
                        .courseDraft(courseDraft)
                        .moodTag(moodTag)
                        .build())
                .forEach(courseDraftMoodTagRepository::save);

        return changed;
    }

    private List<FoodCategory> validateAndGetFoodCategories(List<Long> foodCategoryIds) {
        List<FoodCategory> foundFoodCategories = foodCategoryRepository.findByIdInAndIsActiveTrue(foodCategoryIds);
        if (foundFoodCategories.size() != foodCategoryIds.size()) {
            throw new TaxonomyException(FoodCategoryErrorCode.FOOD_CATEGORY_NOT_FOUND);
        }

        Map<Long, FoodCategory> foodCategoriesById = foundFoodCategories.stream()
                .collect(Collectors.toMap(FoodCategory::getId, Function.identity()));
        return foodCategoryIds.stream()
                .map(foodCategoriesById::get)
                .toList();
    }

    private boolean updateFoodCategories(CourseDraft courseDraft, List<FoodCategory> foodCategories) {
        List<CourseDraftFoodCategory> existingFoodCategories =
                courseDraftFoodCategoryRepository.findByCourseDraft(courseDraft);
        Set<Long> requestedFoodCategoryIds = foodCategories.stream()
                .map(FoodCategory::getId)
                .collect(Collectors.toSet());
        Set<Long> existingFoodCategoryIds = existingFoodCategories.stream()
                .map(courseDraftFoodCategory -> courseDraftFoodCategory.getFoodCategory().getId())
                .collect(Collectors.toSet());
        boolean changed = !requestedFoodCategoryIds.equals(existingFoodCategoryIds);

        List<CourseDraftFoodCategory> foodCategoriesToDelete = existingFoodCategories.stream()
                .filter(courseDraftFoodCategory ->
                        !requestedFoodCategoryIds.contains(courseDraftFoodCategory.getFoodCategory().getId()))
                .toList();
        courseDraftFoodCategoryRepository.deleteAll(foodCategoriesToDelete);

        foodCategories.stream()
                .filter(foodCategory -> !existingFoodCategoryIds.contains(foodCategory.getId()))
                .map(foodCategory -> CourseDraftFoodCategory.builder()
                        .courseDraft(courseDraft)
                        .foodCategory(foodCategory)
                        .build())
                .forEach(courseDraftFoodCategoryRepository::save);

        return changed;
    }

    private void deletePlacesIfPreferenceChanged(CourseDraft courseDraft, boolean preferenceChanged) {
        if (preferenceChanged && courseDraftPlaceRepository.existsByCourseDraft(courseDraft)) {
            courseDraftPlaceRepository.deleteByCourseDraft(courseDraft);
        }
    }

    private void validateExactlyOneSource(BasePlaceSaveRequest request) {
        boolean hasPlaceId = request.placeId() != null;
        boolean hasExternalPlace = request.externalPlace() != null;

        if (hasPlaceId && hasExternalPlace) {
            throw new CourseException(CourseDraftErrorCode.BASE_PLACE_SOURCE_CONFLICT);
        }
        if (!hasPlaceId && !hasExternalPlace) {
            throw new CourseException(CourseDraftErrorCode.BASE_PLACE_SOURCE_MISSING);
        }
    }

    private ResolvedPlace resolveFromInternalPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceException(PlaceDetailErrorCode.PLACE_NOT_FOUND));

        validatePlaceAvailable(place);

        String sourceType = placeSourceRepository.findByPlaceIdAndIsPrimaryTrue(place.getId())
                .map(placeSource -> placeSource.getDataSource().getCode())
                .orElse(DEFAULT_SOURCE_TYPE);

        return new ResolvedPlace(place, sourceType, false);
    }

    private ResolvedPlace resolveFromExternalPlace(ExternalPlace externalPlace) {
        validateExternalPlaceRequiredFields(externalPlace);
        validateCoordinates(externalPlace.latitude(), externalPlace.longitude());

        PlaceDataSource dataSource = dataSourceRepository.findByCodeAndIsActiveTrue(externalPlace.dataSourceCode())
                .orElseThrow(() -> new PlaceException(ExternalPlaceRegistrationErrorCode.DATA_SOURCE_NOT_FOUND));

        Area area = areaRepository.findByCode(externalPlace.areaCode())
                .filter(Area::getIsActive)
                .orElseThrow(() -> new TaxonomyException(AreaErrorCode.AREA_NOT_SUPPORTED));

        PlaceCategory placeCategory = placeCategoryRepository.findByCode(externalPlace.categoryCode())
                .filter(PlaceCategory::getIsActive)
                .orElseThrow(() -> new TaxonomyException(PlaceCategoryErrorCode.PLACE_CATEGORY_NOT_SUPPORTED));

        Optional<PlaceSource> existingSource =
                placeSourceRepository.findByDataSourceAndSourcePlaceId(dataSource, externalPlace.sourcePlaceId());
        if (existingSource.isPresent()) {
            Place existingPlace = existingSource.get().getPlace();
            validatePlaceAvailable(existingPlace);
            return new ResolvedPlace(existingPlace, dataSource.getCode(), false);
        }

        return createExternalPlace(dataSource, area, placeCategory, externalPlace);
    }

    private ResolvedPlace createExternalPlace(
            PlaceDataSource dataSource,
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
            throw new CourseException(CourseDraftErrorCode.BASE_PLACE_SOURCE_MISSING);
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        boolean valid = latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE
                && longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;

        if (!valid) {
            throw new PlaceException(ExternalPlaceRegistrationErrorCode.INVALID_PLACE_COORDINATE);
        }
    }

    private CourseDraftPlace upsertBasePlace(CourseDraft courseDraft, Place place) {
        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(courseDraft);
        List<CourseDraftPlace> baseDraftPlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.BASE)
                .toList();

        if (baseDraftPlaces.isEmpty()) {
            return courseDraftPlaceRepository.save(CourseDraftPlace.builder()
                    .courseDraft(courseDraft)
                    .place(place)
                    .visitOrder(BASE_VISIT_ORDER)
                    .placeRole(PlaceRole.BASE)
                    .build());
        }

        CourseDraftPlace baseDraftPlace = baseDraftPlaces.get(0);
        CourseDraftPlace selectedDraftPlace = findSelectedDraftPlaceByPlaceId(draftPlaces, place.getId());
        if (selectedDraftPlace != null) {
            swapBasePlace(baseDraftPlace, selectedDraftPlace, nextTemporaryVisitOrder(draftPlaces));
            return selectedDraftPlace;
        }

        baseDraftPlace.updatePlace(place);
        return baseDraftPlace;
    }

    private CourseDraftPlace findSelectedDraftPlaceByPlaceId(
            List<CourseDraftPlace> draftPlaces,
            Long placeId
    ) {
        return draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.SELECTED)
                .filter(draftPlace -> draftPlace.getPlace() != null)
                .filter(draftPlace -> placeId.equals(draftPlace.getPlace().getId()))
                .findFirst()
                .orElse(null);
    }

    private void swapBasePlace(
            CourseDraftPlace baseDraftPlace,
            CourseDraftPlace selectedDraftPlace,
            int temporaryVisitOrder
    ) {
        Integer selectedVisitOrder = selectedDraftPlace.getVisitOrder();
        baseDraftPlace.updateRoleAndVisitOrder(
                PlaceRole.SELECTED,
                temporaryVisitOrder
        );
        courseDraftPlaceRepository.flush();

        selectedDraftPlace.updateRoleAndVisitOrder(PlaceRole.BASE, BASE_VISIT_ORDER);
        courseDraftPlaceRepository.flush();

        baseDraftPlace.updateRoleAndVisitOrder(PlaceRole.SELECTED, selectedVisitOrder);
    }

    private int nextTemporaryVisitOrder(List<CourseDraftPlace> draftPlaces) {
        return draftPlaces.stream()
                .map(CourseDraftPlace::getVisitOrder)
                .filter(visitOrder -> visitOrder != null)
                .max(Integer::compareTo)
                .orElse(BASE_VISIT_ORDER) + 1;
    }

    private void validatePlaceAvailable(Place place) {
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
            throw new PlaceException(ExternalPlaceRegistrationErrorCode.PLACE_NOT_AVAILABLE);
        }
    }

    private List<CourseDraftPlace> resolveTargetPlaces(
            List<CourseDraftPlace> allPlaces,
            List<PlaceOrderItem> placeOrders
    ) {
        Map<Long, CourseDraftPlace> placesById = allPlaces.stream()
                .collect(Collectors.toMap(CourseDraftPlace::getId, Function.identity()));

        List<CourseDraftPlace> targetPlaces = new ArrayList<>();
        for (PlaceOrderItem item : placeOrders) {
            CourseDraftPlace place = placesById.get(item.courseDraftPlaceId());
            if (place == null) {
                throw new CourseException(CourseDraftErrorCode.SELECTED_PLACE_NOT_FOUND);
            }
            targetPlaces.add(place);
        }
        return targetPlaces;
    }

    private void updateVisitOrders(List<CourseDraftPlace> targetPlaces, List<PlaceOrderItem> placeOrders) {
        for (int i = 0; i < targetPlaces.size(); i++) {
            targetPlaces.get(i).updateRoleAndVisitOrder(
                    PlaceRole.SELECTED,
                    targetPlaces.size() + SELECTED_PLACE_START_ORDER + i
            );
        }
        courseDraftPlaceRepository.flush();

        for (int i = 0; i < targetPlaces.size(); i++) {
            Integer visitOrder = placeOrders.get(i).visitOrder();
            targetPlaces.get(i).updateRoleAndVisitOrder(
                    visitOrder.equals(BASE_VISIT_ORDER)
                            ? PlaceRole.BASE
                            : PlaceRole.SELECTED,
                    visitOrder
            );
        }
    }

    private void validateVisitOrders(List<CourseDraftPlace> allPlaces, List<PlaceOrderItem> placeOrders) {
        List<Integer> sortedOrders = placeOrders.stream()
                .map(PlaceOrderItem::visitOrder)
                .sorted()
                .toList();
        for (int i = 0; i < sortedOrders.size(); i++) {
            if (!sortedOrders.get(i).equals(BASE_VISIT_ORDER + i)) {
                throw new CourseException(CourseDraftErrorCode.INVALID_VISIT_ORDER);
            }
        }

        Set<Long> requestedIds = placeOrders.stream()
                .map(PlaceOrderItem::courseDraftPlaceId)
                .collect(Collectors.toSet());
        Set<Long> actualPlaceIds = allPlaces.stream()
                .map(CourseDraftPlace::getId)
                .collect(Collectors.toSet());

        if (requestedIds.size() != placeOrders.size() || !requestedIds.equals(actualPlaceIds)) {
            throw new CourseException(CourseDraftErrorCode.INVALID_VISIT_ORDER);
        }
    }

    private record ResolvedPlace(Place place, String sourceType, boolean isNewPlace) {
    }
}
