package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KakaoPlaceAreaResolver {
    private static final List<AreaRule> AREA_RULES = List.of(
            new AreaRule(
                    "HONGDAE",
                    List.of(
                            "서교동",
                            "동교동",
                            "합정동",
                            "상수동"
                    )
            ),
            new AreaRule(
                    "YEONNAM",
                    List.of("연남동")
            ),
            new AreaRule(
                    "SEONGSU",
                    List.of(
                            "성수동1가",
                            "성수동2가"
                    )
            )
    );

    private final AreaRepository areaRepository;

    public Map<String, Area> getActiveAreasByCode() {
        return areaRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .collect(
                        Collectors.toMap(
                                Area::getCode,
                                Function.identity()
                        )
                );
    }

    public Area resolve(
            String address,
            String roadAddress,
            Map<String, Area> activeAreasByCode
    ) {
        String addressText = combineAddresses(
                address,
                roadAddress
        );

        if (addressText.isBlank()) {
            return null;
        }

        for (AreaRule rule : AREA_RULES) {
            if (containsAnyKeyword(
                    addressText,
                    rule.addressKeywords()
            )) {
                return activeAreasByCode.get(rule.areaCode());
            }
        }

        return null;
    }

    private String combineAddresses(
            String address,
            String roadAddress
    ) {
        String normalizedAddress =
                address == null ? "" : address.trim();

        String normalizedRoadAddress =
                roadAddress == null
                        ? ""
                        : roadAddress.trim();

        return normalizedAddress
                + " "
                + normalizedRoadAddress;
    }

    private boolean containsAnyKeyword(
            String address,
            List<String> keywords
    ) {
        return keywords.stream()
                .anyMatch(address::contains);
    }

    private record AreaRule(
            String areaCode,
            List<String> addressKeywords
    ) {
    }

}
