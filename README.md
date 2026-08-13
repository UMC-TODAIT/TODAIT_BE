<div align="center">

<img width="180" alt="TODAIT" src="https://github.com/user-attachments/assets/7434f4da-ae95-46c1-b9a1-372579ee32ae"/>

# TODAIT · 투데잇

### 취향과 지역으로 잇는 데이트·나들이 **코스 추천 서비스**

</div>

<div align="center">

## 서비스 소개

**TODAIT(투데잇)** 은 데이트·나들이 코스를 **추천받고**, 마음에 드는 장소로 **나만의 코스를 직접 구성·저장**하는 서비스입니다.

**오늘의 추천 코스 / 추천 장소** — 날짜 로테이션 + 운영자 큐레이션 기반으로, 같은 날엔 모두에게 일관된 추천을 제공합니다. <br>
**코스 직접 구성** — 기준 장소 → 분위기 → 음식 취향 → 장소 선택 → **드래그로 순서 설정** → 저장까지 단계별 플로우. <br>
**장소 검색·상세** — 카카오 로컬 기반 검색과, 이미지·메뉴·분위기 태그·**영업 상태(OPEN/CLOSED)** 까지 제공하는 상세 화면.

**지원 지역(MVP)** : 홍대 · 연남 · 성수

</div>

## 주요 화면

<table>
  <tr>
    <td align="center"><img width="180" alt="image" src="https://github.com/user-attachments/assets/60cf6b04-e4f9-4099-87c2-9d721354e6f7" />
</td>
    <td align="center"><img width="180" alt="image" src="https://github.com/user-attachments/assets/87dcb121-c58f-439b-9df3-f2b5eb23db22" />
</td>
    <td align="center"><img width="180" alt="image" src="https://github.com/user-attachments/assets/944573d4-bbe5-456a-b36f-9009cde369a4" />
</td>
    <td align="center"><img width="180" alt="image" src="https://github.com/user-attachments/assets/4f8bb38b-e138-4c0c-89d4-febd9ef986d1" />
</td>
    <td align="center"><img width="180" alt="image" src="https://github.com/user-attachments/assets/bc734f45-5d8a-4182-a5d5-d338f3ace282" />
</td>
  </tr>
  <tr>
    <td align="center">메인 화면</td>
    <td align="center">추천 코스</td>
    <td align="center">기준 장소 설정</td>
    <td align="center">코스 구성하기</td>
    <td align="center">장소 상세</td>
  </tr>
</table>


## 시스템 아키텍처

<div align="center">
  <img width="1448" height="1086" alt="image" src="https://github.com/user-attachments/assets/41f92768-dd10-4750-ad36-ed88394ca953" />
</div>

## 기술 스택

| 구분 | 내용 |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.0 · Web MVC · Validation |
| **Persistence** | Spring Data JPA / Hibernate · MySQL 8 |
| **Cache / Store** | Spring Data Redis · Redis 7 |
| **Auth** | Spring Security · JWT(jjwt) · OAuth2 (Kakao · Google) |
| **Mail** | Spring Mail (SMTP) — 이메일 인증 / 비밀번호 재설정 |
| **External API** | Kakao Local (장소 검색) |
| **API Docs** | springdoc-openapi (Swagger UI) |
| **Build** | Gradle |
| **Infra** | AWS EC2 · Docker / Docker Compose · **nginx (HTTPS 리버스 프록시)** |
| **CI/CD** | GitHub Actions (SSH 배포) |


## 핵심 기능

### 회원 / 인증 (`member`)
- 이메일 회원가입·로그인·로그아웃, 이메일 인증코드 발송/검증, 비밀번호 재설정
- **카카오 · 구글 소셜 로그인**(OAuth2) 및 신규 회원 **온보딩**(닉네임·약관 동의)
- **JWT Access/Refresh** 발급 및 리프레시 토큰 **회전(rotation)**

### 코스 구성 (`course`)
- 임시 코스 **단계별 생성 플로우**: 분위기 → 음식 → 기준 장소 → 선택 장소 → **순서 설정(드래그)** → 저장
- 상태 머신 기반 진행(`MOOD_SELECTING → … → ORDERING → SAVING → COMPLETED`)
- 저장 코스 조회/메모/삭제, 만료된 임시 코스 자동 정리(스케줄러)

### 장소 (`place`)
- 카카오 로컬 기반 **장소 검색**(지원 지역/카테고리 필터)
- **장소 상세**: 이미지(대표/내부)·메뉴·분위기 태그·음식 카테고리·**영업 상태 계산**·라스트오더

### 추천 (`recommendation`)
- **홈 추천 코스 / 추천 장소** — 날짜 로테이션 + 운영자 우선순위, 커서 페이지네이션
- **카테고리별 추천 장소 · 핫플레이스** 추천
- 추천 요청/노출 로그 기록(`recommendation_log` / `recommendation_result`)

### 기준 정보 (`taxonomy`)
- 장소 카테고리(카페·액티비티·음식점·바·**기타**)·지역·분위기 태그·음식 카테고리


## API 개요

전체 명세는 앱 실행 후 **Swagger UI** 에서 확인할 수 있습니다.
```
https://api.todait.co.kr/swagger-ui/index.html
```

<details>
<summary><b>주요 엔드포인트 펼쳐보기</b></summary>

