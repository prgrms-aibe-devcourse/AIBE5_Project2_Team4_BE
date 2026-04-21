# Pre-Frontend Integration Backend Audit

## 기준
- Source of truth:
  - `pom.xml`
  - `src/main/java/**`
  - `src/main/resources/application*.yml`
  - `src/test/java/**`
  - 실제 Oracle 로컬 스키마 (`application-local.yml` 기준 `jdbc:oracle:thin:@localhost:1521/XEPDB1`)
- 제외:
  - `README.md`, AGENTS, 기타 md 문서
  - DDL / ALTER / migration SQL

## 현재 구현된 도메인
- 구현 완료 및 실제 코드 확인:
  - auth: signup, login, refresh/reissue, logout, kakao oauth login
  - user: `/users/me`, `/users/me/mypage`
  - freelancer: 프로필 upsert/조회, 공개 목록/상세, 파일 업로드/삭제
  - verification: 신청, 목록, 상세, 파일 업로드, 관리자 승인/반려
  - project: 생성, 목록, 상세, 수정, 취소, 시작, 완료
  - proposal: 생성, 작성자 목록/상세, 프리랜서 목록/상세, accept, reject
  - review: 생성, 수정, 삭제, 공개 조회, tag code 조회
  - report: 생성, `/reports/me`
  - notice: 공개 조회, admin notice
  - notification: 목록, 상세, 읽음, 전체 읽음
  - admin: dashboard, verification, project, freelancer, review, report, notice
  - file: view, download
- placeholder 상태:
  - `domain/recommendation`: 컨트롤러/서비스/리포지토리 골격만 있고 실제 기능 없음
  - `domain/chat`: `package-info.java` 수준 placeholder, 실사용 API/엔티티/서비스 없음

## MVP 범위 판단
- recommendation:
  - 이번 작업 범위에서 제외.
  - 현재 placeholder 이므로 이번 baseline 에서는 미구현 상태로 유지.
- chat:
  - 현재 실제 코드 기준으로 MVP 구현이 전혀 시작되지 않았고, FE 연동 직전 백엔드 정리 범위에서도 필수 흐름으로 연결되지 않음.
  - 이번 작업에서는 `out-of-scope`.
  - 별도 프롬프트에서 요구사항과 계약을 확정한 뒤 신규 구현하는 것이 맞음.

## FE 연동 직전 주요 문제와 조치

### 1. auth / security
- 문제:
  - refresh token 이 DB에 평문 저장되고 있었음.
  - `/api/v1/auth/refresh` 와 `/api/v1/auth/reissue` 가 동일 계약으로 중복 노출.
  - logout 정책이 FE 관점에서 모호했음.
  - signup 응답이 생성 API 치고 `201 Created` 가 아니었음.
- 조치:
  - 신규 refresh token 저장 시 원문 대신 `HmacSHA256(jwt secret)` 해시 저장으로 전환.
  - refresh 시 `hashed -> legacy plaintext fallback` 순으로 조회하여 스키마 변경 없이 기존 토큰도 점진적으로 수용.
  - `/api/v1/auth/refresh` 를 canonical endpoint 로 고정.
  - `/api/v1/auth/reissue` 는 `@Hidden`, `@Deprecated`, `Deprecation`/`Link` 헤더가 붙는 임시 alias 로 축소.
  - logout 은 access token 인증 기반으로 현재 사용자 기준 모든 active refresh token revoke 정책으로 명확화.
  - signup 응답을 `201 Created` + `Location: /api/v1/users/{id}` 로 수정.
- 한계 / blocker:
  - 스키마 변경 금지 조건 때문에 기존 DB의 평문 refresh token row 를 일괄 해시 마이그레이션하지는 못함.
  - 현재는 회전(refresh) 또는 logout 이후 신규/갱신 토큰부터 안전한 저장 방식으로 전환됨.

### 2. 중복 / debug endpoint
- 문제:
  - proposal reject endpoint 가 2개여서 FE 계약이 모호했음.
  - debug 성격 endpoint 가 외부 계약에 섞여 있었음.
- 조치:
  - proposal reject canonical endpoint 를 `/api/v1/freelancers/me/proposals/{proposalId}/reject` 로 고정.
  - 기존 `/api/v1/proposals/{proposalId}/reject` 제거.
  - `/api/v1/admin/access-check` 제거.
  - `/api/v1/freelancers/me/workspace` 제거.
  - 제거된 endpoint 는 404 로 응답되도록 `NoResourceFoundException` 처리 추가.

### 3. code lookup API
- 문제:
  - code table entity/repository 는 있었지만 FE 폼에서 바로 쓸 public lookup API 가 없었음.
