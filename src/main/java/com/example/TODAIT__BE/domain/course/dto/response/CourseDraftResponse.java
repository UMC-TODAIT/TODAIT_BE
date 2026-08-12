package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import java.time.LocalDateTime;
import java.util.List;

public final class CourseDraftResponse {

    private CourseDraftResponse() {
    }

    public record CreateResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            LocalDateTime createdAt
    ) {

        public static CreateResponse from(CourseDraft courseDraft) {
            return new CreateResponse(
                    courseDraft.getId(),
                    courseDraft.getStatus(),
                    courseDraft.getCreatedAt()
            );
        }
    }

    public record MoodTagSaveResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            List<MoodTagItem> moodTags
    ) {

        public static MoodTagSaveResponse of(
                Long courseDraftId,
                CourseDraftStatus draftStatus,
                List<MoodTag> moodTags
        ) {
            return new MoodTagSaveResponse(
                    courseDraftId,
                    draftStatus,
                    moodTags.stream().map(MoodTagItem::from).toList()
            );
        }
    }

    public record MoodTagItem(
            Long moodTagId,
            String code,
            String name
    ) {
        public static MoodTagItem from(MoodTag moodTag) {
            return new MoodTagItem(moodTag.getId(), moodTag.getCode(), moodTag.getName());
        }
    }

    public record FoodCategorySaveResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            List<FoodCategoryItem> foodCategories
    ) {

        public static FoodCategorySaveResponse of(
                Long courseDraftId,
                CourseDraftStatus draftStatus,
                List<FoodCategory> foodCategories
        ) {
            return new FoodCategorySaveResponse(
                    courseDraftId,
                    draftStatus,
                    foodCategories.stream().map(FoodCategoryItem::from).toList()
            );
        }
    }

    public record FoodCategoryItem(
            Long foodCategoryId,
            String code,
            String name
    ) {
        public static FoodCategoryItem from(FoodCategory foodCategory) {
            return new FoodCategoryItem(foodCategory.getId(), foodCategory.getCode(), foodCategory.getName());
        }
    }

    public record BasePlaceSaveResponse(
            Long courseDraftId,
            BasePlace basePlace,
            CourseDraftStatus draftStatus
    ) {

        public static BasePlaceSaveResponse of(
                Long courseDraftId,
                CourseDraftStatus draftStatus,
                BasePlace basePlace
        ) {
            return new BasePlaceSaveResponse(courseDraftId, basePlace, draftStatus);
        }
    }

    public record BasePlace(
            Long placeId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            AreaSummary area,
            CategorySummary category,
            String subCategory,
            String sourceType,
            Boolean isNewPlace,
            Integer visitOrder,
            PlaceRole placeRole
    ) {
        public static BasePlace of(CourseDraftPlace baseDraftPlace, String sourceType, boolean isNewPlace) {
            Place place = baseDraftPlace.getPlace();
            return new BasePlace(
                    place.getId(),
                    place.getName(),
                    place.getAddress(),
                    place.getRoadAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    AreaSummary.from(place.getArea()),
                    CategorySummary.from(place.getPlaceCategory()),
                    place.getSubCategory(),
                    sourceType,
                    isNewPlace,
                    baseDraftPlace.getVisitOrder(),
                    baseDraftPlace.getPlaceRole()
            );
        }
    }

    public record PlaceAddResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            AddedPlace addedPlace,
            Integer selectedPlaceCount,
            Integer totalPlaceCount
    ) {

        public static PlaceAddResponse of(
                Long courseDraftId,
                CourseDraftStatus draftStatus,
                CourseDraftPlace addedDraftPlace,
                int selectedPlaceCount,
                int totalPlaceCount
        ) {
            return new PlaceAddResponse(
                    courseDraftId,
                    draftStatus,
                    AddedPlace.from(addedDraftPlace),
                    selectedPlaceCount,
                    totalPlaceCount
            );
        }
    }

    public record AddedPlace(
            Long courseDraftPlaceId,
            Long placeId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            AreaSummary area,
            CategorySummary category,
            String subCategory,
            String imageUrl,
            Integer visitOrder,
            PlaceRole placeRole
    ) {
        public static AddedPlace from(CourseDraftPlace draftPlace) {
            Place place = draftPlace.getPlace();
            return new AddedPlace(
                    draftPlace.getId(),
                    place.getId(),
                    place.getName(),
                    place.getAddress(),
                    place.getRoadAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    AreaSummary.from(place.getArea()),
                    CategorySummary.from(place.getPlaceCategory()),
                    place.getSubCategory(),
                    place.getDefaultImageUrl(),
                    draftPlace.getVisitOrder(),
                    draftPlace.getPlaceRole()
            );
        }
    }

    public record DraftPlaceResponse(
            Long courseDraftPlaceId,
            Long placeId,
            PlaceRole placeRole,
            Integer visitOrder,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {

        public static DraftPlaceResponse from(CourseDraftPlace courseDraftPlace) {
            Place place = courseDraftPlace.getPlace();
            return new DraftPlaceResponse(
                    courseDraftPlace.getId(),
                    place.getId(),
                    courseDraftPlace.getPlaceRole(),
                    courseDraftPlace.getVisitOrder(),
                    place.getName(),
                    place.getAddress(),
                    place.getLatitude(),
                    place.getLongitude()
            );
        }
    }

    public record SavingEnterResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            int totalPlaceCount,
            List<DraftPlaceResponse> routePreview
    ) {

        public static SavingEnterResponse of(
                Long courseDraftId,
                CourseDraftStatus draftStatus,
                List<DraftPlaceResponse> routePreview
        ) {
            return new SavingEnterResponse(
                    courseDraftId,
                    draftStatus,
                    routePreview.size(),
                    routePreview
            );
        }
    }

    public record OrderingEntryResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            int totalPlaceCount,
            int selectedPlaceCount,
            List<OrderingEntryPlaceResponse> places
    ) {
    }

    public record OrderingEntryPlaceResponse(
            Long courseDraftPlaceId,
            Long placeId,
            Integer visitOrder,
            PlaceRole placeRole,
            boolean draggable,
            boolean deletable,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude
    ) {

        public static OrderingEntryPlaceResponse from(CourseDraftPlace courseDraftPlace) {
            Place place = courseDraftPlace.getPlace();
            return new OrderingEntryPlaceResponse(
                    courseDraftPlace.getId(),
                    place.getId(),
                    courseDraftPlace.getVisitOrder(),
                    courseDraftPlace.getPlaceRole(),
                    true,
                    false,
                    place.getName(),
                    place.getAddress(),
                    place.getRoadAddress(),
                    place.getLatitude(),
                    place.getLongitude()
            );
        }
    }

    public record PlaceOrderUpdateResponse(
            Long courseDraftId,
            List<DraftPlaceResponse> places
    ) {

        public static PlaceOrderUpdateResponse of(Long courseDraftId, List<DraftPlaceResponse> places) {
            return new PlaceOrderUpdateResponse(courseDraftId, places);
        }
    }

    public record StatusUpdateResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus
    ) {

        public static StatusUpdateResponse of(CourseDraft courseDraft) {
            return new StatusUpdateResponse(
                    courseDraft.getId(),
                    courseDraft.getStatus()
            );
        }
    }

    public record AbandonResponse(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            LocalDateTime expiresAt
    ) {

        public static AbandonResponse from(CourseDraft courseDraft) {
            return new AbandonResponse(
                    courseDraft.getId(),
                    courseDraft.getStatus(),
                    courseDraft.getExpiresAt()
            );
        }
    }

    public record AreaSummary(
            Long areaId,
            String code,
            String name
    ) {
        public static AreaSummary from(Area area) {
            return new AreaSummary(area.getId(), area.getCode(), area.getName());
        }
    }

    public record CategorySummary(
            Long placeCategoryId,
            String code,
            String name
    ) {
        public static CategorySummary from(PlaceCategory placeCategory) {
            return new CategorySummary(placeCategory.getId(), placeCategory.getCode(), placeCategory.getName());
        }
    }
}