| 도메인 | Method & Path | 설명 |
|---|---|---|
| 인증 | `POST /api/auth/signup` · `login` · `logout` | 이메일 회원가입/로그인/로그아웃 |
| 인증 | `POST /api/auth/kakao/login` · `google/login` | 소셜 로그인 |
| 인증 | `POST /api/auth/email/send-code` · `verify-code` | 이메일 인증 |
| 인증 | `POST /api/auth/token/refresh` | 토큰 재발급 |
| 인증 | `POST /api/auth/password-reset/...` · `PATCH /api/auth/password-reset` | 비밀번호 재설정 |
| 회원 | `PATCH /api/members/me/onboarding` | 온보딩(닉네임·약관) |
| 회원 | `GET /api/members/me` · `nickname-availability` | 내 정보 · 닉네임 중복 |
| 장소 | `GET /api/places/search` · `GET /api/places/{placeId}` | 장소 검색 · 상세 |
| 카테고리 | `GET /api/place-categories` | 장소 카테고리 목록 |
| 임시코스 | `POST /api/course-drafts` · `GET /current` | 임시 코스 생성 · 현재 조회 |
| 임시코스 | `PUT /{id}/mood-tags` · `food-categories` · `PATCH /{id}/base-place` | 분위기·음식·기준 장소 |
| 임시코스 | `POST /{id}/places` · `PATCH /{id}/ordering` · `places/order` | 장소 선택·순서 진입/변경 |
| 임시코스 | `POST /{id}/courses` | 코스 저장 |
| 추천 | `GET /api/recommended-courses` · `recommended-places` | 홈 추천 코스·장소 |
| 추천 | `GET /api/course-drafts/{id}/recommended-places` · `hot-places` | 카테고리 추천·핫플 |
| 저장코스 | `GET /api/courses/me/overview` · `GET /{id}` · `DELETE /{id}` | 저장 코스 목록/상세/삭제 |

</details>


## 프로젝트 구조

```
src/main/java/com/example/TODAIT__BE
├── domain
│   ├── member          # 회원, 인증(JWT·OAuth), 이메일 인증, 비밀번호 재설정
│   ├── course          # 임시 코스 구성 플로우, 저장 코스
│   ├── place           # 장소 검색·상세
│   ├── recommendation  # 홈/카테고리/핫플 추천, 추천 로그
│   └── taxonomy        # 지역·카테고리·분위기 태그 등 기준 정보
├── infra               # kakao(장소 검색) · oauth · mail · redis
└── global
    ├── apiPayload      # 공통 응답(ApiResponse)·에러 코드·예외 처리
    ├── security        # JWT 필터·토큰·SecurityConfig
    ├── config          # Jackson·JPA Auditing·Swagger·Redis·Async
    └── common / util   # BaseEntity · 거리/영업시간 계산 등
```
각 도메인은 `controller · service · repository · entity · dto · code · exception` 계층으로 구성됩니다.


## 로컬 실행

```bash
# 1. 환경변수 파일 준비
cp .env.example .env        # DB / JWT / KAKAO / GOOGLE / MAIL 값 채우기

# 2. 인프라(MySQL·Redis) 기동
docker compose up -d mysql redis

# 3. 애플리케이션 실행
./gradlew bootRun
```
- API 문서: `https://api.todait.co.kr/swagger-ui/index.html`
- 필요 env: `DB_URL / DB_NAME / DB_USER / DB_PW / MYSQL_ROOT_PASSWORD`, `JWT_SECRET`, `KAKAO_*`, `GOOGLE_WEB_CLIENT_ID`, `MAIL_*`


## 배포

- **환경**: AWS EC2 단일 노드. 호스트의 **nginx 리버스 프록시**가 **HTTPS(443)를 종료**하고 내부 `docker compose`(app + redis)로 전달. **DB는 관리형 MySQL**.
- **파이프라인** (`.github/workflows/deploy-dev.yml`): `develop` push → `compileJava` 검증 → EC2 **SSH** → 최신 코드 반영 → `docker compose -f compose.deploy.yaml up -d --build` → `/v3/api-docs` **헬스체크**.
- 시크릿(EC2 키·DB 접속정보 등)은 **GitHub Secrets / 서버 환경변수** 로 관리하며 레포에 커밋하지 않습니다.

```
Android ──HTTPS:443──▶ nginx(EC2, TLS 종료) ──:8080──▶ Spring Boot(Docker) ──▶ MySQL / Redis
```


## 협업 & 컨벤션

- **브랜치 전략**: GitHub Flow — `develop` 기반, `feat/#이슈번호-설명` → PR → **코드 리뷰** → 머지
- **이슈 / PR 템플릿** 사용, 최소 1인 이상 리뷰 후 머지
- **커밋 컨벤션**: `feat: · fix: · refactor: · docs: · test:` + 이슈 번호
- 응답은 공통 포맷 `{ isSuccess, code, message, result }` 를 따릅니다.


## 팀

| 이름 | 역할 | GitHub |
|:---:|:---:|:---:|
| 믹키/손홍락 | Backend | [@sonhonglock51](https://github.com/sonhonglock51) |
| 죠/선지오 | Backend | [@JioCoder](https://github.com/JioCoder) |
| 올리/신채영 | Backend | [@chaeyounggggggg](https://github.com/chaeyounggggggg) |
| 고슴이/고정수 | Backend | [@gocleanwater](https://github.com/gocleanwater) |
| 멍이/전지은 | Backend | [@kniiiiko](https://github.com/kniiiiko) |

<div align="center">

**TODAIT**

</div>