- 조치:
  - 추가:
    - `GET /api/v1/codes/project-types`
    - `GET /api/v1/codes/regions`
    - `GET /api/v1/codes/available-time-slots`
  - 규약:
    - `code`
    - `name`
    - `sortOrder`
    - 필요 시 `parentRegionCode`, `regionLevel`, `startMinute`, `endMinute`
  - `activeYn = true` 인 값만 노출하도록 repository/service 정리.
  - `SecurityConfig` 에 public GET 허용 추가.

### 4. API 계약
- `ApiResponse`:
  - canonical envelope 는 `{ success, data, error }`
- `PageResponse`:
  - canonical pagination 필드는 `{ content, page, size, totalElements, totalPages, hasNext }`
- `ErrorResponse`:
  - canonical 필드를 `{ errorCode, message, status, timestamp, path }` 로 고정.
  - 기존 FE 호환성을 위해 `code` alias 도 동일 값으로 계속 제공.
- 삭제 응답:
  - body 를 주는 delete 성격 API 는 `204 No Content` 대신 `200 OK + ApiResponse.empty()` 로 정리.
- 파일 계약:
  - `viewUrl`, `downloadUrl` 중심으로 유지.
  - 내부 저장 경로는 API 응답에 노출하지 않음.

## Oracle 기준 정합성 점검 결과

### 확인 및 수정한 핵심 항목
- 실제 Oracle 연결로 smoke 수행:
  - profile: `local`
  - dialect: `OracleLegacyDialect`
- 스키마 검증 중 발견된 실제 불일치:
  - `NOTICE.PUBLISHED_YN`: 실제 `CHAR(1)` vs 엔티티 `VARCHAR(1)` 기대
  - `NOTIFICATION.READ_YN`: 실제 `CHAR(1)` 대응 필요
  - `REVIEW.BLINDED_YN`: 실제 `CHAR(1)` 대응 필요
- 조치:
  - Oracle 실제 컬럼 타입에 맞춰 `@JdbcTypeCode(SqlTypes.CHAR)` 보정
  - char(1) 기반 Y/N 매핑을 Oracle 검증 기준으로 통일
- smoke 에서 실제 확인한 항목:
  - 테이블/컬럼 존재 및 타입 일부 검증
    - `APP_USER.ACTIVE_YN = CHAR`
    - `PROJECT.REQUEST_DETAIL = CLOB`
    - `NOTICE.PUBLISHED_YN = CHAR`
    - `REFRESH_TOKEN.TOKEN_VALUE = VARCHAR2`
  - sequence 존재 검증
    - `SEQ_APP_USER`
    - `SEQ_PROJECT`
    - `SEQ_REFRESH_TOKEN`
  - major flow 실행
    - auth signup/login/refresh/logout
    - `/users/me`, `/users/me/mypage`
    - freelancer profile / public detail / file upload / file view / file download
    - verification create/detail/file upload/admin approve
    - project create/detail/list/start/complete
    - proposal list/detail/accept/reject
    - review create/update/public list
    - report create `/reports/me`
    - notification list/read/read-all
    - admin major endpoints
    - notice create/public list/detail

### Oracle 로컬 데이터 상태 관련 메모
- 일부 로컬 Oracle 환경에서는 code table 데이터가 비어 있을 수 있었음.
- smoke test 는 트랜잭션 안에서만 최소 참조 코드를 보강하도록 작성해 재현성을 확보함.
- 스키마 변경은 하지 않았고, 테스트 종료 시 트랜잭션 롤백됨.

## 실제 수정한 항목
- auth:
  - refresh token hash 저장 도입
  - refresh/reissue 계약 정리
  - logout 응답 명확화
  - signup `201 Created` 적용
- code:
  - public lookup controller/service/repository query 추가
- common:
  - `ErrorResponse` canonical field 정리
  - 제거 endpoint 404 처리를 위한 예외 매핑 추가
  - public code endpoint 보안 설정 추가
- proposal/debug:
  - reject endpoint 중복 제거
  - debug endpoint 제거
- Oracle:
  - `CHAR(1)` 매핑 정합성 보정
  - 실제 Oracle smoke test 추가
- 테스트:
  - auth canonical contract/regression 강화
  - code lookup integration test 추가
  - removed endpoint regression test 추가
  - canonical reject endpoint regression test 추가

## 남은 TODO / blocker
- recommendation 구현:
  - 여전히 placeholder 이며 별도 AI 추천 프롬프트에서 진행해야 함.
- chat 구현:
  - 현재 out-of-scope.
  - FE 범위에 포함될 경우 별도 MVP 계약 정의 후 신규 구현 필요.
- refresh token legacy row:
  - 스키마 변경 금지로 인해 과거 평문 row 는 즉시 일괄 해시 전환하지 못함.
  - 운영 반영 후 로그인/refresh/logout 을 통해 점진적으로 정리됨.
- 초기 코드 데이터 운영 반영:
  - smoke 는 최소 참조 데이터를 자체 보강하지만, 실제 FE 연동 환경에서는 code table seed 상태를 운영/개발 배포 절차에서 보장하는 것이 바람직함.
