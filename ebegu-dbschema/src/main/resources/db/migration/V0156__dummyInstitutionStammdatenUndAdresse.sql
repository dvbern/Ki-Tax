-- Dummy Institution for Tagesfamilien
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status)
VALUES ('00000000-0000-0000-0000-000000000001', '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'UNKNOWN_TAGESFAMILIE', 'e3736eb8-6eef-40ef-9e52-96ab48d8f220', null, 'AKTIV');

-- Dummy KITA Institution Adresse
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde, gueltig_ab, gueltig_bis, hausnummer, land, ort, plz, strasse, zusatzzeile)
VALUES ('00000000-0000-0000-0000-000000000000', '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', '21.0', 'CH', 'Bern', '3022.0', 'Nussbaumstrasse', null);

-- Dummy Tagesfamilien Institution Adresse
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde, gueltig_ab, gueltig_bis, hausnummer, land, ort, plz, strasse, zusatzzeile)
VALUES ('00000000-0000-0000-0000-000000000001', '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', '21.0', 'CH', 'Bern', '3022.0', 'Nussbaumstrasse', null);

-- Dummy KITA Institution Stammdaten
INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, iban, adresse_id, institution_id, kontoinhaber, adresse_kontoinhaber_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, mail, telefon)
VALUES ('00000000-0000-0000-0000-000000000000', '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'KITA', null, '00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000', null, null, null, null, 'mail@example.com', null);

-- Dummy Tagesfamilien Institution Stammdaten
INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, iban, adresse_id, institution_id, kontoinhaber, adresse_kontoinhaber_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, mail, telefon)
VALUES ('00000000-0000-0000-0000-000000000001', '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'TAGESFAMILIEN', null, '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', null, null, null, null, 'mail@example.com', null);
