package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.dto.response.CategoryRecommendedPlaceResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NearBasePlaceResponseAssembler {

    private final MoodTagRepository moodTagRepository;
    private final NearBasePlaceReasonResolver reasonResolver;

    public CategoryRecommendedPlaceResponse assemble(
            RecommendationLog recommendationLog,
            Place basePlace,
            NearBasePlaceRecommendationSelection selection
    ) {
        Map<Long, MoodTag> moodTagMap =
                loadMoodTagMap(selection.places());

        List<CategoryRecommendedPlaceResponse.PlaceInfo> places =
                new ArrayList<>();

        for (int index = 0;
             index < selection.places().size();
             index++) {

            EvaluatedNearBasePlace evaluated =
                    selection.places().get(index);

            places.add(
                    toPlaceInfo(
                            evaluated,
                            index + 1,
                            moodTagMap
                    )
            );
        }

        return new CategoryRecommendedPlaceResponse(
                recommendationLog.getId(),
                recommendationLog.getCourseDraft().getId(),
                recommendationLog.getCourseDraft()
                        .getStatus()
                        .name(),

                new CategoryRecommendedPlaceResponse.PlaceCategoryInfo(
                        recommendationLog
                                .getPlaceCategory()
                                .getId(),
                        recommendationLog
                                .getPlaceCategory()
                                .getCode(),
                        recommendationLog
                                .getPlaceCategory()
                                .getName()
                ),

                new CategoryRecommendedPlaceResponse.BasePlaceInfo(
                        basePlace.getId(),
                        basePlace.getName(),
                        toAreaInfo(basePlace)
                ),

                selection.appliedRelaxationLevel(),

                places
        );
    }

    private CategoryRecommendedPlaceResponse.PlaceInfo toPlaceInfo(
            EvaluatedNearBasePlace evaluated,
            int rank,
            Map<Long, MoodTag> moodTagMap
    ) {
        Place place = evaluated.place();

        List<CategoryRecommendedPlaceResponse.MoodTagInfo>
                matchedMoodTags =
                evaluated.matchedMoodTagIds()
                        .stream()
                        .map(moodTagMap::get)
                        .filter(java.util.Objects::nonNull)
                        .map(tag ->
                                new CategoryRecommendedPlaceResponse
                                        .MoodTagInfo(
                                        tag.getId(),
                                        tag.getCode(),
                                        tag.getName()
                                )
                        )
                        .toList();

        return new CategoryRecommendedPlaceResponse.PlaceInfo(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getLatitude(),
                place.getLongitude(),

                toAreaInfo(place),

                new CategoryRecommendedPlaceResponse.CategoryInfo(
                        place.getPlaceCategory().getId(),
                        place.getPlaceCategory().getCode(),
                        place.getPlaceCategory().getName()
                ),

                place.getSubCategory(),
                place.getDefaultImageUrl(),

                rank,
                evaluated.distanceMeters(),

                evaluated.matchedMoodCount(),
                matchedMoodTags,

                evaluated.matchedFoodCount(),
                evaluated.internalScore(),

                reasonResolver.resolve(evaluated),

                false,
                true
        );
    }

    private CategoryRecommendedPlaceResponse.AreaInfo toAreaInfo(
            Place place
    ) {
        return new CategoryRecommendedPlaceResponse.AreaInfo(
                place.getArea().getId(),
                place.getArea().getCode(),
                place.getArea().getName()
        );
    }

    private Map<Long, MoodTag> loadMoodTagMap(
            List<EvaluatedNearBasePlace> places
    ) {
        Set<Long> moodTagIds = new HashSet<>();

        for (EvaluatedNearBasePlace place : places) {
            moodTagIds.addAll(
                    place.matchedMoodTagIds()
            );
        }

        if (moodTagIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, MoodTag> result = new HashMap<>();

        for (MoodTag moodTag :
                moodTagRepository
                        .findAllByIdInAndIsActiveTrue(
                                moodTagIds
                        )) {

            result.put(
                    moodTag.getId(),
                    moodTag
            );
        }

        return result;
    }
}
