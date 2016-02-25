CREATE TABLE ddl_log AS
  SELECT
    ACTION_NAME                       AS operation,
    s.owner                           AS obj_owner,
    OBJ_NAME                          AS obj_name,
    object_type                       AS obj_type,
    empty_clob()                      AS sql_text,
    USER                              AS attempt_by,
    sysdate                           AS attempt_dt,
    SYS_CONTEXT('USERENV', 'OS_USER') AS user_name,
    SYS_CONTEXT('USERENV', 'HOST')    AS user_host
  FROM USER_AUDIT_STATEMENT s, all_objects o
  WHERE NULL IS NOT NULL