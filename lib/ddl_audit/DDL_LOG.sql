CREATE TABLE ddl_log (
  operation  VARCHAR2(30),
  obj_owner  VARCHAR2(30),
  obj_name   VARCHAR2(30),
  obj_type   VARCHAR2(30),
  sql_text   CLOB,
  attempt_by VARCHAR2(30),
  attempt_dt DATE,
  user_name  VARCHAR2(50),
  user_host  VARCHAR2(50)
)
