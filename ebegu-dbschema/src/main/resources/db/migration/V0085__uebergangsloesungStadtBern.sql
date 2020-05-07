INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('6ca2551d-8a3a-4d44-88af-59c1f5392ed9', '-','')), '2020-04-09 00:00:00', '2020-04-09 00:00:00', 'flyway', 'flyway', 0, null, 'STADT_BERN_ASIV_START_DATUM', '01.01.2021');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('06e8af7d-26df-4ec9-bb9d-dc1a524091df', '-','')), '2020-04-09 00:00:00', '2020-04-09 00:00:00', 'flyway', 'flyway', 0, null, 'STADT_BERN_ASIV_CONFIGURED', 'false');
