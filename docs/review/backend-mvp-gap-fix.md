# Backend MVP Gap Fix

## 기준

- 기준 레포: `AIBE5_Project2_Team4_BE`
- 기준 프론트: `AIBE5_Project2_Team4_FE`
- 기준 DB: Oracle (`DB_URL=jdbc:oracle:thin:@175.126.145.185:1521:orcl`)
- 원칙:
  - Oracle 스키마를 source of truth로 사용
  - DDL / ALTER / migration 미작성
  - 실제 코드, 테스트, application 설정, Oracle 스키마 우선
  - `feature/list-api` 에 최신 `origin/main` 머지 후 재검증

## 현재 구현 상태

### 1. 마이페이지 집계 API

- `GET /api/v1/users/me/mypage` 구현
- 응답 포함:
  - `user`
  - `projectStats`
  - `reviewStats`
  - `notificationSummary`
  - `freelancerProfile`
  - `verificationSummary`
  - `proposalSummary`
- count query 중심으로 집계하도록 정리

### 2. 프로젝트 작성자용 제안 목록 조회 API

- `GET /api/v1/projects/{projectId}/proposals` 구현
- 프로젝트 작성자 본인만 조회 가능
- `ProposalStatus` 필터 + 페이지네이션 지원
- summary projection 기반으로 목록 응답 구성

### 3. 제안 거절 API

- `PATCH /api/v1/proposals/{proposalId}/reject` 구현
- 프리랜서 본인만 가능
- `PENDING` 상태에서만 가능
- body 없이 처리
- `status -> REJECTED`, `respondedAt` 저장

### 4. 내 신고 목록 조회 API

- `GET /api/v1/reports/me` 구현
- reporter 기준 본인 신고만 반환
- 페이지네이션 지원
- review / handledBy summary 포함

### 5. 리뷰 태그 구조 정리

- canonical field를 `tagCodes: string[]`로 통일
- 적용 범위:
  - create request
  - update request
  - list/detail response
  - entity mapping
  - public freelancer review list
  - `/api/v1/reviews/tag-codes`
- `REVIEW_TAG` / `REVIEW_TAG_CODE`를 그대로 사용하되 JPA 매핑 충돌 제거

### 6. verification 모델 정리

- `VERIFICATION` 단일 aggregate 기준으로 통합
- 중복 매핑이던 `VerificationRequest` / `VerificationRequestRepository` 제거
- 신청, 목록, 상세, 파일 업로드, 관리자 승인/반려를 `Verification` 기준으로 정리
- `APPROVED` / `REJECTED` 상태의 verification 은 파일 upload/delete mutation 을 허용하지 않음

### 7. 파일 다운로드 / 열람 방식 정리

- raw storage path 노출 제거
- 신규 API:
  - `GET /api/v1/files/{fileKey}`
  - `GET /api/v1/files/{fileKey}/download`
- 응답 DTO는 `viewUrl`, `downloadUrl`만 노출
- verification 파일은 본인/관리자만, portfolio 파일은 공개 프로필이면 공개 접근 허용

### 8. 프로젝트 상태 전이 권한 정리

- `REQUESTED -> ACCEPTED`: proposal accept에서만 처리
- `ACCEPTED -> IN_PROGRESS`: 수락된 프리랜서 또는 관리자
- `IN_PROGRESS -> COMPLETED`: 수락된 프리랜서 또는 관리자
- `REQUESTED -> CANCELLED`: 작성자만 가능
- `REQUESTED` 상태에서만 update/cancel 허용 유지

## 기존 문제점

- 마이페이지 API가 단순 사용자 조회 수준이거나 부재했고, 프로젝트/리뷰/알림/검증 집계가 없었음
- 프로젝트 작성자 기준 proposal list API가 owner 정책, 페이지네이션, 목록 필드, 상태 정리가 부족했음
- proposal reject API가 없음
- 내 신고 목록 API가 없음
- review request/response/entity 간 tag 구조가 일관되지 않았고, Oracle 스키마 기준 정리가 필요했음
- verification 모델이 `Verification` 과 `VerificationRequest` 로 중복되어 동일 테이블 책임이 분산됨
- 업로드 파일 응답이 내부 파일 경로/URL 성격이 섞여 프론트에서 직접 쓰기 어려웠음
- start/complete 권한이 실제 제품 흐름과 명확히 정리되어 있지 않았음

## Oracle 스키마 기준 mismatch

- `REVIEW` 는 `REVIEWER_USER_ID` 를 보유하므로 리뷰 작성자 식별은 project owner id 추론이 아니라 컬럼 기준으로 맞춰야 했음
- review tag는 단일 컬럼이 아니라 `REVIEW_TAG`, `REVIEW_TAG_CODE` 구조이므로 canonical contract를 `tagCodes[]` 로 정리함
- Oracle dev DB에는 `REVIEW_TAG_CODE` 기준 데이터가 비어 있었음
  - 코드 검증은 정상적으로 400을 반환
  - smoke는 `tagCodes=[]` 기준으로 검증
  - 운영/개발 데이터 seed가 필요
