INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
 email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (UNHEX(REPLACE
 ('88888888-2222-2222-2222-222222222222', '-', '')), '2020-09-01 00:00:00', '2020-09-01 00:00:00', 'flyway',
 'flyway',
  0, null, 'betreuungEvent@dvbern.ch', 'BetreuungsEvent', 'Mutation', '', UNHEX(REPLACE
  ('e3736eb8-6eef-40ef-9e52-96ab48d8f220',
  '-', '')), null, 'INAKTIV');

ALTER TABLE betreuungsmitteilung_pensum add COLUMN isVollstaendig BIT NOT NULL DEFAULT true;
UPDATE betreuungsmitteilung_pensum set ebegu.betreuungsmitteilung_pensum.isVollstaendig = true;