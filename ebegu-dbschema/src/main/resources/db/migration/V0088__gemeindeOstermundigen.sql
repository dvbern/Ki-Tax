-- Damit wir in den Testdaten die Gemeinden richtig setzen können, sollten wir ihre ID kennen. Bern wurde aber mit UUID() erstellt. Wir machen es hier neu
INSERT INTO gemeinde (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, enabled, name)
VALUES ('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0, NULL, TRUE, 'Bern1');

UPDATE dossier
SET gemeinde_id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
UPDATE berechtigung_gemeinde
SET gemeinde_list_id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';

-- Jetzt das "alte" Bern loeschen
DELETE FROM gemeinde
WHERE id <> 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';

UPDATE gemeinde
SET name = 'Bern'
WHERE id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';

INSERT INTO gemeinde (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, enabled, name)
VALUES ('80a8e496-b73c-4a4a-a163-a0b2caf76487', '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0, NULL, TRUE, 'Ostermundigen');