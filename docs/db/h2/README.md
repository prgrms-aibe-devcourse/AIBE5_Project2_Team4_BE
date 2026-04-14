# H2 test 자산 위치

이 디렉터리는 H2 test 전용 자산 위치다.

운영 원칙:

- H2는 `test` 프로필 smoke/integration test 지원이 목적이다.
- Oracle 전용 SQL을 그대로 복제하지 않는다.
- 필요한 경우에만 test fixture SQL 또는 호환성 보조 스크립트를 둔다.
- 운영 DDL의 기준은 여기가 아니라 Oracle 문서/SQL이다.
