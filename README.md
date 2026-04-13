# 안심동행 Backend

안심동행은 생활 지원이 필요한 사용자가 프로젝트를 등록하고, 적합한 프리랜서를 탐색·제안·매칭한 뒤, 진행·완료·리뷰·관리자 운영까지 웹에서 처리할 수 있도록 설계한 매칭 플랫폼입니다.

## 1. 프로젝트 소개

### 서비스 한 줄 소개
생활 지원이 필요한 사용자와 프리랜서를 연결하는 웹 기반 매칭 플랫폼 백엔드입니다.

### 핵심 흐름
회원가입 / 로그인  
→ 프로젝트 등록  
→ 프리랜서 탐색  
→ 프로젝트 제안  
→ 프리랜서 수락  
→ 프로젝트 진행  
→ 프로젝트 완료  
→ 리뷰 작성  
→ 관리자 검증 및 운영

### 개발 목적
- 생활 지원 서비스를 웹 기반 매칭 플랫폼 형태로 구현
- 사용자 / 프리랜서 / 관리자 역할을 분리한 구조 설계
- REST API 중심 백엔드 아키텍처 구축
- Oracle DB 기반의 안정적인 데이터 모델 구성
- AI 태그 기반 추천 보조 기능 및 1:1 채팅 MVP 구조 반영

---

## 2. 개발 범위

### MVP 범위
- 회원가입 / 로그인 / 로그아웃 / 토큰 재발급
- 사용자 / 프리랜서 / 관리자 권한 분리
- 프리랜서 프로필 등록 / 수정
- 포트폴리오 업로드
- 검증 신청 / 승인 / 반려
- 프로젝트 등록 / 조회 / 수정 / 취소
- 프로젝트 제안 / 수락 / 거절
- 프로젝트 상태 관리
- 리뷰 작성 / 수정 / 삭제 / 신고
- 공지사항 / 알림 기능
- 관리자 운영 기능
- 실시간 채팅 MVP
- AI 태그 기반 추천 보조 기능

### 이번 초기 세팅 범위
- Maven 기반 Spring Boot 프로젝트 생성
- Java 21 / Oracle / H2(test) 환경 분리
- 공통 설정 파일 및 profile 구성
- 공통 응답 / 예외 처리 구조
- Security / JWT 기본 골격
- Swagger/OpenAPI 기본 설정
- WebSocket / STOMP 기본 골격
- 패키지 구조 및 디렉토리 초기화
- 테스트 가능한 최소 환경 구성

### 현재 범위에서 제외
- React 프론트엔드 구현
- Redis / Kafka / Docker / Kubernetes
- OAuth 로그인
- 실제 AI API 완성 연동
- 실제 파일 저장소 연동
- 배포 자동화 / CI-CD / 모니터링 고도화

---

## 3. 기술 스택

### Frontend
- React (별도 개발)

### Backend
- Java 17
- Spring Boot
- Spring Web
- Spring Security
- JWT
- Spring Data JPA
- QueryDSL
- Spring Validation
- Spring WebSocket
- STOMP
- WebClient
- Swagger / OpenAPI
- Maven
- Lombok

### Database
- Oracle Database
- H2 Database (test)

### Collaboration
- Git
- GitHub
- Notion
- Figma

---

## 4. 브랜치 전략

기본 브랜치는 아래처럼 운영합니다.

- `main` : 배포/제출 기준 브랜치
- `develop` : 통합 개발 브랜치
- `feature/*` : 기능 개발
- `fix/*` : 버그 수정
- `docs/*` : 문서 작업
- `refactor/*` : 리팩토링
- `chore/*` : 설정, 빌드, 잡무성 변경

### 브랜치 네이밍 예시
```bash
feature/auth-login
feature/project-create
feature/proposal-accept
fix/jwt-filter-error
docs/readme-init
refactor/project-service
chore/maven-wrapper
```

### 브랜치 운영 규칙
1. 모든 작업은 `develop` 에서 분기합니다.
2. 작업 브랜치에서 개발 후 Pull Request를 생성합니다.
3. 리뷰 후 `develop` 으로 병합합니다.
4. 제출/배포 시점에 `develop` → `main` 으로 병합합니다.

---

## 5. 커밋 컨벤션

커밋 메시지는 아래 형식을 사용합니다.

```text
type(scope): subject
```

### 타입 목록
- `feat` : 새로운 기능
- `fix` : 버그 수정
- `docs` : 문서 수정
- `style` : 포맷팅, 세미콜론 등 비기능 변경
- `refactor` : 리팩토링
- `test` : 테스트 추가/수정
- `chore` : 빌드, 설정, 패키지 관리
- `build` : Maven/의존성/빌드 관련
- `ci` : CI 설정 관련

### 예시
```bash
feat(auth): add login API skeleton
feat(project): add project domain package structure
fix(security): resolve jwt filter exception handling
docs(readme): add project overview and conventions
build(maven): initialize spring boot project with pom.xml
test(common): add boolean yn converter test
```

