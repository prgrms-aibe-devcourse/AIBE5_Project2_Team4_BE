# Backend Frontend Gap For Remaining MVP

## 전제

- 프론트 레포는 현재 API 연결형이 아니라 localStorage / mock store 기반
- 확인한 주요 파일:
  - `src/store/accessControl.ts`
  - `src/store/appProjectStore.ts`
  - `src/store/appProposalStore.ts`
  - `src/store/appReviewStore.ts`
  - `src/mypage/MyPage2.tsx`
- 최신 `origin/main` 머지 후 위 파일 기준으로 계약 재확인

## 정리된 백엔드 계약

### 마이페이지

- API: `GET /api/v1/users/me/mypage`
- 응답:
  - `user`
  - `projectStats`
  - `reviewStats`
  - `notificationSummary`
  - `freelancerProfile`
  - `verificationSummary`
  - `proposalSummary`
- 프론트는 기존처럼 여러 store를 합쳐 계산하지 말고 이 응답을 직접 사용해야 함

### 프로젝트 작성자용 proposal list

- API: `GET /api/v1/projects/{projectId}/proposals?page=0&size=10&status=...`
- 응답: `PageResponse<ProjectProposalSummaryResponse>`
- 항목 핵심 필드:
  - `proposalId`
  - `projectId`
  - `freelancerProfileId`
  - `freelancerName`
  - `freelancerVerifiedYn`
  - `freelancerAverageRating`
  - `status`
  - `message`
  - `createdAt`
  - `respondedAt`

### proposal reject

- API: `PATCH /api/v1/proposals/{proposalId}/reject`
- body 없음
- 응답은 `ProposalDetailResponse`
- 프론트 mock과 동일하게 button action 형태로 연결하면 됨

### review

- canonical request/response field: `tagCodes: string[]`
- API:
  - `POST /api/v1/projects/{projectId}/reviews`
  - `PATCH /api/v1/users/me/reviews/{reviewId}`
  - `GET /api/v1/users/me/reviews`
  - `GET /api/v1/freelancers/{freelancerProfileId}/reviews`
  - `GET /api/v1/reviews/tag-codes`
- 프론트의 기존 `tags: string[]` 는 API adapter에서 `tagCodes` 로 치환 필요
- 태그 선택값은 하드코딩하지 말고 `/api/v1/reviews/tag-codes` 에서 받아야 함

### verification

- canonical model: `verificationId`, `type`, `status`, `requestMessage`, `requestedAt`, `reviewedAt`, `rejectReason`
- API:
  - `POST /api/v1/freelancers/me/verifications`
  - `GET /api/v1/freelancers/me/verifications`
  - `GET /api/v1/freelancers/me/verifications/{verificationId}`
  - `POST /api/v1/freelancers/me/verifications/{verificationId}/files`
  - `GET /api/v1/admin/verifications`
  - `PATCH /api/v1/admin/verifications/{verificationId}/approve`
  - `PATCH /api/v1/admin/verifications/{verificationId}/reject`

### files

- 응답 DTO는 raw path 대신:
  - `viewUrl`
  - `downloadUrl`
- 실제 열람/다운로드 API:
  - `GET /api/v1/files/{fileKey}`
  - `GET /api/v1/files/{fileKey}/download`
- 프론트는 `<a href={viewUrl}>`, `<a href={downloadUrl}>` 형태로 바로 연결 가능

### project status transition

- start: `PATCH /api/v1/projects/{projectId}/start`
- complete: `PATCH /api/v1/projects/{projectId}/complete`
- cancel: `PATCH /api/v1/projects/{projectId}/cancel`
- proposal accept: `PATCH /api/v1/freelancers/me/proposals/{proposalId}/accept`
- 권한 정책은 프론트 mock `accessControl.ts` 와 맞춤:
  - start / complete: 배정된 프리랜서 또는 관리자
  - cancel: 작성자 + `REQUESTED`

## 프론트 기준 실제 gap

### 1. 마이페이지는 mock aggregate 제거 필요

- `MyPage2.tsx` 는 현재 `projects`, `reviews`, `reportedReviews`, `verifyRequests` 를 각각 mock store에서 따로 조합
- 실제 연동 시:
  - account summary
  - review count
  - project status count
  - unread notification count
  - verification summary
  - proposal summary
  를 `GET /api/v1/users/me/mypage` 단일 응답으로 교체해야 함

### 2. proposal 데이터 shape 차이

- 프론트 mock `Proposal` 은 `freelancerEmail`, `userEmail`, `sentAt`, `status` 중심
- 실제 백엔드는 pageable + DB timestamp + freelancer summary 중심
- proposal tab은 list item renderer를 API shape로 변경해야 함

### 3. review tag naming 차이

- 프론트 mock은 `tags`
- 백엔드는 `tagCodes`
- review edit/create form submit 시 필드명 변환 필요

### 4. review tag source 차이

- 프론트는 `DEFAULT_REVIEW_TAGS` 하드코딩
- 백엔드는 `/api/v1/reviews/tag-codes` 제공
- 현재 Oracle dev DB는 tag code data가 비어 있으므로, 빈 목록 대응 UI가 필요

### 5. verification 상세/목록 shape 차이

- 프론트 mock `VerifyRequest` 는 `freelancerName`, `skills`, `requestedAt`, `status` 중심
- 실제 백엔드는 verification aggregate + freelancer profile/file summary 조합
- 관리자 검증 화면은 목록/상세를 실제 API 필드 기준으로 다시 매핑해야 함

### 6. 파일 사용 방식 차이

- 프론트 mock은 portfolio 파일명을 문자열로만 다룸
- 실제 백엔드는 file metadata + `viewUrl` / `downloadUrl`
- 포트폴리오/verification 파일 UI는 파일명 텍스트가 아니라 링크/버튼 기반으로 바꿔야 함

### 7. project transition trigger 차이

- 프론트 `canManageProjectStatus` 는 freelancer email 기반
- 실제 백엔드는 accepted proposal 기준 freelancer ownership 확인
- 프론트는 project detail/list에 배정 freelancer 식별자를 같이 들고 있어야 버튼 제어가 안정적임

### 8. report 탭 차이

- 프론트 mypage report 화면은 현재 관리자 리뷰 moderation mock 중심
- 실제 사용자용 `GET /api/v1/reports/me` 와는 목적이 다름
- 사용자 my reports tab 과 관리자 moderation tab 을 분리해야 함

## 프론트가 바로 맞춰야 하는 최소 변경

- mypage는 `/api/v1/users/me/mypage` 연결
- proposal tab은 owner proposal list pageable API 연결
- proposal reject는 body 없는 PATCH 호출로 연결
- review form/list는 `tagCodes[]` 로 전환
- review tag option은 `/api/v1/reviews/tag-codes` 사용
- verification 화면은 `verificationId` / `type` / `status` / file URL 기준으로 매핑
- 파일 버튼은 `viewUrl`, `downloadUrl` 사용
- project start/complete button은 assigned freelancer 기준으로 노출

## 추가 메모

- 현재 프론트는 전체적으로 mock store 중심이므로 백엔드 계약에 맞춘 API client / query layer 도입이 선행되어야 함
- 백엔드 계약은 최신 `main` 머지 후 기준으로 실제 Oracle 스키마와 smoke 결과에 맞춰 정리됨