- verification 은 `VERIFICATION` 단일 테이블이 source of truth이며, 별도 `VerificationRequest` 엔티티 유지가 오히려 혼선을 만들었음
- `PORTFOLIO_FILE.FILE_URL`, `VERIFICATION_FILE.FILE_URL` 은 서버 저장 경로 역할이라 외부 API 응답으로 그대로 노출하면 안 됨

## 프론트 연동 기준 mismatch

- 프론트는 현재 localStorage mock 구조를 사용하고 있어 실제 API 응답 shape 와 연결 어댑터가 필요
- proposal reject mock은 body 없는 액션이라 백엔드도 body 없이 통일
- 프론트 review mock은 `tags: string[]` 를 사용하므로, 백엔드는 `tagCodes[]` 로 고정하고 `/api/v1/reviews/tag-codes` 를 제공
- 공개 freelancer review 는 `publicYn=true`, `activeYn=true`, `ROLE_FREELANCER`, `blindedYn=false`, accepted proposal 연결 조건에서만 노출되도록 정리
- 프론트 프로젝트 권한 mock은 start/complete 를 배정된 프리랜서 또는 관리자에게 열고 있어, 백엔드 정책도 동일하게 정리
- 프론트 파일 사용은 링크 기반이 자연스러워 `viewUrl` / `downloadUrl` 계약으로 정리

## 실제 수정한 항목

- user:
  - `UserController`, `UserService`
  - `UserMyPageResponse`
- proposal:
  - owner proposal list query / DTO / controller
  - reject endpoint controller / service
  - mypage count query 보강
- report:
  - `ReportController`, `ReportService`, `ReportServiceImpl`, `ReportSummaryResponse`
- review:
  - `Review`, `ReviewTag`, `ReviewTagCode`, `ReviewTagId`
  - create/update/detail/list DTO 전면 정리
  - `ReviewController`, `ReviewService`, `ReviewRepository`
  - `ReviewTagCodeRepository`, `ReviewTagRepository`
- verification:
  - `Verification` 중심으로 서비스/DTO/repository/controller 정리
  - `VerificationRequest*` 삭제
- file:
  - `FileController`, `FileService`, `FileKeySupport`
  - file response DTO 전반 `viewUrl` / `downloadUrl` 적용
- project:
  - `start`, `complete` API 추가
  - 상태 전이 권한/예외 정리
- notification:
  - proposal accept, verification approve/reject, project start/complete 에서 알림 생성
- security:
  - public review list / tag code / file endpoint 접근 정책 정리
- exception:
  - unexpected exception logging 추가

## 제거/통합한 중복 코드

- 삭제:
  - `domain/verification/entity/VerificationRequest.java`
  - `domain/verification/repository/VerificationRequestRepository.java`
- review tag 저장 로직에서 `@MapsId` 충돌 매핑 제거
- 파일 응답에서 내부 경로 노출 제거

## 테스트 보강

- 추가 integration test:
  - `UserMyPageIntegrationTest`
  - `ProjectProposalOwnerIntegrationTest`
  - `ProposalRejectIntegrationTest`
  - `ReportControllerIntegrationTest`
  - `ReviewControllerIntegrationTest`
  - `FileControllerIntegrationTest`
  - `ProjectStatusTransitionIntegrationTest`
- 기존 freelancer/admin support test도 canonical verification/review/file contract에 맞게 수정

## 실행 / 검증 결과

### Compile

- 명령: `./mvnw -q -DskipTests compile`
- 결과: 성공

### Test

- 명령: `./mvnw test`
- 결과: 성공
- 통과: 103 tests

### Oracle smoke

- 실행 방식:
  - `local` profile로 앱 기동
  - `--server.port=18080`
  - 실제 Oracle DB 연결 확인 후 HTTP smoke 수행
  - smoke run id: `1776658798052`
- 확인한 흐름:
  - mypage aggregation
  - owner proposal list
  - proposal reject
  - my reports
  - review create/update/list with canonical tag shape
  - verification submit/list/admin approve/reject
  - file view/download
  - project accept/start/complete/cancel
- 핵심 결과:
  - proposal list before: `PENDING`, `PENDING`
  - proposal list after: `REJECTED`, `ACCEPTED`
  - project state: `ACCEPTED -> IN_PROGRESS -> COMPLETED`
  - cancel state: `CANCELLED`
  - report list: `RESOLVED`, handledBy populated
  - freelancer mypage: verification `APPROVED`, proposal summary populated
  - file preview/download: 200 + `Content-Disposition` 정상

## 남은 blocker / TODO

- Oracle dev DB의 `REVIEW_TAG_CODE` 기준 데이터가 비어 있음
  - 현재 코드 검증은 정상
  - 프론트에서 태그 선택 UI를 실제로 쓰려면 운영/개발 기준 데이터 투입 필요
- 프론트 레포는 아직 mock/localStorage store 중심이라, 실제 API 연결 adapter 와 query layer 교체가 필요
- smoke용 실데이터는 unique 식별자로 생성되었고 자동 cleanup 은 수행하지 않음
