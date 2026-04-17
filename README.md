# 안심동행 Backend

안심동행 백엔드는 React SPA 프론트엔드와 분리된 Spring Boot 3.x REST API 서버입니다.  
현재 저장소는 문서 중심 샘플이 아니라 실제 Oracle 운영 스키마 기준으로 동작하도록 정리된 백엔드 코드베이스입니다.

중요:

- 문서보다 실제 코드와 실제 DB 스키마가 우선입니다.
- 운영 스키마 변경용 DDL은 이 저장소에서 관리하지 않습니다.
- `local/dev/prod`는 Oracle 기준, `test`만 H2 기준입니다.

## 기술 스택

- Java 21
- Spring Boot 3.3.x
- Maven Wrapper
- Spring Web
- Spring Validation
- Spring Security + JWT
- Spring Data JPA
- QueryDSL
- Spring WebFlux
- Spring WebSocket
- Oracle Database
- H2 in-memory database for tests
- springdoc OpenAPI

## 패키지 구조

주요 소스 루트:

- `src/main/java/com/ieum/ansimdonghaeng/common`
- `src/main/java/com/ieum/ansimdonghaeng/domain`
- `src/main/java/com/ieum/ansimdonghaeng/infra`
- `src/main/resources`
- `src/test/java`

주요 도메인 패키지:

- `auth`
- `user`
- `freelancer`
- `verification`
- `project`
- `proposal`
- `notice`
- `notification`
- `admin`
- `report`
- `review`
- `chat`
- `recommendation`

## 현재 구현 상태

실제 코드 기준으로 현재 바로 연동 가능한 핵심 영역은 다음과 같습니다.

- 인증
  - 회원가입
  - 로그인
  - JWT 재발급
  - 로그아웃
  - Kakao OAuth 로그인
- 사용자
  - 내 정보 조회/수정
  - 공개 프로필 조회
- 프리랜서
  - 내 프로필 생성/조회/수정
  - 포트폴리오 파일 업로드/조회/삭제
  - 공개 프리랜서 목록/상세 조회
  - 프리랜서 워크스페이스 접근 확인
- 검증
  - 검증 요청 생성/조회
  - 검증 파일 업로드/조회/삭제
  - 관리자 승인/반려/목록 조회
- 프로젝트
  - 생성
  - 내 목록 조회
  - 상세 조회
  - 수정
  - 취소
- 제안
  - 사용자 제안 생성
  - 프리랜서 제안 목록/상세 조회
  - 제안 수락
- 공지/알림
  - 관리자 공지 생성/발행
  - 공지 공개 조회
  - 내 알림 목록 조회
  - 알림 단건 읽음
  - 알림 전체 읽음
- 관리자
  - 대시보드
  - 프리랜서 운영 API
  - 프로젝트 운영 API
  - 검증 운영 API
  - 공지 운영 API
  - 리뷰/신고 운영 API 일부

## 이번 정리에서 반영된 핵심 사항

- Oracle 실제 컬럼 길이에 맞춰 DTO/JPA 제약을 재정렬했습니다.
- `notification` 도메인에 실제 사용자용 API를 추가했습니다.
- 컨트롤러의 인증 사용자 ID 추출 로직을 공통화했습니다.
- 비활성 사용자 로그인 거부 테스트를 보강했습니다.
- Oracle 코드 테이블 기준 선검증을 추가해 잘못된 코드값이 FK 500이 아니라 400으로 반환되도록 수정했습니다.

## 데이터베이스 기준

운영 스키마 source of truth:

- `APP_USER`
- `FREELANCER_PROFILE`
- `PORTFOLIO_FILE`
- `VERIFICATION`
- `VERIFICATION_FILE`
- `PROJECT`
- `PROPOSAL`
- `NOTICE`
- `NOTIFICATION`
- `REFRESH_TOKEN`

대표 시퀀스:

- `SEQ_APP_USER`
- `SEQ_FL_PROFILE`
- `SEQ_PROJECT`
- `SEQ_PROPOSAL`
- `SEQ_NOTICE`
- `SEQ_NOTIFICATION`
- `SEQ_VERIFICATION`
- `SEQ_VER_FILE`
- `SEQ_REFRESH_TOKEN`

Oracle 11g 계열 호환을 위해 `OracleLegacyDialect`를 사용합니다.

## 프로필 구성

- `local`
  - Oracle 연결
  - 개발 로그 강화
- `dev`
  - Oracle 연결
  - `ddl-auto=validate`
- `prod`
  - Oracle 연결
  - `ddl-auto=validate`
- `test`
  - H2 in-memory
  - `ddl-auto=create-drop`
  - 코드 테이블 최소 시드 포함

## 환경 변수

예시는 [.env.example](/C:/dev/AIBE5_Project2_Team4_BE/.env.example)에 있습니다.

