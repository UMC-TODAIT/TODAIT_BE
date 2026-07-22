# TODAIT_BE

## 로컬 개발 환경 실행

### 1. 환경변수 파일 초기화

`compose.yaml`은 `.env`를 직접 참조합니다. 저장소에는 `.env.example`만 포함되어 있으므로, 최초 체크아웃 후 반드시 복사한 뒤 값을 채워야 합니다.

```bash
cp .env.example .env
```

`.env`에서 최소한 아래 값들을 실제 값으로 채웁니다.

- `DB_NAME`, `DB_USER`, `DB_PW` — 앱이 접속할 전용 DB 계정 (root 사용 금지)
- `MYSQL_ROOT_PASSWORD` — 로컬 MySQL 컨테이너 root 비밀번호
- `JWT_SECRET`, `KAKAO_*`, `GOOGLE_WEB_CLIENT_ID`, `MAIL_*` 등 나머지 필수 값

> `DB_URL`은 compose 실행 시 내부 네트워크 기준(`jdbc:mysql://mysql:3306/${DB_NAME}`)으로 자동 주입됩니다.

### 2. 컨테이너 실행

```bash
docker compose up -d
```

- `mysql`(3306), `redis`(6379)는 호스트의 `127.0.0.1`에만 노출됩니다.
- `app`(8080)은 `mysql`/`redis`가 healthy 상태가 된 후 기동됩니다.

### 3. 종료

```bash
docker compose down
```

DB 데이터를 초기화하려면 볼륨까지 제거합니다.

```bash
docker compose down -v
```
