-- 프로젝트 API Oracle 수동 검증용 최소 참조 데이터 시드

MERGE INTO APP_USER target
USING (
    SELECT 1 AS USER_ID,
           'project-user1@example.com' AS EMAIL,
           'bootstrap-password' AS PASSWORD_HASH,
           '프로젝트 테스트 사용자1' AS NAME,
           '01000000001' AS PHONE,
           'ROLE_USER' AS ROLE_CODE,
           'Y' AS ACTIVE_YN
    FROM dual
) source
ON (target.USER_ID = source.USER_ID)
WHEN MATCHED THEN UPDATE SET
    target.EMAIL = source.EMAIL,
    target.PASSWORD_HASH = source.PASSWORD_HASH,
    target.NAME = source.NAME,
    target.PHONE = source.PHONE,
    target.ROLE_CODE = source.ROLE_CODE,
    target.ACTIVE_YN = source.ACTIVE_YN,
    target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    USER_ID, EMAIL, PASSWORD_HASH, NAME, PHONE, ROLE_CODE, ACTIVE_YN, CREATED_AT, UPDATED_AT
) VALUES (
    source.USER_ID, source.EMAIL, source.PASSWORD_HASH, source.NAME, source.PHONE,
    source.ROLE_CODE, source.ACTIVE_YN, SYSTIMESTAMP, SYSTIMESTAMP
);

MERGE INTO APP_USER target
USING (
    SELECT 2 AS USER_ID,
           'project-user2@example.com' AS EMAIL,
           'bootstrap-password' AS PASSWORD_HASH,
           '프로젝트 테스트 사용자2' AS NAME,
           '01000000002' AS PHONE,
           'ROLE_USER' AS ROLE_CODE,
           'Y' AS ACTIVE_YN
    FROM dual
) source
ON (target.USER_ID = source.USER_ID)
WHEN MATCHED THEN UPDATE SET
    target.EMAIL = source.EMAIL,
    target.PASSWORD_HASH = source.PASSWORD_HASH,
    target.NAME = source.NAME,
    target.PHONE = source.PHONE,
    target.ROLE_CODE = source.ROLE_CODE,
    target.ACTIVE_YN = source.ACTIVE_YN,
    target.UPDATED_AT = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    USER_ID, EMAIL, PASSWORD_HASH, NAME, PHONE, ROLE_CODE, ACTIVE_YN, CREATED_AT, UPDATED_AT
) VALUES (
    source.USER_ID, source.EMAIL, source.PASSWORD_HASH, source.NAME, source.PHONE,
    source.ROLE_CODE, source.ACTIVE_YN, SYSTIMESTAMP, SYSTIMESTAMP
);

MERGE INTO PROJECT_TYPE_CODE target
USING (
    SELECT 'HOSPITAL_COMPANION' AS PROJECT_TYPE_CODE,
           '병원 동행' AS PROJECT_TYPE_NAME,
           1 AS SORT_ORDER,
           'Y' AS ACTIVE_YN
    FROM dual
) source
ON (target.PROJECT_TYPE_CODE = source.PROJECT_TYPE_CODE)
WHEN MATCHED THEN UPDATE SET
    target.PROJECT_TYPE_NAME = source.PROJECT_TYPE_NAME,
    target.SORT_ORDER = source.SORT_ORDER,
    target.ACTIVE_YN = source.ACTIVE_YN
WHEN NOT MATCHED THEN INSERT (
    PROJECT_TYPE_CODE, PROJECT_TYPE_NAME, SORT_ORDER, ACTIVE_YN
) VALUES (
    source.PROJECT_TYPE_CODE, source.PROJECT_TYPE_NAME, source.SORT_ORDER, source.ACTIVE_YN
);

MERGE INTO REGION_CODE target
USING (
    SELECT 'SEOUL_GANGNAM' AS REGION_CODE,
           CAST(NULL AS VARCHAR2(20)) AS PARENT_REGION_CODE,
           '서울 강남' AS REGION_NAME,
           2 AS REGION_LEVEL,
           'Y' AS ACTIVE_YN
    FROM dual
) source
ON (target.REGION_CODE = source.REGION_CODE)
WHEN MATCHED THEN UPDATE SET
    target.PARENT_REGION_CODE = source.PARENT_REGION_CODE,
    target.REGION_NAME = source.REGION_NAME,
    target.REGION_LEVEL = source.REGION_LEVEL,
    target.ACTIVE_YN = source.ACTIVE_YN
WHEN NOT MATCHED THEN INSERT (
    REGION_CODE, PARENT_REGION_CODE, REGION_NAME, REGION_LEVEL, ACTIVE_YN
) VALUES (
    source.REGION_CODE, source.PARENT_REGION_CODE, source.REGION_NAME, source.REGION_LEVEL, source.ACTIVE_YN
);