### 커밋 작성 규칙
- 제목은 50자 내외로 간결하게 작성
- 제목은 명령형 또는 변경 요약 형태로 작성
- 하나의 커밋은 하나의 의도를 가지도록 유지
- 설정 변경과 기능 개발은 가능한 분리

---

## 6. 프로젝트 디렉토리 구조

```text
src
├─ main
│  ├─ java
│  │  └─ com
│  │     └─ ieum
│  │        └─ ansimdonghaeng
│  │           ├─ AnsimdonghaengApplication.java
│  │           ├─ common
│  │           │  ├─ audit
│  │           │  ├─ config
│  │           │  ├─ converter
│  │           │  ├─ docs
│  │           │  ├─ exception
│  │           │  ├─ jwt
│  │           │  ├─ response
│  │           │  ├─ security
│  │           │  └─ websocket
│  │           ├─ infra
│  │           │  └─ ai
│  │           │     ├─ client
│  │           │     ├─ config
│  │           │     └─ dto
│  │           └─ domain
│  │              ├─ auth
│  │              ├─ user
│  │              ├─ freelancer
│  │              ├─ project
│  │              ├─ proposal
│  │              ├─ review
│  │              ├─ verification
│  │              ├─ notification
│  │              ├─ notice
│  │              ├─ chat
│  │              └─ code
│  └─ resources
│     ├─ application.yml
│     ├─ application-local.yml
│     ├─ application-dev.yml
│     ├─ application-prod.yml
│     └─ application-test.yml
└─ test
   └─ java
      └─ com
         └─ ieum
            └─ ansimdonghaeng
```

---

## 7. 초기 프로젝트 설계 원칙

### 아키텍처 방향
- React 프론트엔드와 분리된 REST API 서버
- JSON 응답 기반 구조
- Spring Security + JWT 기반 stateless 인증
- Oracle 운영 DB / H2 테스트 DB 분리
- JPA + QueryDSL 기반 데이터 접근
- Swagger/OpenAPI 기반 API 문서화
- WebSocket + STOMP 기반 채팅 확장 가능 구조

### 패키지 설계 원칙
- `common` : 공통 설정, 응답, 예외, 보안, 컨버터
- `infra` : 외부 연동
- `domain` : 비즈니스 도메인 단위 패키지 분리

### DB 설계 원칙
- Oracle 기준 테이블/시퀀스 설계
- 다중값은 정규화 테이블로 분리
- 프로젝트 수락 프리랜서는 `acceptedFreelancerId` 저장보다 `ACCEPTED proposal` 을 기준으로 판단
- 리뷰는 프로젝트 완료 이후 가능한 행위로 처리

---

## 8. 실행 방법

### 요구 사항
- JDK 21
- Maven Wrapper 사용 권장
- Oracle Database
- 테스트 시 H2 사용

### 로컬 실행
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 테스트 실행
```bash
./mvnw test
```

### 패키징
```bash
./mvnw clean package
```

---

## 9. 환경 변수 예시

```env
SPRING_PROFILES_ACTIVE=local

DB_URL=jdbc:oracle:thin:@localhost:1521:xe
DB_USERNAME=your_username
DB_PASSWORD=your_password

JWT_SECRET=change-this-secret
JWT_ISSUER=ansimdonghaeng
JWT_ACCESS_TOKEN_EXPIRE_SECONDS=3600
JWT_REFRESH_TOKEN_EXPIRE_SECONDS=1209600

CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000
SERVER_PORT=8080
```

---

## 10. API / 문서

- Base URL: `/api/v1`
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI Docs: `/v3/api-docs`

---

## 11. 협업 규칙

### Pull Request 권장 방식
- PR 제목도 commit convention에 가깝게 작성
- 변경 목적, 주요 변경점, 테스트 여부를 적기
- 너무 큰 PR보다 기능 단위 PR 권장

### 리뷰 시 확인 포인트
- 도메인 책임 분리
- 예외 처리 일관성
- API 응답 형식 일관성
- 보안 설정 누락 여부
- Oracle / H2 profile 분리 여부

---

## 12. 향후 구현 우선순위

1. Maven 기반 Spring Boot 초기 세팅
2. Security / JWT / CORS 공통 설정
3. 공통 응답 / 예외 처리
4. 사용자 / 인증 도메인 구현
5. 프로젝트 / 제안 도메인 구현
6. 프리랜서 프로필 / 검증 구현
7. 리뷰 / 신고 / 알림 / 공지 구현
8. 채팅 MVP 및 AI 추천 보조 기능 확장

---

## 13. 참고

본 저장소는 백엔드 전용 저장소이며, 프론트엔드는 React 기반으로 별도 개발합니다.
초기 단계에서는 안정적인 프로젝트 골격과 개발 규칙 정리를 우선합니다.
