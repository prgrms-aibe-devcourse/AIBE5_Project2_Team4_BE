# DB 자산 관리 규칙

이 저장소에서 DB 관련 자산은 아래 위치를 기준으로 관리합니다.

- `docs/db/oracle`: Oracle 운영 계열 기준 문서와 SQL
- `docs/db/h2`: H2 test 전용 문서와 보조 SQL

운영 원칙:

- 운영 스키마 기준은 Oracle이다.
- H2는 `test` 프로필 전용이며 운영 스키마의 완전한 대체가 아니다.
- DDL은 별도 SQL 자산 기준으로 관리한다.
- JPA `ddl-auto`는 개발/테스트 보조 수단이며 운영 DDL 대체 수단이 아니다.
- sequence, table naming, column naming 규칙은 Oracle 기준 문서와 SQL에서 관리한다.

향후 실제 스키마 파일이 추가되면 이 디렉터리 아래에만 정리한다.
