CREATE TABLE ddl_ignore AS
  SELECT
    '                              ' AS operation,
    owner                            AS obj_owner,
    object_name                      AS obj_name,
    object_type                      AS obj_type
  FROM all_objects
  WHERE NULL IS NOT NULL