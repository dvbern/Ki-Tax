INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')), '2020-08-20 00:00:00', '2020-08-20 00:00:00', 'flyway', 'flyway', 0, null, 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH', '2020-07-31');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')), '2020-08-20 00:00:00', '2020-08-20 00:00:00', 'flyway', 'flyway', 0, null, 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT', '2020-07-17');
