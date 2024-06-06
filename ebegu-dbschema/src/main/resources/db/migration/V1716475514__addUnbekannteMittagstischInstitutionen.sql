/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));
SET @mandant_id_luzern = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
SET @mandant_id_bern = UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', ''));
SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));

INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status,
						event_published)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  'Unbekannter Mittagtisch' AS name,
		  @mandant_id_schwyz AS mandant_id,
		  NULL AS traegerschaft_id,
		  'AKTIV' AS status,
		  TRUE AS event_published) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', ''))
	)
LIMIT 1;

INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  NULL AS gemeinde,
		  '21.0' AS hausnummer,
		  'CH' AS land,
		  NULL AS organisation,
		  'Schwyz' AS ort,
		  '3022' AS plz,
		  'Nussbaumstrasse' AS strasse,
		  NULL AS zusatzzeile) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', ''))
	)
LIMIT 1;

INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									adresse_id,
									institution_id,
									institution_stammdaten_betreuungsgutscheine_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite,
									oeffnungszeiten)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  'MITTAGSTISCH' AS betreuungsangebot_typ,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', '')) AS adresse_id,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', '')) AS institution_id,
		  NULL AS institution_stammdaten_betreuungsgutscheine_id,
		  NULL AS institution_stammdaten_tagesschule_id,
		  NULL AS institution_stammdaten_ferieninsel_id,
		  'mail@example.com' AS mail,
		  NULL AS telefon,
		  NULL AS webseite,
		  NULL AS oeffnungszeiten) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000015', '-', ''))
	)
LIMIT 1;


INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status,
						event_published)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  'Unbekannter Mittagtisch' AS name,
		  @mandant_id_bern AS mandant_id,
		  NULL AS traegerschaft_id,
		  'AKTIV' AS status,
		  TRUE AS event_published) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', ''))
	)
LIMIT 1;

INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  NULL AS gemeinde,
		  '21.0' AS hausnummer,
		  'CH' AS land,
		  NULL AS organisation,
		  'Schwyz' AS ort,
		  '3022' AS plz,
		  'Nussbaumstrasse' AS strasse,
		  NULL AS zusatzzeile) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', ''))
	)
LIMIT 1;

INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									adresse_id,
									institution_id,
									institution_stammdaten_betreuungsgutscheine_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite,
									oeffnungszeiten)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  'MITTAGSTISCH' AS betreuungsangebot_typ,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) AS adresse_id,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) AS institution_id,
		  NULL AS institution_stammdaten_betreuungsgutscheine_id,
		  NULL AS institution_stammdaten_tagesschule_id,
		  NULL AS institution_stammdaten_ferieninsel_id,
		  'mail@example.com' AS mail,
		  NULL AS telefon,
		  NULL AS webseite,
		  NULL AS oeffnungszeiten) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', ''))
	)
LIMIT 1;


INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status,
						event_published)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  'Unbekannter Mittagtisch' AS name,
		  @mandant_id_solothurn AS mandant_id,
		  NULL AS traegerschaft_id,
		  'AKTIV' AS status,
		  TRUE AS event_published) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', ''))
	)
LIMIT 1;

INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  NULL AS gemeinde,
		  '21.0' AS hausnummer,
		  'CH' AS land,
		  NULL AS organisation,
		  'Schwyz' AS ort,
		  '3022' AS plz,
		  'Nussbaumstrasse' AS strasse,
		  NULL AS zusatzzeile) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', ''))
	)
LIMIT 1;

INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									adresse_id,
									institution_id,
									institution_stammdaten_betreuungsgutscheine_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite,
									oeffnungszeiten)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  'MITTAGSTISCH' AS betreuungsangebot_typ,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', '')) AS adresse_id,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', '')) AS institution_id,
		  NULL AS institution_stammdaten_betreuungsgutscheine_id,
		  NULL AS institution_stammdaten_tagesschule_id,
		  NULL AS institution_stammdaten_ferieninsel_id,
		  'mail@example.com' AS mail,
		  NULL AS telefon,
		  NULL AS webseite,
		  NULL AS oeffnungszeiten) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000017', '-', ''))
	)
LIMIT 1;


INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status,
						event_published)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  'Unbekannter Mittagtisch' AS name,
		  @mandant_id_luzern AS mandant_id,
		  NULL AS traegerschaft_id,
		  'AKTIV' AS status,
		  TRUE AS event_published) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', ''))
	)
LIMIT 1;

INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  NULL AS gemeinde,
		  '21.0' AS hausnummer,
		  'CH' AS land,
		  NULL AS organisation,
		  'Schwyz' AS ort,
		  '3022' AS plz,
		  'Nussbaumstrasse' AS strasse,
		  NULL AS zusatzzeile) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', ''))
	)
LIMIT 1;

INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									adresse_id,
									institution_id,
									institution_stammdaten_betreuungsgutscheine_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite,
									oeffnungszeiten)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  'MITTAGSTISCH' AS betreuungsangebot_typ,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', '')) AS adresse_id,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', '')) AS institution_id,
		  NULL AS institution_stammdaten_betreuungsgutscheine_id,
		  NULL AS institution_stammdaten_tagesschule_id,
		  NULL AS institution_stammdaten_ferieninsel_id,
		  'mail@example.com' AS mail,
		  NULL AS telefon,
		  NULL AS webseite,
		  NULL AS oeffnungszeiten) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000018', '-', ''))
	)
LIMIT 1;


INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status,
						event_published)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  'Unbekannter Mittagtisch' AS name,
		  @mandant_id_ar AS mandant_id,
		  NULL AS traegerschaft_id,
		  'AKTIV' AS status,
		  TRUE AS event_published) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', ''))
	)
LIMIT 1;

INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  NULL AS gemeinde,
		  '21.0' AS hausnummer,
		  'CH' AS land,
		  NULL AS organisation,
		  'Schwyz' AS ort,
		  '3022' AS plz,
		  'Nussbaumstrasse' AS strasse,
		  NULL AS zusatzzeile) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', ''))
	)
LIMIT 1;

INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									adresse_id,
									institution_id,
									institution_stammdaten_betreuungsgutscheine_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite,
									oeffnungszeiten)
SELECT *
FROM (SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', '')) AS id,
		  '2024-01-01 00:00:00' AS timestamp_erstellt,
		  '2024-01-01 00:00:00' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  NULL AS vorgaenger_id,
		  '1000-01-01' AS gueltig_ab,
		  '9999-12-31' AS gueltig_bis,
		  'MITTAGSTISCH' AS betreuungsangebot_typ,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', '')) AS adresse_id,
		  UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', '')) AS institution_id,
		  NULL AS institution_stammdaten_betreuungsgutscheine_id,
		  NULL AS institution_stammdaten_tagesschule_id,
		  NULL AS institution_stammdaten_ferieninsel_id,
		  'mail@example.com' AS mail,
		  NULL AS telefon,
		  NULL AS webseite,
		  NULL AS oeffnungszeiten) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000019', '-', ''))
	)
LIMIT 1;
