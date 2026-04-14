# AGENTS

## 저장소 목적

- 이 저장소는 안심동행 백엔드(Spring Boot + Maven) 프로젝트다.
- React SPA 프론트엔드와 분리된 JSON REST API 서버를 대상으로 한다.
- 현재 단계는 기능 구현 전 셋업 마감(`pre-feature hardening`)이다.

## 현재 단계에서 해야 할 일

- 문서와 설정 기준 통일
- typed properties 기반 공통 설정 정리
- 보안/예외 JSON 응답 일관화
- enum, package skeleton, auth skeleton 보강
- 파일 업로드/DB 자산 위치/테스트 기반 정리
- 컴파일 및 테스트 품질 게이트 유지

## 현재 단계에서 하지 말 일

- 실제 도메인 CRUD 구현
- 실제 로그인/회원가입/리프레시 토큰 저장 구현
- 실제 채팅 기능 완성
- 실제 AI 추천 구현
- 실제 파일 업로드 저장소 구현
- Gradle 추가
- MyBatis 추가
- Thymeleaf/JSP/Freemarker 추가
- Redis/Kafka/Docker/Kubernetes 추가
- 임의의 Oracle 운영 스키마 발명

## 빌드/테스트 명령

- compile: `./mvnw -q -DskipTests compile`
- test: `./mvnw test`
- run(local): `./mvnw spring-boot:run`

Windows PowerShell에서는 `.\mvnw.cmd ...` 형식을 사용한다.

## 수정 원칙

- 기존 구조와 파일을 최대한 보존하고 필요한 부분만 보강한다.
- field injection은 금지하고 생성자 주입만 사용한다.
- 설정값은 가능하면 `@ConfigurationProperties` 기반으로 관리한다.
- 민감정보는 하드코딩하지 않는다.
- `README.md`, `.env.example`, `application*.yml` 간 설정 충돌을 남기지 않는다.

## 완료 기준

- 문서와 환경설정 기준이 통일되어 있다.
- 401/403이 JSON 응답으로 일관화되어 있다.
- 기능 개발 전에 필요한 enum과 skeleton 패키지가 준비되어 있다.
- 파일 업로드/DB 문서 위치/테스트 기반이 정리되어 있다.
- `./mvnw test`가 성공한다.
- 가능하면 `./mvnw -q -DskipTests compile`도 성공한다.

## 보안

- secret commit 금지
- 실제 DB 계정 commit 금지
- 실제 JWT key commit 금지
