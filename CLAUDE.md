# Todait 도메인 및 엔티티 설명서

이 문서는 현재 프로젝트 기준으로 Spring Boot 백엔드에서 도메인과 엔티티를 어떻게 나누면 좋은지 정리한 문서이다.

# 1. 추천 패키지 구조

```text
com.todait
├── domain
│   ├── member
│   ├── taxonomy
│   ├── place
│   ├── placeops
│   ├── course
│   └── recommendation
├── global
│   ├── config
│   ├── exception
│   ├── security
│   └── common
└── infra
    ├── oauth
    ├── kakao
    ├── naver
    └── storage
```

# 1-1. 확정된 실제 파일 구조 (담당자 포함)

아래는 팀 논의를 거쳐 실제로 적용하기로 확정한 패키지/파일 구조이다. 담당자와 함께 정리한다.

```text
src/main/java/com/example/TODAIT__BE
├── TodaitBeApplication.java
│
├── domain
│   ├── member                                      # 담당: 올리, 고슴이
│   │   ├── controller
│   │   │   ├── AuthController.java                 # 담당: 올리 | 이메일 로그인, 일반 회원가입, 토큰 재발급, 로그아웃
│   │   │   ├── OAuthController.java                # 담당: 올리 | 카카오/구글 로그인, OAuth callback
│   │   │   ├── EmailVerificationController.java    # 담당: 고슴이 | 이메일 인증번호 발송/확인
│   │   │   ├── MemberController.java               # 담당: 고슴이 | 내 정보 조회, 닉네임 중복 확인
│   │   │   ├── OnboardingController.java           # 담당: 올리 | 소셜 회원가입 완료
│   │   │   ├── TermController.java                 # 담당: 고슴이 | 약관 목록/상세 조회
│   │   │   ├── NoticeController.java               # 담당: 고슴이 | 공지사항 목록/상세 조회
│   │   │   └── NotificationSettingController.java  # 담당: 고슴이 | 알림 설정 조회/수정
│   │   │
│   │   ├── service
│   │   │   ├── AuthService.java                    # 담당: 올리 | 이메일 로그인, 회원가입, 토큰 발급/재발급
│   │   │   ├── OAuthService.java                   # 담당: 올리 | 카카오/구글 OAuth 처리
│   │   │   ├── EmailVerificationService.java       # 담당: 고슴이 | 이메일 인증번호 발송/검증
│   │   │   ├── MemberService.java                  # 담당: 고슴이 | 회원 조회, 닉네임 검증
│   │   │   ├── OnboardingService.java              # 담당: 올리 | 소셜 온보딩 완료 처리
│   │   │   ├── TermService.java                    # 담당: 고슴이 | 약관 조회, 필수 약관 검증
│   │   │   ├── NoticeService.java                  # 담당: 고슴이 | 공지사항 조회
│   │   │   └── NotificationSettingService.java     # 담당: 고슴이 | 알림 설정 조회/수정
│   │   │
│   │   ├── dto
│   │   │   ├── request                             # 담당: 각 API 담당자
│   │   │   └── response                            # 담당: 각 API 담당자
│   │   │
│   │   ├── entity                                  # 담당: 올리 중심, 고슴이 보조
│   │   │   ├── Member.java
│   │   │   ├── MemberOAuthAccount.java
│   │   │   ├── RefreshToken.java
│   │   │   ├── Term.java
│   │   │   ├── MemberTermAgreement.java
│   │   │   ├── NotificationSetting.java
│   │   │   └── Notice.java
│   │   │
│   │   ├── repository
│   │   │   ├── MemberRepository.java               # 담당: 올리/고슴이
│   │   │   ├── MemberOAuthAccountRepository.java   # 담당: 올리
│   │   │   ├── RefreshTokenRepository.java         # 담당: 올리
│   │   │   ├── TermRepository.java                 # 담당: 고슴이
│   │   │   ├── MemberTermAgreementRepository.java  # 담당: 올리/고슴이
│   │   │   ├── NotificationSettingRepository.java  # 담당: 고슴이
│   │   │   └── NoticeRepository.java               # 담당: 고슴이
│   │   │
│   │   └── enums                                   # 담당: 올리 중심
│   │       ├── MemberStatus.java
│   │       ├── MemberRole.java
│   │       ├── OAuthProvider.java
│   │       └── TermType.java
│   │
│   ├── taxonomy                                    # 담당: 죠, 믹키
│   │   ├── controller
│   │   │   ├── AreaController.java                 # 담당: 죠 | 지원 지역 조회
│   │   │   ├── PlaceCategoryController.java        # 담당: 죠 | 장소 카테고리 조회
│   │   │   ├── MoodTagController.java              # 담당: 믹키 | 분위기 태그 조회
│   │   │   └── FoodCategoryController.java         # 담당: 믹키 | 음식 카테고리 조회
│   │   │
│   │   ├── service
│   │   │   ├── AreaService.java                    # 담당: 죠
│   │   │   ├── PlaceCategoryService.java           # 담당: 죠
│   │   │   ├── MoodTagService.java                 # 담당: 믹키
│   │   │   └── FoodCategoryService.java            # 담당: 믹키
│   │   │
│   │   ├── dto
│   │   │   └── response                            # 담당: 죠/믹키
│   │   │
│   │   ├── entity                                  # 담당: 죠/믹키
│   │   │   ├── Area.java
│   │   │   ├── PlaceCategory.java
│   │   │   ├── MoodTag.java
│   │   │   └── FoodCategory.java
│   │   │
│   │   └── repository
│   │       ├── AreaRepository.java                 # 담당: 죠
│   │       ├── PlaceCategoryRepository.java        # 담당: 죠
│   │       ├── MoodTagRepository.java              # 담당: 믹키
│   │       └── FoodCategoryRepository.java         # 담당: 믹키
│   │
│   ├── place                                       # 담당: 죠
│   │   ├── controller
│   │   │   ├── PlaceController.java                # 담당: 죠 | 장소명 검색, 장소 상세 조회
│   │   │   └── RecommendedPlaceController.java     # 담당: 죠 | 추천 장소 목록/상세 조회
│   │   │
│   │   ├── service
│   │   │   ├── PlaceService.java                   # 담당: 죠 | 장소 검색, 장소 상세 조회
│   │   │   └── RecommendedPlaceService.java        # 담당: 죠 | 기준 장소/카테고리 기반 추천 장소 조회
│   │   │
│   │   ├── dto
│   │   │   ├── request                             # 담당: 죠
│   │   │   └── response                            # 담당: 죠
│   │   │
│   │   ├── entity                                  # 담당: 죠
│   │   │   ├── Place.java
│   │   │   ├── PlaceImage.java
│   │   │   ├── PlaceMoodTag.java
│   │   │   ├── PlaceFoodCategory.java
│   │   │   └── PlaceSource.java
│   │   │
│   │   ├── repository
│   │   │   ├── PlaceRepository.java                # 담당: 죠
│   │   │   ├── PlaceImageRepository.java           # 담당: 죠
│   │   │   ├── PlaceMoodTagRepository.java         # 담당: 죠
│   │   │   ├── PlaceFoodCategoryRepository.java    # 담당: 죠
│   │   │   └── PlaceSourceRepository.java          # 담당: 죠
│   │   │
│   │   └── enums                                   # 담당: 죠
│   │       ├── PlaceReviewStatus.java
│   │       └── PlaceExposureStatus.java
│   │
│   ├── course                                      # 담당: 멍이, 믹키
│   │   ├── controller
│   │   │   ├── CourseDraftController.java          # 담당: 멍이 | 임시 코스 생성/조회/포기, 취향 저장, 기준 장소 설정
│   │   │   ├── CourseDraftPlaceController.java     # 담당: 멍이 | 선택 장소 목록/추가/삭제/순서 변경
│   │   │   ├── CourseSaveController.java           # 담당: 멍이 | 임시 코스 최종 저장
│   │   │   ├── SavedCourseController.java          # 담당: 믹키 | 저장 코스 목록/상세/수정/삭제
│   │   │   └── RecommendedCourseController.java    # 담당: 믹키 | 추천 코스 목록/상세/저장
│   │   │
│   │   ├── service
│   │   │   ├── CourseDraftService.java             # 담당: 멍이 | 임시 코스 진행 상태 관리
│   │   │   ├── CourseDraftPlaceService.java        # 담당: 멍이 | 장소 추가/삭제/순서 변경
│   │   │   ├── CourseSaveService.java              # 담당: 멍이 | draft → course 저장
│   │   │   ├── SavedCourseService.java             # 담당: 믹키 | 저장 코스 조회/수정/삭제
│   │   │   └── RecommendedCourseService.java       # 담당: 믹키 | 추천 코스 조회/내 코스로 저장
│   │   │
│   │   ├── dto
│   │   │   ├── request                             # 담당: 멍이/믹키
│   │   │   └── response                            # 담당: 멍이/믹키
│   │   │
│   │   ├── entity                                  # 담당: 멍이/믹키, 수정 시 공유 필수
│   │   │   ├── CourseDraft.java
│   │   │   ├── CourseDraftMoodTag.java
│   │   │   ├── CourseDraftFoodCategory.java
│   │   │   ├── CourseDraftPlace.java
│   │   │   ├── Course.java
│   │   │   ├── CoursePlace.java
│   │   │   ├── CourseMoodTag.java
│   │   │   └── CourseFoodCategory.java
│   │   │
│   │   ├── repository
│   │   │   ├── CourseDraftRepository.java              # 담당: 멍이
│   │   │   ├── CourseDraftMoodTagRepository.java       # 담당: 멍이
│   │   │   ├── CourseDraftFoodCategoryRepository.java  # 담당: 멍이
│   │   │   ├── CourseDraftPlaceRepository.java         # 담당: 멍이
│   │   │   ├── CourseRepository.java                   # 담당: 믹키, 멍이와 공유 주의
│   │   │   ├── CoursePlaceRepository.java              # 담당: 믹키, 멍이와 공유 주의
│   │   │   ├── CourseMoodTagRepository.java            # 담당: 믹키
│   │   │   └── CourseFoodCategoryRepository.java       # 담당: 믹키
│   │   │
│   │   └── enums                                       # 담당: 멍이/믹키
│   │       ├── CourseDraftStatus.java
│   │       ├── CourseVisibility.java
│   │       ├── CourseSourceType.java
│   │       └── PlaceRole.java
│   │
│   └── recommendation                              # 담당: 보류
│       └── README.md                               # FS데이 이후 추천 로그/결과/행동 로그 구현 예정
│
├── global                                          # 담당: 믹키 중심
│   ├── common
│   │   ├── BaseEntity.java                         # 담당: 믹키
│   │   ├── ApiResponse.java                        # 담당: 믹키
│   │   ├── PageResponse.java                       # 담당: 믹키
│   │   └── EmptyResponse.java                      # 담당: 믹키
│   │
│   ├── exception
│   │   ├── GlobalExceptionHandler.java             # 담당: 믹키
│   │   ├── BusinessException.java                  # 담당: 믹키
│   │   └── ErrorCode.java                          # 담당: 믹키
│   │
│   ├── security
│   │   ├── JwtTokenProvider.java                   # 담당: 올리
│   │   ├── JwtAuthenticationFilter.java            # 담당: 올리
│   │   ├── SecurityConfig.java                     # 담당: 올리/믹키 협의
│   │   └── AuthMember.java                         # 담당: 올리/믹키 협의
│   │
│   ├── config
│   │   ├── JpaAuditingConfig.java                  # 담당: 믹키
│   │   ├── RedisConfig.java                        # 담당: 고슴이/믹키 협의
│   │   ├── WebConfig.java                          # 담당: 믹키 | CORS 설정
│   │   └── SwaggerConfig.java                      # 담당: 믹키
│   │
│   └── util
│       ├── DistanceCalculator.java                 # 담당: 죠
│       └── RandomCodeGenerator.java                # 담당: 고슴이
│
└── infra                                           # 외부 연동 영역
    ├── mail                                        # 담당: 고슴이
    │   ├── MailSender.java                         # 담당: 고슴이
    │   └── EmailVerificationMailService.java       # 담당: 고슴이
    │
    ├── redis                                       # 담당: 고슴이
    │   └── EmailVerificationRedisRepository.java   # 담당: 고슴이
    │
    └── oauth                                       # 담당: 올리
        ├── KakaoOAuthClient.java                   # 담당: 올리
        └── GoogleOAuthClient.java                  # 담당: 올리
```