주요 항목:

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER`
- `CORS_ALLOWED_ORIGINS`
- `WEBSOCKET_ENDPOINT`
- `FILE_STORAGE_BASE_DIR`

주의:

- `local/dev/prod`는 `JWT_SECRET`가 비어 있으면 fail-fast로 기동되지 않습니다.
- Oracle 접속 정보는 `.env` 또는 OS 환경 변수로 주입합니다.
- 테스트 프로필만 테스트 전용 JWT 설정을 사용합니다.

## 실행 방법

컴파일:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

테스트:

```powershell
.\mvnw.cmd test
```

로컬 실행:

```powershell
.\mvnw.cmd spring-boot:run
```

프로필 지정 실행 예시:

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:JWT_SECRET='replace-with-a-real-secret'
.\mvnw.cmd spring-boot:run
```

## API 규약

- Base URL: `/api/v1`
- 성공 응답: `ApiResponse<T>`
- 실패 응답: `ApiResponse<Void>` 내부 `error`
- 날짜/시간 포맷: ISO-8601, `Asia/Seoul`
- 인증 헤더: `Authorization: Bearer <access-token>`

### 인증 API

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/reissue`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/oauth/kakao`

로그인은 `email`이 기준이며, 하위 호환을 위해 `username` alias도 허용합니다.

### 사용자/프리랜서 API

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `GET /api/v1/users/{userId}/public-profile`
- `POST /api/v1/freelancers/me/profile`
- `GET /api/v1/freelancers/me/profile`
- `PATCH /api/v1/freelancers/me/profile`
- `POST /api/v1/freelancers/me/files`
- `GET /api/v1/freelancers/me/files`
- `DELETE /api/v1/freelancers/me/files/{fileId}`

### 검증 API

- `POST /api/v1/freelancers/me/verifications`
- `GET /api/v1/freelancers/me/verifications`
- `GET /api/v1/freelancers/me/verifications/{verificationRequestId}`
- `POST /api/v1/freelancers/me/verifications/{verificationRequestId}/files`
- `GET /api/v1/freelancers/me/verifications/{verificationRequestId}/files`
- `DELETE /api/v1/freelancers/me/verifications/files/{fileId}`

### 프로젝트/제안 API

- `POST /api/v1/projects`
- `GET /api/v1/projects/me`
- `GET /api/v1/projects/{projectId}`
- `PATCH /api/v1/projects/{projectId}`
- `PATCH /api/v1/projects/{projectId}/cancel`
- `POST /api/v1/projects/{projectId}/proposals`
- `GET /api/v1/freelancers/me/proposals`
- `GET /api/v1/freelancers/me/proposals/{proposalId}`
- `PATCH /api/v1/freelancers/me/proposals/{proposalId}/accept`

### 공지/알림 API

- `GET /api/v1/notices`
- `GET /api/v1/notices/{noticeId}`
- `GET /api/v1/notifications`
- `PATCH /api/v1/notifications/{notificationId}/read`
- `PATCH /api/v1/notifications/read-all`

## 상태값

프로젝트 상태:

- `REQUESTED`
- `ACCEPTED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

제안 상태:

- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `EXPIRED`
- `CANCELLED`

검증 상태:

- `PENDING`
- `APPROVED`
- `REJECTED`

## 코드 테이블 검증

다음 값들은 Oracle 코드 테이블 기준으로 검증됩니다.

- `projectTypeCode`
- `serviceRegionCode`
- `activityRegionCodes`
- `availableTimeSlotCodes`
- `projectTypeCodes`

잘못된 코드값은 DB FK 예외까지 가지 않고 `COMMON_400`으로 반환됩니다.

실제 Oracle 기준으로 `AVAILABLE_TIME_SLOT_CODE` 데이터가 비어 있으면 시간 슬롯은 빈 배열로 보내야 합니다.

## CORS / WebSocket / 파일 업로드

기본 허용 origin:

- `http://localhost:5173`
- `http://127.0.0.1:5173`
- `http://localhost:3000`
- `http://127.0.0.1:3000`

기본 WebSocket endpoint:

- `/ws`

파일 업로드는 멀티파트를 사용하며, 저장 경로는 `FILE_STORAGE_BASE_DIR`로 제어합니다.

## 테스트 및 검증 결과

2026-04-16 기준 확인:

- `.\mvnw.cmd -q -DskipTests compile` 성공
- `.\mvnw.cmd test` 성공
  - 88 tests
  - 0 failures
  - 0 errors
- 실제 Oracle `local` 프로필 기동 성공
- 실제 Oracle smoke 검증 성공
  - signup/login/refresh
  - users/me 수정
  - freelancer profile 생성
  - verification 생성/승인
  - project 생성
  - proposal 생성/수락
  - notice 발행
  - notification 조회/전체 읽음

## 남은 참고 사항

- `review` 관련 미완성 영역은 이번 작업 범위에서 제외했습니다.
- `chat`, `recommendation`은 여전히 추가 구현 여지가 있습니다.
- 문서가 아닌 실제 코드와 실제 Oracle 스키마를 기준으로 판단해야 합니다.
