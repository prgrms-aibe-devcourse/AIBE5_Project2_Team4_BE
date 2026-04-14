# 안심동행 Backend

안심동행 백엔드는 React SPA 프론트엔드와 분리된 `Java 21 + Spring Boot + Maven + Oracle` 기반 JSON REST API 서버입니다.

## 현재 단계

현재 저장소는 기능 구현 전 셋업 마감 단계입니다.

- 공통 설정, 보안, 문서, 테스트 기반 정리
- Oracle 운영 계열 프로필과 H2 test 프로필 분리
- Spring Security + JWT + Swagger + WebSocket skeleton 유지
- 실제 도메인 비즈니스 로직 구현은 아직 범위 밖

## 기술 스택

- Java 21
- Spring Boot 3.x
- Maven Wrapper
- Spring Web / Validation / Security / Data JPA / WebSocket / WebFlux / Actuator
- QueryDSL
- Oracle Database
- H2 in-memory(test)
- JWT
- Swagger / OpenAPI

## 프로젝트 열기

### IntelliJ

1. `C:\dev\AIBE5_Project2_Team4_BE`를 엽니다.
2. `pom.xml`을 Maven 프로젝트로 import 합니다.
3. `Project SDK`와 Maven Runner JRE를 Java 21로 맞춥니다.
4. 필요하면 `Annotation Processing`을 활성화합니다.

### Maven import

```powershell
.\mvnw.cmd dependency:resolve
```

## 실행 명령

기본 프로필은 `local`입니다.

```powershell
.\mvnw.cmd spring-boot:run
```

테스트를 제외한 컴파일 점검:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

전체 테스트:

```powershell
.\mvnw.cmd test
```

## 프로필과 데이터베이스

- `local`: Oracle placeholder, 로컬 개발 로그 강화
- `dev`: Oracle placeholder, `ddl-auto=validate`
- `prod`: Oracle placeholder, `ddl-auto=validate`, 최소 로그
- `test`: H2 in-memory, Oracle 없이 테스트 실행

규칙:

- 운영 DDL은 Oracle 기준으로 별도 SQL 자산에서 관리합니다.
- H2는 오직 테스트 전용입니다.
- `ddl-auto`는 운영 DDL 대체 수단이 아닙니다.

DB 관련 문서는 아래 위치를 사용합니다.

- [docs/db/README.md](/C:/dev/AIBE5_Project2_Team4_BE/docs/db/README.md)
- [docs/db/oracle/README.md](/C:/dev/AIBE5_Project2_Team4_BE/docs/db/oracle/README.md)
- [docs/db/h2/README.md](/C:/dev/AIBE5_Project2_Team4_BE/docs/db/h2/README.md)

## API 규약

- API base path: `/api/v1`
- 성공 응답: `ApiResponse<T>`
- 실패 응답: `ErrorResponse`를 포함한 `ApiResponse`
- wrapped response를 사용하는 API는 일관성을 위해 `200/201` 기반으로 응답하고 `204`는 기본 정책으로 사용하지 않습니다.
- JSON 날짜/시간 직렬화는 ISO-8601 기준이며 timezone은 `Asia/Seoul`입니다.
- 인증 헤더: `Authorization: Bearer <token>`
- 프론트엔드와 분리된 백엔드 서버입니다.

## 공통 엔드포인트

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI Docs: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- Actuator Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- WebSocket handshake endpoint: `/ws`

## CORS 기본값

기본 허용 origin 예시는 아래 4개입니다.

- `http://localhost:5173`
- `http://127.0.0.1:5173`
- `http://localhost:3000`
- `http://127.0.0.1:3000`

## 파일 업로드 설정

실제 업로드 저장 서비스는 아직 구현하지 않았고, 설정만 준비되어 있습니다.

- multipart max file size
- multipart max request size
- 파일 저장 base dir placeholder

관련 설정은 `spring.servlet.multipart.*`, `app.file-storage.base-dir`에서 관리합니다.

## 환경변수

주요 환경변수는 [.env.example](/C:/dev/AIBE5_Project2_Team4_BE/.env.example)를 기준으로 맞춥니다.

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`
- `JWT_REFRESH_TOKEN_EXPIRATION_MINUTES`
- `CORS_ALLOWED_ORIGINS`
- `WEBSOCKET_ENDPOINT`
- `AI_BASE_URL`
- `FILE_STORAGE_BASE_DIR`
- `MULTIPART_MAX_FILE_SIZE`
- `MULTIPART_MAX_REQUEST_SIZE`

실제 secret, 실제 Oracle 계정, 실제 토큰 키는 커밋하지 않습니다.

## 인증 skeleton

현재 정리된 auth skeleton endpoint는 아래와 같습니다.

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/reissue`
- `POST /api/v1/auth/logout`

현재 단계에서는 DTO, validation, controller/service 시그니처와 공통 응답 규약만 정리되어 있습니다.

## 테스트 범위

현재 최소 품질 게이트는 아래를 포함합니다.

- Spring context load
- `BooleanToYnConverter` 테스트
- JWT properties binding 또는 token smoke
- security unauthorized/forbidden JSON 응답
- Querydsl bean smoke
- OpenAPI / actuator health smoke

모든 테스트는 Oracle 없이 `test` 프로필의 H2로 실행됩니다.