# 2. 도메인별 테이블 매핑

| 도메인           | 책임                                                    | 테이블                                                                                                                                                                           |
| ---------------- | ------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `member`         | 회원, 소셜 로그인, 토큰, 약관 동의, 알림 설정, 공지사항 | `member`, `member_oauth_account`, `refresh_token`, `term`, `member_term_agreement`, `notification_setting`, `notice`                                                              |
| `taxonomy`       | 서비스 기준 지역, 카테고리, 태그 분류 체계             | `area`, `place_category`, `food_category`, `mood_tag`                                                                                                                            |
| `place`          | 앱에서 실제 사용하는 장소 마스터                        | `place`, `place_source`, `place_mood_tag`, `place_food_category`, `place_image`                                                                                                  |
| `placeops`       | 장소 수집, 정제, 검수, 운영 규칙                        | `data_source`, `collection_job`, `raw_place`, `category_mapping_rule`, `mood_keyword_rule`, `place_review_history`                                                               |
| `course`         | 코스 생성 임시 세션, 저장 코스, 공유                    | `course_draft`, `course_draft_mood_tag`, `course_draft_food_category`, `course_draft_place`, `course`, `course_place`, `course_mood_tag`, `course_food_category`, `course_share` |
| `recommendation` | 추천 요청/결과, 사용자 행동 로그, 추천용 회원 선호 요약 | `recommendation_log`, `recommendation_result`, `place_action_log`, `course_action_log`, `member_mood_preference`, `member_food_preference`                                       |

# 3. Member 도메인

회원 계정, 소셜 로그인, 이메일 회원가입, 약관 동의, 인증 세션, 마이페이지 설정, 공지사항을 담당한다.

## `Member`

테이블: `member`

회원 도메인의 루트 엔티티이다.

주요 컬럼:

- `email`: 회원 이메일
- `password_hash`: 일반 이메일 회원가입 비밀번호 해시. 소셜 전용 회원은 `NULL` 가능
- `nickname`: 닉네임
- `profile_image_url`: 프로필 이미지
- `role`: `USER`, `ADMIN`
- `status`: `ACTIVE`, `BLOCKED`, `DELETED`
- `last_login_at`: 마지막 로그인 시각
- `deleted_at`: 탈퇴/소프트 삭제 시각

주요 책임:

- 회원 기본 정보 관리
- 회원 상태 관리
- 관리자 권한 구분
- 탈퇴 시 소프트 삭제 처리

주요 관계:

- `member` 1:N `member_oauth_account`
- `member` 1:N `refresh_token`
- `member` 1:N `member_term_agreement`
- `member` 1:1 `notification_setting`
- `member` 1:N `course`
- `member` 1:N `course_draft`

주의:

- 이메일 인증번호는 Redis에서 만료 시간과 함께 관리하고, 현재 프로젝트 기준으로 별도 DB 테이블을 두지 않는다.
- `password_hash`는 이메일 회원가입 사용자에게만 필수이다. 소셜 로그인만 사용하는 회원은 `member_oauth_account`로 인증 수단을 식별한다.

## `MemberOAuthAccount`

테이블: `member_oauth_account`

소셜 로그인 연동 계정이다.

주요 책임:

- 카카오/구글 계정 연동 관리
- 소셜 로그인 시 기존 회원 식별
- 중복 계정 연동 판단

논리 unique:

- `provider + provider_user_id`

주요 컬럼:

- `member_id`: 소셜 계정이 연결된 회원
- `provider`: 소셜 제공자. `KAKAO`, `GOOGLE`
- `provider_user_id`: 소셜 제공자가 보장하는 사용자 고유 ID. 로그인 식별의 핵심 값
- `provider_email`: 소셜 제공자가 내려준 이메일 원본. 제공되지 않을 수 있다
- `linked_at`: 소셜 계정 연결 시각

## `RefreshToken`

테이블: `refresh_token`

로그인 세션 유지를 위한 토큰 엔티티이다.

주요 책임:

- 리프레시 토큰 저장
- 토큰 만료 관리
- 로그아웃 시 토큰 폐기

주요 컬럼:

- `member_id`: 토큰 소유 회원
- `token_hash`: 리프레시 토큰 원문이 아니라 해시값
- `expires_at`: 토큰 만료 시각
- `revoked_at`: 로그아웃 또는 보안 조치로 폐기된 시각

## `Term`

테이블: `term`

서비스 이용약관, 개인정보 처리방침, 마케팅 수신 동의 같은 약관의 버전별 원본이다.

주요 책임:

- 약관 종류 관리
- 약관 제목/본문/버전 관리
- 필수 약관과 선택 약관 구분
- 현재 사용 중인 약관 여부 관리

주요 타입:

- `SERVICE`
- `PRIVACY`
- `MARKETING`

논리 unique:

- `term_type + version`

주요 컬럼:

- `term_type`: 약관 종류. `SERVICE`, `PRIVACY`, `MARKETING`
- `title`: 약관 제목
- `content`: 약관 본문
- `version`: 약관 버전
- `is_required`: 필수 동의 여부
- `is_active`: 현재 회원가입/온보딩에서 사용하는 약관 여부

주의:

- 프론트엔드에서도 필수 약관 미동의 시 다음 단계로 넘어가지 못하게 막아야 하지만, 백엔드는 반드시 `term.is_required = true`이고 `term.is_active = true`인 약관이 모두 동의되었는지 다시 검증해야 한다.
- 백엔드 검증을 하지 않으면 클라이언트 우회 요청, 오래된 앱 버전, API 직접 호출에서 필수 약관 없이 회원가입 또는 온보딩이 완료될 수 있다.
- 필수 약관 미동의 요청은 `400 Bad Request` 또는 프로젝트 공통 예외 코드로 거절한다.

## `MemberTermAgreement`

테이블: `member_term_agreement`

회원이 어떤 약관의 어떤 버전에 동의했는지 저장하는 이력 엔티티이다.

주요 책임:

- 회원별 약관 동의 여부 저장
- 필수 약관 동의 검증 근거 제공
- 선택 약관 철회 시각 관리
- 약관 버전 변경 시 재동의 여부 판단

주요 관계:

- `member_term_agreement` N:1 `member`
- `member_term_agreement` N:1 `term`

논리 unique:

- `member_id + term_id`

주요 컬럼:

- `member_id`: 약관에 동의한 회원
- `term_id`: 동의 대상 약관
- `agreed`: 동의 여부
- `agreed_at`: 동의 시각
- `withdrawn_at`: 선택 약관 철회 시각

## `NotificationSetting`

테이블: `notification_setting`

회원별 알림 설정이다.

주요 책임:

- 푸시 알림 허용 여부 관리
- 마케팅 알림 허용 여부 관리
- 서비스 알림 허용 여부 관리

논리 unique:

- `member_id`

주요 컬럼:

- `member_id`: 알림 설정 대상 회원
- `push_enabled`: 푸시 알림 허용 여부
- `marketing_enabled`: 마케팅 알림 허용 여부
- `service_enabled`: 서비스 운영 알림 허용 여부

## `Notice`

테이블: `notice`

공지사항 또는 고객센터성 기본 콘텐츠이다. 현재 프로젝트 기준으로는 `member` 도메인에 포함한다.

주요 책임:

- 공지 제목/내용 관리
- 게시 상태 관리
- 게시 시각 관리
- 작성 관리자 연결

상태값:

- `DRAFT`
- `PUBLISHED`
- `HIDDEN`

주요 컬럼:

- `title`: 공지 제목
- `content`: 공지 내용
- `status`: 게시 상태. `DRAFT`, `PUBLISHED`, `HIDDEN`
- `published_at`: 게시 시각
- `created_by`: 작성 관리자 회원

# 4. Taxonomy 도메인

서비스 추천과 코스 생성에 필요한 기준 지역, 카테고리, 태그 분류 체계를 담당한다.

## `Area`

테이블: `area`

지원 지역 기준 데이터이다.

초기 코드:

- `HONGDAE`
- `YEONNAM`
- `SEONGSU`

주요 책임:

- 지원 지역 관리
- 지역 중심 좌표 관리
- 장소와 코스의 대표 지역 기준 제공

논리 unique:

- `code`

주요 컬럼:

- `code`: 지역 코드. `HONGDAE`, `YEONNAM`, `SEONGSU`
- `name`: 화면에 보여줄 지역명
- `description`: 지역 설명
- `center_latitude`, `center_longitude`: 지역 중심 좌표. 근처 장소 추천 기준으로 활용
- `is_active`: 현재 지원 지역 여부
- `sort_order`: 지역 선택 화면의 표시 순서

## `PlaceCategory`

테이블: `place_category`

장소 대분류 기준 데이터이다.

초기 코드:

- `CAFE`
- `RESTAURANT`
- `ACTIVITY`
- `BAR`

주요 책임:

- 코스 구성 화면의 카테고리 탭 기준
- 장소 추천 필터 기준
- 장소의 대분류 기준

논리 unique:

- `code`

주요 컬럼:

- `code`: 장소 대분류 코드. `CAFE`, `RESTAURANT`, `ACTIVITY`, `BAR`
- `name`: 화면에 보여줄 카테고리명
- `description`: 카테고리 설명
- `sort_order`: 카테고리 탭/선택 화면의 표시 순서. 추천 순위가 아니다
- `is_active`: 현재 사용하는 카테고리 여부

## `FoodCategory`

테이블: `food_category`

음식 취향 및 식당 분류 기준 데이터이다.

초기 코드:

- `KOREAN`
- `JAPANESE`
- `WESTERN`
- `CHINESE`
- `BUNSIK`
- `DESSERT`

주의:

- `DESSERT`는 음식 취향에는 포함되지만, 실제 추천에서는 `CAFE` 장소와 연결될 수 있다.

논리 unique:

- `code`

주요 컬럼:

- `code`: 음식 카테고리 코드. `KOREAN`, `JAPANESE`, `WESTERN`, `CHINESE`, `BUNSIK`, `DESSERT`
- `name`: 화면에 보여줄 음식 카테고리명
- `description`: 음식 카테고리 설명
- `sort_order`: 음식 선택 화면의 표시 순서. 추천 순위가 아니다
- `is_active`: 현재 사용하는 음식 카테고리 여부

## `MoodTag`

테이블: `mood_tag`

분위기 태그 기준 데이터이다.

초기 코드:

- `HIP`
- `QUIET`
- `ACTIVE`
- `ROMANTIC`
- `CALM`
- `MODERN`

주요 책임:

- 사용자 분위기 취향 선택
- 장소 분위기 태그 관리
- 코스 분위기 태그 저장
- 추천 매칭 기준

논리 unique:

- `code`

주요 컬럼:

- `code`: 분위기 태그 코드. `HIP`, `QUIET`, `ACTIVE`, `ROMANTIC`, `CALM`, `MODERN`
- `name`: 화면에 보여줄 분위기명
- `description`: 분위기 설명
- `sort_order`: 분위기 선택 화면의 표시 순서. 추천 순위가 아니다
- `is_active`: 현재 사용하는 분위기 태그 여부

# 5. Place 도메인

사용자 앱에서 검색, 추천, 코스 구성에 사용하는 최종 장소 데이터를 담당한다.

## `Place`

테이블: `place`

장소 도메인의 루트 엔티티이다.

주요 책임:

- 추천 가능한 최종 장소 데이터 관리
- 기준 장소 선택과 장소 검색의 기준 데이터
- 카테고리별 추천 후보 제공
- 운영자 검수 상태와 노출 상태 관리
- 선택/저장/조회 카운트 관리

추천 노출 조건:

- `area_id`가 지원 지역
- `place_category_id` 존재
- 장소명, 주소, 좌표 존재
- 분위기 태그 1개 이상 존재
- `is_active = true`
- `is_reviewed = true`
- `review_status = APPROVED`
- `exposure_status = ACTIVE`

주의:

- `popularity_score`는 내부 정렬용이며 화면에 노출하지 않는다.
- 외부 평점/별점은 MVP에서 사용하지 않는다.

주요 컬럼:

- `area_id`: 장소가 속한 지원 지역
- `place_category_id`: 카페/식당/액티비티/술 대분류
- `primary_food_category_id`: 대표 음식 카테고리. 식당 또는 디저트 장소에 사용
- `name`: 장소명
- `address`, `road_address`: 지번 주소와 도로명 주소
- `latitude`, `longitude`: 지도 핀 표시와 거리 계산에 사용하는 좌표
- `phone`: 전화번호. 중복 제거와 운영자 검수 보조값
- `sub_category`: 세부 카테고리. 예: 와인바, 방탈출, 브런치
- `default_image_url`: 대표 이미지 URL
- `default_recommend_reason`: 추천 이유를 만들기 어려울 때 사용할 기본 문구
- `operator_priority`: 운영자가 직접 부여하는 우선순위. 초기 추천 정렬 보정에 사용
- `review_status`: 검수 진행 상태. `BEFORE_REVIEW`, `REVIEWING`, `APPROVED`, `EXCLUDED`, `NEEDS_UPDATE`
- `exposure_status`: 사용자 앱 노출 상태. `ACTIVE`, `INACTIVE`, `CLOSED`, `UNSUITABLE`
- `is_active`: 앱 추천/검색에 사용할 수 있는지 빠르게 필터링하기 위한 값
- `is_reviewed`: 운영자 검수 완료 여부. `review_status`와 중복될 수 있으므로 구현 시 유지 여부를 결정한다
- `admin_memo`: 운영자 전용 메모. 사용자에게 노출하지 않는다
- `selected_count`: 코스 생성 중 장소가 선택된 횟수
- `saved_count`: 저장 완료된 코스에 포함된 횟수
- `viewed_count`: 장소 카드 또는 상세 조회 횟수
- `popularity_score`: 조회/선택/저장 횟수와 운영자 우선순위를 조합한 내부 정렬 점수

## `PlaceSource`

테이블: `place_source`

최종 장소와 원본 출처를 연결한다.

주요 책임:

- 최종 장소가 어떤 외부 데이터에서 왔는지 추적
- 카카오/공공데이터/네이버/운영자 등록 출처 관리
- 중복 제거와 검수 근거 제공

논리 unique:

- `place_id + data_source_id + source_place_id`

주요 컬럼:

- `place_id`: 최종 장소
- `raw_place_id`: 연결된 원본 장소. 운영자 수동 등록이면 `NULL` 가능
- `data_source_id`: 장소 출처
- `source_place_id`: 외부 출처의 장소 ID
- `source_url`: 외부 상세 URL. 운영자 검수 참고용
- `is_primary`: 여러 출처 중 대표 출처 여부

## `PlaceMoodTag`

테이블: `place_mood_tag`

장소와 분위기 태그의 N:M 매핑 엔티티이다.

주요 책임:

- 장소별 분위기 태그 저장
- 자동 규칙 초안과 운영자 확정 여부 관리
- 추천 시 사용자 선택 분위기와 매칭

논리 unique:

- `place_id + mood_tag_id`

주요 컬럼:

- `place_id`: 분위기 태그가 붙은 장소
- `mood_tag_id`: 장소에 부여된 분위기 태그
- `tag_source`: 태그 출처. `RULE`, `OPERATOR`, `USER_BEHAVIOR`
- `confidence_score`: 자동 규칙으로 붙인 태그의 신뢰도
- `is_confirmed`: 운영자가 최종 확인한 태그인지 여부

## `PlaceFoodCategory`

테이블: `place_food_category`

장소의 보조 음식 카테고리이다.

주요 책임:

- 복합 메뉴 장소의 보조 음식 카테고리 저장
- 디저트/브런치처럼 여러 음식 취향과 연결되는 장소 보완

주의:

- 대표 음식 카테고리는 `place.primary_food_category_id`로 관리한다.

논리 unique:

- `place_id + food_category_id`

주요 컬럼:

- `place_id`: 음식 카테고리가 연결된 장소
- `food_category_id`: 연결된 음식 카테고리
- `is_primary`: 대표 음식 카테고리 여부. `place.primary_food_category_id`와 맞춰 관리한다

## `PlaceImage`

테이블: `place_image`

장소 이미지 엔티티이다.

주요 책임:

- 운영자 등록 이미지 관리
- 카테고리 기본 이미지 관리
- 사용 허가 이미지 관리

주의:

- 외부 서비스 이미지를 무단 크롤링해서 사용하는 구조로 보지 않는다.

주요 컬럼:

- `place_id`: 이미지가 속한 장소
- `image_url`: 이미지 URL
- `image_type`: 이미지 종류. `OPERATOR`, `DEFAULT_CATEGORY`, `LICENSED`
- `source_name`: 이미지 출처명
- `display_order`: 이미지 목록의 표시 순서
- `is_primary`: 대표 이미지 여부

# 6. PlaceOps 도메인

장소 수집, 정제, 검수, 운영 규칙을 담당한다.

사용자 앱 런타임의 핵심 기능과 분리해서 관리하는 것이 좋다.

## `DataSource`

테이블: `data_source`

장소 데이터 출처 기준 데이터이다.

초기 코드:

- `KAKAO`
- `PUBLIC_DATA`
- `NAVER`
- `OPERATOR`

주요 컬럼:

- `code`: 출처 코드. `KAKAO`, `PUBLIC_DATA`, `NAVER`, `OPERATOR`
- `name`: 출처 표시명
- `usage_type`: 사용 목적. `COLLECTION`, `VALIDATION`, `MANUAL`
- `is_active`: 현재 사용하는 출처 여부

## `CollectionJob`

테이블: `collection_job`

장소 후보 수집 작업 단위이다.

주요 책임:

- 어떤 출처에서 어떤 지역/키워드로 수집했는지 기록
- 수집 성공/실패 상태 관리
- 요청 건수와 수집 건수 관리
- 실패 사유 기록

주요 컬럼:

- `data_source_id`: 수집에 사용한 출처
- `area_id`: 수집 대상 지역
- `keyword`: 검색 키워드. 예: 홍대 카페, 성수 전시
- `category_group_code`: 외부 API의 카테고리 그룹 코드
- `status`: 작업 상태. `READY`, `RUNNING`, `SUCCESS`, `FAILED`
- `requested_count`: 외부 API 요청 건수
- `collected_count`: 수집된 장소 수
- `failed_reason`: 실패 사유
- `started_at`, `finished_at`: 수집 시작/종료 시각

## `RawPlace`

테이블: `raw_place`

외부 API 또는 공공데이터에서 들어온 원본 장소이다.

주요 책임:

- 외부 장소 ID 저장
- 원본 장소명, 주소, 좌표, 카테고리 저장
- 원본 API 응답 JSON 보관
- 정제 전 데이터 보존

주의:

- 앱 추천 결과에 직접 사용하지 않는다.
- 운영자 검수와 정제를 거쳐 `place`로 반영한다.

논리 unique:

- `data_source_id + source_place_id`

주요 컬럼:

- `collection_job_id`: 어떤 수집 작업에서 들어온 원본인지
- `data_source_id`: 원본 데이터 출처
- `source_place_id`: 외부 출처의 장소 ID
- `name`: 원본 장소명
- `address`, `road_address`: 원본 주소
- `latitude`, `longitude`: 원본 좌표
- `phone`: 전화번호
- `raw_category`: 외부 원본 카테고리
- `source_url`: 외부 상세 URL
- `raw_payload`: 외부 API 응답 원본 JSON
- `collected_at`: 수집 시각

## `CategoryMappingRule`

테이블: `category_mapping_rule`

외부 카테고리를 투데잇 내부 카테고리로 변환하는 규칙이다.

예시:

- 카페, 디저트, 베이커리 → `CAFE`
- 한식, 일식, 양식 → `RESTAURANT`
- 와인바, 이자카야, 펍 → `BAR`

주요 컬럼:

- `data_source_id`: 특정 출처에만 적용할 규칙. `NULL`이면 공통 규칙
- `raw_category_pattern`: 외부 카테고리/키워드 패턴
- `place_category_id`: 매핑될 내부 장소 대분류
- `food_category_id`: 매핑될 음식 카테고리. 필요 없으면 `NULL`
- `sub_category`: 세부 카테고리
- `priority`: 규칙 우선순위
- `is_active`: 현재 사용하는 규칙 여부

## `MoodKeywordRule`

테이블: `mood_keyword_rule`

장소명, 원본 카테고리, 운영자 키워드로 분위기 태그 초안을 만드는 규칙이다.

예시:

- 루프탑, 와인바, 다이닝 → `ROMANTIC`, `MODERN`
- 방탈출, 보드게임 → `ACTIVE`
- 북카페, 티룸 → `QUIET`, `CALM`

주요 컬럼:

- `keyword`: 분위기 태그를 추론할 키워드
- `mood_tag_id`: 매핑될 분위기 태그
- `place_category_id`: 특정 장소 대분류에만 적용할 때 사용
- `food_category_id`: 특정 음식 카테고리에만 적용할 때 사용
- `priority`: 규칙 우선순위
- `is_active`: 현재 사용하는 규칙 여부

## `PlaceReviewHistory`

테이블: `place_review_history`

장소 운영자 검수 이력이다.

주요 책임:

- 검수 상태 변경 기록
- 검수자 기록
- 검수 메모 기록
- 장소 데이터 변경 추적성 확보

주요 컬럼:

- `place_id`: 검수 대상 장소
- `reviewer_id`: 검수자 회원. 보통 관리자
- `previous_status`: 변경 전 검수 상태
- `next_status`: 변경 후 검수 상태
- `memo`: 검수 사유나 보완 메모
- `reviewed_at`: 검수 시각

# 7. Course 도메인

코스 생성 중 임시 상태와 저장 완료된 코스를 담당한다.

## `CourseDraft`

테이블: `course_draft`

사용자가 코스를 만드는 중간 상태의 루트 엔티티이다.

주요 책임:

- 분위기 선택 상태 관리
- 음식 선택 상태 관리
- 기준 장소 선택
- 장소 추가/삭제
- 방문 순서 수정
- 저장 전 임시 세션 관리

상태값:

- `MOOD_SELECTING`
- `FOOD_SELECTING`
- `BASE_PLACE_SELECTING`
- `PLACE_SELECTING`
- `ORDERING`
- `SAVING`
- `COMPLETED`
- `ABANDONED`

주요 컬럼:

- `member_id`: 코스를 작성 중인 회원
- `base_place_id`: 기준 장소
- `status`: 코스 생성 진행 상태
- `user_latitude`, `user_longitude`: 위치 권한 허용 시 사용자 위치
- `expires_at`: 임시 코스 세션 만료 시각

## `CourseDraftMoodTag`

테이블: `course_draft_mood_tag`

임시 코스에서 선택한 분위기 태그이다.

논리 unique:

- `course_draft_id + mood_tag_id`

주요 컬럼:

- `course_draft_id`: 임시 코스
- `mood_tag_id`: 사용자가 선택한 분위기 태그

## `CourseDraftFoodCategory`

테이블: `course_draft_food_category`

임시 코스에서 선택한 음식 카테고리이다.

논리 unique:

- `course_draft_id + food_category_id`

주요 컬럼:

- `course_draft_id`: 임시 코스
- `food_category_id`: 사용자가 선택한 음식 카테고리

## `CourseDraftPlace`

테이블: `course_draft_place`

임시 코스에 담긴 기준 장소와 추가 장소이다.

주요 책임:

- 기준 장소와 선택 장소 구분
- 방문 순서 관리
- 장소별 임시 메모 관리
- 드래그 앤 드롭 순서 변경 반영

`place_role`:

- `BASE`
- `SELECTED`

논리 unique:

- `course_draft_id + place_id`
- `course_draft_id + visit_order`

주요 컬럼:

- `course_draft_id`: 임시 코스
- `place_id`: 임시 코스에 담긴 장소
- `visit_order`: 방문 순서
- `place_role`: 기준 장소인지 선택 장소인지 구분. `BASE`, `SELECTED`
- `memo`: 저장 전 장소별 임시 메모

## `Course`

테이블: `course`

저장 완료된 코스의 루트 엔티티이다.

주요 책임:

- 코스명과 메모 저장
- 기준 장소와 대표 지역 저장
- 공개 범위 관리
- 서비스 생성/추천 코스 구분
- 조회/저장/재사용 횟수 관리

`visibility`:

- `PRIVATE`
- `PUBLIC`
- `RECOMMENDED`

`source_type`:

- `USER_CREATED`
- `SERVICE_CREATED`

의미:

- `USER_CREATED`: 사용자가 직접 만든 코스
- `SERVICE_CREATED`: 서비스가 초기 추천 또는 기본 추천으로 제공하기 위해 준비한 코스. PM 명세의 "운영자 추천 코스" 표현은 DB enum에서는 `SERVICE_CREATED`로 해석한다.

주요 컬럼:

- `member_id`: 코스 소유 회원
- `base_place_id`: 코스의 기준 장소
- `area_id`: 대표 지역
- `title`: 코스명
- `memo`: 코스 전체 메모
- `visibility`: 공개 범위. `PRIVATE`, `PUBLIC`, `RECOMMENDED`
- `source_type`: 코스 생성 출처. `USER_CREATED`, `SERVICE_CREATED`
- `representative_image_url`: 코스 대표 이미지
- `place_count`: 코스에 포함된 장소 수
- `view_count`: 코스 상세 조회 수
- `save_count`: 저장 또는 불러오기 수
- `reuse_count`: 재사용 수
- `operator_priority`: 서비스 추천 코스 우선순위

## `CoursePlace`

테이블: `course_place`

저장된 코스의 방문 장소 목록이다.

주요 책임:

- 방문 순서 저장
- 기준 장소/선택 장소 구분
- 장소별 사용자 메모 저장
- 저장 당시 장소명/주소/좌표/카테고리 스냅샷 저장

스냅샷을 저장하는 이유:

- 나중에 `place`가 삭제되거나 비노출 처리되어도 저장된 코스 상세가 깨지지 않게 하기 위해서이다.

논리 unique:

- `course_id + visit_order`

주요 컬럼:

- `course_id`: 장소가 포함된 코스
- `place_id`: 현재 장소 참조. 장소가 삭제/비노출될 수 있어 `NULL` 가능
- `visit_order`: 방문 순서
- `place_role`: 기준 장소인지 선택 장소인지 구분. `BASE`, `SELECTED`
- `place_name_snapshot`: 저장 당시 장소명
- `address_snapshot`: 저장 당시 주소
- `latitude_snapshot`, `longitude_snapshot`: 저장 당시 좌표
- `category_snapshot`: 저장 당시 카테고리 코드
- `memo`: 장소별 사용자 메모

## `CourseMoodTag`

테이블: `course_mood_tag`

저장된 코스의 분위기 태그이다.

논리 unique:

- `course_id + mood_tag_id`

주요 컬럼:

- `course_id`: 코스
- `mood_tag_id`: 코스에 저장된 분위기 태그

## `CourseFoodCategory`

테이블: `course_food_category`

저장된 코스의 음식 태그이다.

논리 unique:

- `course_id + food_category_id`

주요 컬럼:

- `course_id`: 코스
- `food_category_id`: 코스에 저장된 음식 카테고리

## `CourseShare`

테이블: `course_share`

사용자가 만든 코스를 다른 사용자가 볼 수 있도록 공유하는 엔티티이다.

현재 프로젝트에서 `course_share`는 "외부 카카오톡 공유 전용"이라기보다, 음악 앱의 공개 플레이리스트처럼 사용자가 만든 코스를 공유 가능한 링크 또는 공개 진입점으로 노출하기 위한 확장 구조로 본다. 커뮤니티 화면이 없더라도 공유 링크나 추천 코스 영역에서 사용자 생성 코스를 보여줄 수 있다.

주요 책임:

- 공유 토큰 발급
- 공유 만료 관리
- 공유 사용 횟수 관리
- 공유 링크를 통해 접근 가능한 코스 식별

논리 unique:

- `share_token`

주요 컬럼:

- `course_id`: 공유 대상 코스
- `owner_member_id`: 공유 링크를 만든 회원
- `share_token`: 공유 링크 식별 토큰
- `provider`: 공유 방식. `KAKAO`, `LINK`
- `expires_at`: 공유 만료 시각
- `used_count`: 공유 사용 횟수

# 8. Recommendation 도메인

추천 후보 조회, 추천 결과 기록, 사용자 행동 로그를 담당한다.

이 도메인은 "사용자가 추천해주세요 버튼을 눌렀을 때만" 사용하는 도메인이 아니다. 홈 화면 진입 시 자동으로 추천 목록을 구성하거나, 코스 생성 화면에서 기준 장소와 취향을 바탕으로 장소 목록을 자동으로 보여주는 것도 서버 입장에서는 추천 요청 1회로 볼 수 있다.

초기 MVP에서는 추천 알고리즘을 복잡하게 만들 필요가 없다. `place`와 `course`를 조건에 맞게 조회하고 정렬하는 서비스 로직이 먼저이고, `recommendation_log`와 `recommendation_result`는 그때 어떤 조건으로 어떤 결과가 내려갔는지 남기는 선택적 기록 테이블이다.

PM 기능 명세 기준으로 recommendation 도메인이 연결되는 화면은 다음과 같다.

- 홈 화면: 추천 코스 목록, 추천 장소 영역을 만든다.
- 기준 장소 설정 화면: 검색 전 상태의 기본 핫플 목록을 만든다.
- 코스 구성 화면: 기준 장소, 분위기, 음식 카테고리, 선택 탭을 기준으로 장소 후보를 만든다.
- 저장된 코스/장소 행동 데이터: 운영 이후 추천 품질 개선을 위한 행동 로그를 쌓는다.

구현 기준:

- 추천 목록을 "계산하는 서비스"와 추천 이력을 "저장하는 테이블"을 분리한다.
- MVP에서는 `place`, `course`, `course_place`, `place_mood_tag`, `place_food_category`를 조회해서 조건 필터링과 정렬을 먼저 구현한다.
- `recommendation_log`와 `recommendation_result` 저장은 분석 가치가 큰 흐름부터 적용한다. 모든 조회 API가 반드시 이력을 남겨야 하는 것은 아니다.
- `internal_score`, `popularity_score` 같은 내부 점수는 응답으로 내려주지 않거나, 내려주더라도 화면 표시 대상에서 제외한다.
- 추천 결과 없음과 API 오류는 구분한다. 결과 없음은 빈 배열과 빈 상태 문구, API 오류는 공통 오류 응답으로 처리한다.

## `MemberMoodPreference`

테이블: `member_mood_preference`

회원의 분위기 선호 요약이다.

주요 책임:

- 코스 생성, 코스 저장, 장소 선택 이력에서 추론한 회원별 분위기 선호 요약
- 홈 취향 기반 추천과 추천 정렬 보정에 사용
- MVP의 코스 생성 과정에서 사용자가 매번 선택하는 "오늘의 분위기"는 `course_draft_mood_tag`에 저장한다
- 사용자가 직접 장기 취향을 설정하는 기능은 현재 MVP 요구사항에는 없으며, 필요해지면 `MANUAL` 출처로 확장한다

논리 unique:

- `member_id + mood_tag_id`

주요 컬럼:

- `member_id`: 선호 요약 대상 회원
- `mood_tag_id`: 회원에게 선호 신호가 있다고 판단한 분위기 태그
- `preference_source`: 선호 요약이 만들어진 출처. `COURSE_HISTORY`, `SAVE_HISTORY`, `PLACE_ACTION`, `MANUAL`

구현 메모:

- 이 테이블은 사용자가 코스 생성 중 선택한 "오늘의 분위기"를 그대로 저장하는 테이블이 아니다. 오늘 선택값은 `course_draft_mood_tag`에 저장한다.
- 이 테이블은 여러 행동 데이터를 집계한 장기 취향 요약에 가깝다.
- MVP에서는 직접 갱신하지 않고 비워 두어도 된다. 홈 개인화 추천을 고도화할 때 배치 작업이나 이벤트 처리로 갱신한다.
- 예를 들어 사용자가 `ROMANTIC` 분위기 코스를 자주 저장하거나, `ROMANTIC` 태그 장소를 자주 선택하면 해당 회원의 분위기 선호로 요약할 수 있다.

## `MemberFoodPreference`

테이블: `member_food_preference`

회원의 음식 선호 요약이다.

주요 책임:

- 코스 생성, 코스 저장, 장소 선택 이력에서 추론한 회원별 음식 선호 요약
- 홈 취향 기반 추천과 식당 추천 정렬 보정에 사용
- MVP의 코스 생성 과정에서 사용자가 매번 선택하는 "오늘의 음식"은 `course_draft_food_category`에 저장한다
- 사용자가 직접 장기 취향을 설정하는 기능은 현재 MVP 요구사항에는 없으며, 필요해지면 `MANUAL` 출처로 확장한다

논리 unique:

- `member_id + food_category_id`

주요 컬럼:

- `member_id`: 선호 요약 대상 회원
- `food_category_id`: 회원에게 선호 신호가 있다고 판단한 음식 카테고리
- `preference_source`: 선호 요약이 만들어진 출처. `COURSE_HISTORY`, `SAVE_HISTORY`, `PLACE_ACTION`, `MANUAL`

구현 메모:

- 이 테이블은 사용자가 코스 생성 중 선택한 "오늘의 음식"을 그대로 저장하는 테이블이 아니다. 오늘 선택값은 `course_draft_food_category`에 저장한다.
- 이 테이블은 장기 음식 취향 요약이다.
- MVP에서는 직접 갱신하지 않고 비워 두어도 된다. 저장 코스, 선택 장소, 장소 행동 로그가 쌓인 뒤 추천 고도화 단계에서 갱신한다.
- `DESSERT`는 음식 취향이지만 실제 장소 대분류는 `CAFE`와 연결될 수 있으므로 추천 로직에서 `food_category_id = DESSERT`와 `place_category.code = CAFE`의 관계를 고려한다.

## `RecommendationLog`

테이블: `recommendation_log`

추천 목록을 만든 1회의 조건을 저장한다.

여기서 "요청"은 사용자가 명시적으로 버튼을 누른 경우뿐 아니라 홈 화면 진입, 내 주변 핫플 조회, 기준 장소 설정 후 카테고리별 장소 목록 조회처럼 서버가 추천 목록을 계산한 모든 순간을 포함한다.

추천 타입:

- `HOME_POPULAR_COURSE`
- `HOME_PLACE`
- `HOT_PLACE`
- `NEAR_BASE_PLACE`

주요 컬럼:

- `member_id`: 추천을 요청한 회원. 공통 추천이나 비회원 가능성을 고려해 `NULL` 가능
- `course_draft_id`: 코스 생성 중 발생한 추천이면 연결되는 임시 코스
- `base_place_id`: 기준 장소
- `area_id`: 추천 기준 지역
- `recommendation_type`: 추천 요청 종류. 홈 인기 코스, 홈 장소, 내 주변 핫플, 기준 장소 주변 추천 등
- `place_category_id`: 추천 탭 카테고리
- `user_latitude`, `user_longitude`: 요청 당시 사용자 위치
- `request_context`: 분위기/음식/제외 장소 등 요청 조건 JSON

왜 저장하는가:

- 추천 결과가 이상하다는 피드백이 왔을 때 당시 조건을 재현하기 위해서이다.
- 어떤 추천 타입이 자주 호출되는지, 어떤 조건에서 결과가 부족한지 분석하기 위해서이다.
- 운영 이후 추천 알고리즘을 개선할 때 실제 요청 조건 데이터를 참고하기 위해서이다.
- MVP에서는 모든 추천에 반드시 저장하지 않아도 되며, 홈 추천과 코스 생성 추천처럼 분석 가치가 큰 흐름부터 저장해도 된다.

`request_context` 예시:

```json
{
  "moodTagIds": [1, 4],
  "foodCategoryIds": [2],
  "excludedPlaceIds": [10, 15],
  "selectedPlaceCategoryCode": "CAFE",
  "limit": 20,
  "sortPolicy": "DISTANCE_THEN_MOOD_THEN_OPERATOR_PRIORITY"
}
```

추천 타입별 의미:

- `HOME_POPULAR_COURSE`: 홈 화면 추천 코스 영역을 만들 때 사용한다. 초기에는 `course.visibility = RECOMMENDED` 또는 `course.source_type = SERVICE_CREATED` 코스를 우선 조회한다.
- `HOME_PLACE`: 홈 화면 추천 장소 영역을 만들 때 사용한다. 사용자 이력이 있으면 선호 요약을 참고하고, 없으면 기본 추천 장소를 조회한다.
- `HOT_PLACE`: 기준 장소를 고르기 전 검색 전 상태 또는 내 주변 핫플 영역에 사용할 수 있다.
- `NEAR_BASE_PLACE`: 기준 장소 설정 후 코스 구성 화면에서 카테고리별 주변 장소를 추천할 때 사용한다.

저장하지 않아도 되는 경우:

- 단순 taxonomy 목록 조회처럼 추천 계산이 없는 API
- 장소 상세 조회처럼 이미 특정 `placeId`가 정해진 API
- 프론트의 로컬 정렬, 탭 전환만으로 서버 추천 결과가 새로 계산되지 않는 경우

## `RecommendationResult`

테이블: `recommendation_result`

추천 목록을 만든 결과 스냅샷이다.

주요 책임:

- 추천 순위 저장
- 추천된 장소 또는 코스 저장
- 추천 이유 문구 저장
- 거리, 매칭 개수, 내부 점수 저장

주의:

- `internal_score`는 화면에 노출하지 않는다.
- 이 테이블은 사용자가 실제로 저장하거나 선택한 데이터가 아니라, "그 시점에 서버가 몇 번째로 어떤 장소/코스를 보여줬는지"를 기록하는 용도이다.

논리 unique:

- `recommendation_log_id + rank_no`

주요 컬럼:

- `recommendation_log_id`: 어떤 추천 요청의 결과인지
- `place_id`: 추천 장소. 장소 추천일 때 사용
- `course_id`: 추천 코스. 코스 추천일 때 사용
- `rank_no`: 추천 결과 순위
- `reason_text`: 사용자에게 보여줄 추천 이유 문구
- `distance_meters`: 기준 장소 또는 사용자 위치와의 거리
- `matched_mood_count`: 일치한 분위기 태그 수
- `matched_food_count`: 일치한 음식 카테고리 수
- `internal_score`: 내부 정렬 점수. 화면에 노출하지 않는다

주의:

- `place_id`와 `course_id`는 둘 중 정확히 하나만 가져야 한다.
- 이 규칙은 `RecommendationResult.forPlace(...)`, `RecommendationResult.forCourse(...)` 같은 정적 생성 메서드와 서비스 검증으로 막는 것을 권장한다.
- DB 개념 DDL에 CHECK 제약이 없더라도 Spring 서비스/도메인 생성 메서드에서 반드시 검증한다.

구현 메모:

- `recommendation_log`가 "어떤 조건으로 추천했는가"라면, `recommendation_result`는 "그 조건으로 무엇을 몇 번째에 보여줬는가"이다.
- 사용자가 실제로 클릭했는지, 저장했는지, 선택했는지는 이 테이블이 아니라 `place_action_log`, `course_action_log`에 남긴다.
- 같은 추천 요청에서 장소 20개를 내려줬다면 `recommendation_result`는 최대 20행이 생길 수 있다.
- `rank_no`는 화면에 내려간 순서이다. 사용자가 정렬을 바꾸거나 새 조건으로 다시 조회하면 새로운 `recommendation_log`를 만드는 편이 추적하기 쉽다.
- MVP에서 저장량이 부담되면 상위 N개만 저장하거나, `HOME_PLACE`, `NEAR_BASE_PLACE`처럼 중요한 추천 타입만 저장한다.

## `PlaceActionLog`

테이블: `place_action_log`

장소에 대한 사용자 행동 로그이다.

행동 타입:

- `VIEW`
- `SELECT`
- `DESELECT`
- `SAVE_INCLUDED`

주요 활용:

- 장소 조회 수 집계
- 장소 선택 횟수 집계
- 저장된 코스 포함 횟수 집계
- 분위기별/음식별 선택 이력 분석

주요 컬럼:

- `member_id`: 행동한 회원
- `place_id`: 행동 대상 장소
- `course_draft_id`: 임시 코스 맥락
- `course_id`: 저장 코스 맥락
- `action_type`: 행동 종류. `VIEW`, `SELECT`, `DESELECT`, `SAVE_INCLUDED`
- `mood_tag_id`: 행동 당시 선택되어 있던 분위기
- `food_category_id`: 행동 당시 선택되어 있던 음식 카테고리

행동 타입별 저장 시점:

- `VIEW`: 장소 카드 상세 또는 장소 정보를 사용자가 열람했을 때 저장한다. 카드가 목록에 보였다는 이유만으로 모두 저장하면 로그가 과도하게 쌓일 수 있다.
- `SELECT`: 코스 생성 중 사용자가 장소를 추가했을 때 저장한다.
- `DESELECT`: 코스 생성 중 사용자가 선택한 장소를 제거했을 때 저장한다.
- `SAVE_INCLUDED`: 코스 저장 시 해당 코스에 포함된 장소마다 저장한다.

구현 메모:

- `member_id`는 비로그인 탐색 가능성을 고려해 NULL 허용이지만, 저장 코스 관련 행동은 로그인 사용자를 전제로 한다.
- `mood_tag_id`, `food_category_id`는 행동 당시의 선택 조건 스냅샷이다. 이후 draft 선택값이 바뀌어도 과거 로그는 수정하지 않는다.
- `place.selected_count`, `place.saved_count`, `place.viewed_count`는 이 로그를 기반으로 집계하거나, 서비스 로직에서 동시에 증가시킬 수 있다. 초기에는 단순 카운터 증가로 시작해도 된다.

## `CourseActionLog`

테이블: `course_action_log`

코스에 대한 사용자 행동 로그이다.

행동 타입:

- `VIEW`
- `SAVE`
- `REUSE`
- `SHARE`
- `DELETE`

주요 활용:

- 인기 코스 정렬
- 많이 이용한 코스 목록
- 코스 공유/재사용 지표 분석

주요 컬럼:

- `member_id`: 행동한 회원
- `course_id`: 행동 대상 코스
- `action_type`: 행동 종류. `VIEW`, `SAVE`, `REUSE`, `SHARE`, `DELETE`

행동 타입별 저장 시점:

- `VIEW`: 저장 코스 상세 또는 추천 코스 상세를 열람했을 때 저장한다.
- `SAVE`: 사용자가 코스를 저장했을 때 저장한다.
- `REUSE`: 추천 코스나 공유받은 코스를 내 코스 생성 흐름에 가져와 재사용할 때 저장한다.
- `SHARE`: `course_share`를 통해 공유 링크를 만들거나 공유 액션을 수행했을 때 저장한다.
- `DELETE`: 사용자가 저장 코스를 삭제했을 때 저장한다.

구현 메모:

- 인기 코스나 추천 코스 정렬은 초기에 `SERVICE_CREATED` 코스와 운영자 우선순위 중심으로 처리한다.
- 실제 사용자 데이터가 쌓인 뒤 `VIEW`, `SAVE`, `REUSE`, `SHARE` 로그를 추천 정렬 보조 지표로 사용할 수 있다.
- 삭제 로그는 운영 분석용이며, 실제 코스 삭제는 `course.deleted_at` 기반 소프트 삭제를 우선한다.

# 9. MVP 구현 우선순위

1. `member`
2. `taxonomy`
3. `place`
4. `course`
5. `recommendation`
6. `placeops`

MVP의 핵심 흐름은 다음 네 도메인으로 먼저 만들 수 있다.

```text
member → taxonomy → place → course
```

`recommendation`은 처음부터 복잡한 알고리즘으로 만들 필요는 없다.  
초기에는 `place` 또는 `course` 조회 서비스에서 조건 필터링과 정렬을 수행하고, 필요할 때 요청/결과 기록만 `recommendation`에 남기는 방식이 적당하다. 홈 화면 자동 추천, 내 주변 핫플, 기준 장소 주변 추천은 모두 서버 내부적으로 추천 목록을 계산하는 흐름이므로 `recommendation_log.recommendation_type`으로 구분한다.

`placeops`는 관리자 페이지가 없어도 데이터 수집/검수 구조 때문에 필요하다.  
다만 실제 운영 API는 MVP 이후로 미뤄도 된다.

# 10. JPA 구현 팁

## ID 생성 전략

`UMC_Todait_ERD.sql`은 개념 DDL이므로 `AUTO_INCREMENT`, 실제 unique key, 세부 CHECK 제약이 모두 들어간 운영 마이그레이션 파일로 보지 않는다.

엔티티 구현 시점에 ID 생성 전략을 통일한다. MySQL 기준으로는 `IDENTITY` 전략을 우선 검토한다.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## BaseEntity

`created_at`, `updated_at`, `deleted_at` 같은 공통 시간 컬럼은 Spring의 `BaseEntity`로 처리한다.

권장 방식:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

따라서 `updated_at`에 DB 레벨 `ON UPDATE CURRENT_TIMESTAMP(6)`를 반드시 둘 필요는 없다. 시간 값의 기준을 애플리케이션으로 통일하려면 JPA Auditing 기준으로 관리하는 편이 낫다.

## Index 운영 기준

초기 엔티티 구현은 PK/FK 중심으로 시작하고, 실제 API 조회가 시작되면 아래 조회 패턴에 맞춰 일반 인덱스를 추가하는 것을 추천한다.

추천 인덱스는 별도 파일 [todait_recommended_indexes.sql](./todait_recommended_indexes.sql)에 정리해 둔다.

Spring/JPA에서도 `@Table(indexes = ...)`로 인덱스를 선언할 수 있다. 다만 운영 DB 반영은 나중에 Flyway/Liquibase 같은 마이그레이션 도구로 관리하는 편이 안전하다. 현재 프로젝트 단계에서는 조회 패턴을 기록해 두고, 엔티티 구현 시 필요한 인덱스를 선별해서 반영한다.

우선순위가 높은 인덱스:

- 장소 추천 조회: `place(area_id, place_category_id)`
- 추천 가능 장소 필터: `place(review_status, exposure_status, is_active)`
- 거리 계산 후보 축소: `place(latitude, longitude)`
- 저장 코스 목록: `course(member_id, created_at)`
- 코스 장소 순서 조회: `course_place(course_id, visit_order)`
- 임시 코스 장소 순서 조회: `course_draft_place(course_draft_id, visit_order)`
- 추천 결과 순서 조회: `recommendation_result(recommendation_log_id, rank_no)`

