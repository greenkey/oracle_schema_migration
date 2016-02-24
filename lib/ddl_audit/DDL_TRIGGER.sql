CREATE OR REPLACE TRIGGER ddl_trigger
BEFORE DDL
ON SCHEMA

  DECLARE
    stmt     CLOB;
    sql_text ora_name_list_t;
  BEGIN
    FOR i IN 1 .. ora_sql_txt(sql_text)
    LOOP
      -- put all statement lines in 1 field
      stmt := stmt || sql_text(i);
    END LOOP;

    IF ora_sysevent <> 'TRUNCATE'
    THEN
      INSERT INTO ddl_log (operation, obj_owner, obj_name, obj_type, sql_text, attempt_by, attempt_dt, user_name, user_host)
        SELECT
          ora_sysevent,
          ora_dict_obj_owner,
          ora_dict_obj_name,
          ora_dict_obj_type,
          stmt,
          USER,
          SYSDATE,
          SYS_CONTEXT('USERENV', 'OS_USER'),
          SYS_CONTEXT('USERENV', 'HOST')
        FROM dual d
          LEFT OUTER JOIN ddl_ignore i
            ON ora_sysevent LIKE i.operation
               AND ora_dict_obj_owner LIKE i.obj_owner
               AND ora_dict_obj_name LIKE i.obj_name
               AND ora_dict_obj_type LIKE i.obj_type
        WHERE i.rowid IS NULL;
    END IF;
  END ddl_trigger;
/