주의:

- 이 인덱스들은 unique 제약이 아니다.
- 논리 unique 후보는 기존 정책처럼 DB unique key가 아니라 애플리케이션 레벨에서 검증한다.

## 연관관계 방향

초기에는 `ManyToOne` 중심으로 시작하는 것이 좋다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private Member member;
```

`OneToMany` 컬렉션은 꼭 필요한 경우에만 추가한다.

## Enum 저장

상태값은 Java enum으로 관리하고 DB에는 문자열로 저장하는 것을 추천한다.

```java
public enum CourseDraftStatus {
    MOOD_SELECTING,
    FOOD_SELECTING,
    BASE_PLACE_SELECTING,
    PLACE_SELECTING,
    ORDERING,
    SAVING,
    COMPLETED,
    ABANDONED
}
```

```java
@Enumerated(EnumType.STRING)
@Column(name = "status")
private CourseDraftStatus status;
```

## Soft Delete

다음 엔티티는 소프트 삭제 대상으로 보는 것이 좋다.

- `Member`
- `Place`
- `Course`

권장 처리:

```text
Member.status = DELETED
Member.deletedAt = now()

Place.exposureStatus = INACTIVE 또는 CLOSED
Place.deletedAt = now()

Course.deletedAt = now()
```

## 논리 Unique 검증

개념 DDL에는 unique key를 두지 않았지만, Spring 구현에서는 애플리케이션 레벨에서 최소한 다음 값은 중복 검증해야 한다. AI Agent가 엔티티나 서비스를 구현할 때는 아래 항목을 단순 문서 설명이 아니라 서비스 검증 요구사항으로 해석한다.

- `member.email`
- `member.nickname`
- `member_oauth_account.provider + provider_user_id`
- `term.term_type + version`
- `member_term_agreement.member_id + term_id`
- `area.code`
- `place_category.code`
- `food_category.code`
- `mood_tag.code`
- `course_draft_place.course_draft_id + visit_order`
- `course_place.course_id + visit_order`
- `course_share.share_token`

# 11. 핵심 유스케이스별 도메인 흐름

## 소셜 로그인

```text
member_oauth_account 조회
→ 없으면 member 생성
→ 신규 회원이면 필수 약관 동의 저장
→ 있으면 기존 member 연결
→ refresh_token 발급
→ member.last_login_at 갱신
```

## 일반 이메일 로그인

```text
member.email 조회
→ member.status 확인
→ password_hash 검증
→ refresh_token 발급
→ member.last_login_at 갱신
```

## 일반 이메일 회원가입

```text
Redis 이메일 인증 상태 확인
→ 필수 약관 동의 여부 확인
→ member 생성(email, password_hash, nickname)
→ member_term_agreement 저장
→ notification_setting 기본값 생성
→ refresh_token 발급 또는 로그인 화면으로 이동
```

## 코스 생성 취향 선택

```text
course_draft 생성 또는 조회
→ mood_tag 목록 조회
→ food_category 목록 조회
→ 사용자가 오늘 원하는 분위기 선택
→ course_draft_mood_tag 저장
→ 사용자가 오늘 원하는 음식 카테고리 선택
→ course_draft_food_category 저장
```

## 기준 장소 설정

```text
place 검색
→ area 지원 여부 확인
→ 좌표 존재 여부 확인
→ course_draft.base_place_id 저장
→ course_draft_place에 BASE 장소 저장
```

## 카테고리별 장소 추천

```text
course_draft 조회
→ base_place, mood_tag, food_category 조건 확인
→ place 후보 조회
→ place_mood_tag / place_food_category 매칭
→ 거리, 분위기, 음식, 운영자 우선순위로 정렬
→ recommendation_log 저장
→ recommendation_result 저장
```

세부 처리:

```text
추천 타입 = NEAR_BASE_PLACE
→ 현재 카테고리 탭(place_category) 확인
→ 이미 선택한 장소와 기준 장소 제외
→ 지원 지역, 노출 상태, 검수 상태 필터링
→ 기준 장소와 후보 장소 간 거리 계산
→ 선택 분위기와 place_mood_tag 매칭
→ 식당/디저트 조건이면 food_category 매칭
→ reason_text 생성
→ 결과 없음이면 빈 배열 반환
```

## 홈 추천 데이터 조회

```text
member 조회
→ 홈 인사/사용자 정보 구성
→ SERVICE_CREATED 또는 RECOMMENDED 코스 조회
→ 사용자 이력이 있으면 member_mood_preference / member_food_preference 참고
→ 사용자 이력이 없으면 기본 추천 장소 조회
→ 필요 시 recommendation_log 저장
→ 필요 시 recommendation_result 저장
→ 홈 화면 응답 반환
```

주의:

- 초기에는 "많이 이용한 코스"처럼 실제 이용 데이터가 쌓였다는 표현을 쓰지 않는다.
- 초기 문구는 "추천 코스", "운영자 추천 코스", "기본 추천 코스"처럼 운영자가 준비한 데이터임을 숨기지 않는 방향이 안전하다.
- 홈 API 하나로 묶을지, 추천 코스/추천 장소 API를 분리할지는 프론트 화면 구성과 성능 기준으로 결정한다.

## 검색 전 핫플 또는 기본 추천 장소 조회

```text
위치 권한 상태 확인
→ 위치가 있고 지원 지역 안이면 가까운 지원 지역 기준
→ 위치가 없거나 지원 지역 밖이면 기본 지역 또는 전체 지원 지역 기준
→ place 후보 조회
→ 추천 타입 = HOT_PLACE
→ 거리, 지역, 카테고리, 운영자 우선순위로 정렬
→ 추천 장소 목록 반환
```

주의:

- 위치 권한이 없어도 기준 장소 검색은 가능해야 한다.
- 현재 위치 주변 전체를 추천하는 서비스가 아니라, 홍대/연남/성수 지원 지역 안에서만 추천한다.
- 검색 전 추천 목록은 사용자가 아직 기준 장소를 정하지 못했을 때 선택을 돕는 용도이다.

## 코스 저장

```text
course_draft 조회
→ course 생성
→ course_mood_tag 저장
→ course_food_category 저장
→ course_place 스냅샷 저장
→ course_draft.status = COMPLETED
```

## 저장된 코스 조회

```text
member 기준 course 목록 조회
→ course_place를 visit_order 순서로 조회
→ course_mood_tag / course_food_category 조회
→ course_action_log VIEW 저장
```

## 추천 행동 로그 저장

```text
장소 상세 조회 또는 장소 카드 상세 열람
→ place_action_log VIEW 저장

코스 생성 중 장소 추가
→ course_draft_place 저장
→ place_action_log SELECT 저장

코스 생성 중 장소 삭제
→ course_draft_place 삭제
→ place_action_log DESELECT 저장

코스 저장 완료
→ course 생성
→ course_place 저장
→ 포함된 장소마다 place_action_log SAVE_INCLUDED 저장
→ course_action_log SAVE 저장
```

주의:

- 행동 로그는 추천 품질 개선과 카운트 집계의 근거이다.
- MVP에서 모든 로그를 완벽히 저장하기 어렵다면 `SELECT`, `SAVE_INCLUDED`, `SAVE`부터 저장한다.
- 카드 목록에 노출된 모든 장소를 `VIEW`로 저장하면 로그가 과도해질 수 있으므로, 상세 열람 또는 명시적 클릭 기준을 권장한다.

## 선호 요약 갱신

```text
place_action_log / course_action_log 누적
→ 회원별 자주 선택한 mood_tag / food_category 계산
→ member_mood_preference 저장 또는 갱신
→ member_food_preference 저장 또는 갱신
→ 홈 추천과 추천 정렬 보정에 활용
```

주의:

- MVP 초기에는 선호 요약 갱신을 구현하지 않아도 된다.
- 선호 요약은 사용자의 현재 코스 생성 선택값이 아니라 장기 취향 데이터이다.
- 나중에 배치 작업, 관리자 작업, 이벤트 기반 집계 중 하나로 구현한다.

## 장소 데이터 수집/정제

```text
collection_job 생성
→ 외부 API 호출
→ raw_place 저장
→ category_mapping_rule 적용
→ mood_keyword_rule로 mood tag 초안 생성
→ 운영자 검수
→ place 저장 또는 갱신
→ place_source 연결
```