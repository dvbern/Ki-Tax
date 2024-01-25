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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
SET @gesuchsperiode_id = UNHEX(REPLACE('1b0ed338-b3d2-11ee-829a-0242ac160002', '-', ''));
SET @aktuelle_gp_solothun := (SELECT gesuchsperiode.id
		   FROM gesuchsperiode
				INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
		   WHERE mandant_identifier = 'SOLOTHURN' AND gueltig_ab = '2022-08-01');

INSERT INTO mandant
VALUES (@mandant_id_schwyz, '2021-11-30 00:00:00', '2021-11-30 00:00:00', 'flyway', 'flyway', 0, NULL, 'Kanton Schwyz', 'SCHWYZ', false, 0, 0);

# APPLICATION PROPERTIES
INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										 version, vorgaenger_id, name, value, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
	   NULL, name, value, @mandant_id_schwyz
FROM application_property
WHERE mandant_id = @mandant_id_solothurn AND
	  NOT EXISTS(SELECT name
				 FROM application_property a_p
				 WHERE mandant_id = @mandant_id_schwyz AND
					   a_p.name = application_property.name);


UPDATE application_property SET value = '#ee1d23' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = '#BF0425' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = '#F0C3CB' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'logo-kibon-schwyz.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'logo-kibon-white-schwyz.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @mandant_id_schwyz;

UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'false' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_schwyz;

# BFS Gemeinden
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab)
VALUES
	(UUID(), @mandant_id_schwyz, 'SZ', 1301, 'Einsiedeln', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1311, 'Gersau', '1991-01-01'),
	# 1320s
	(UUID(), @mandant_id_schwyz, 'SZ', 1321, 'Feusisberg', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1322, 'Freienbach', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1323, 'Wollerau', '1991-01-01'),
	# 1330s
	(UUID(), @mandant_id_schwyz, 'SZ', 1331, 'Küssnacht (SZ)', '1991-01-01'),
	# 1340s
	(UUID(), @mandant_id_schwyz, 'SZ', 1341, 'Altendorf', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1342, 'Galgenen', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1343, 'Innerthal', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1344, 'Lachen', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1345, 'Reichenburg', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1346, 'Schübelbach', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1347, 'Tuggen', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1348, 'Voderthal', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1349, 'Wangen (SZ)', '1991-01-01'),
	# 1360s
	(UUID(), @mandant_id_schwyz, 'SZ', 1361, 'Alpthal', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1362, 'Arth', '1991-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1363, 'Illgau', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1364, 'Ingenbohl', '1961-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1365, 'Lauerz', '1995-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1366, 'Morschach', '2010-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1367, 'Muotathal', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1368, 'Oberiberg', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1369, 'Riemenstalden', '2013-01-01'),
	# 1370s
	(UUID(), @mandant_id_schwyz, 'SZ', 1370, 'Rothenthurm', '2014-01-01'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1371, 'Sattel', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1372, 'Schwyz', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1373, 'Steinen', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1374, 'Steinerberg', '1848-09-12'),
	(UUID(), @mandant_id_schwyz, 'SZ', 1375, 'Unteriberg', '1848-09-12');

INSERT INTO gesuchsperiode
VALUES (@gesuchsperiode_id, NOW(), NOW(), 'system_sz', 'system_sz', 0, NULL, '2024-08-01', '2025-07-31', NULL,
		'ENTWURF', NULL, NULL, NULL,
		NULL, NULL, NULL, @mandant_id_schwyz, NULL,
		NULL);

# Einstellungen für Gesuchsperiode kopieren
INSERT INTO einstellung
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system_sz', 'system_sz', 0, einstellung_key, value, NULL,
	   (SELECT gesuchsperiode.id
		FROM gesuchsperiode
			 INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
		WHERE mandant_identifier = 'SCHWYZ'), NULL, erklaerung
FROM einstellung
WHERE gesuchsperiode_id = @aktuelle_gp_solothun AND gemeinde_id IS NULL AND einstellung.mandant_id is NULL;
# Gemeinde Einstellungen für Gesuchsperiode kopieren
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	   einstellung_key, value, NULL, @gesuchsperiode_id, @mandant_id_schwyz, erklaerung
FROM einstellung
WHERE mandant_id = @mandant_id_solothurn AND gesuchsperiode_id = @aktuelle_gp_solothun AND NOT EXISTS(
	SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  @gesuchsperiode_id
		and e1.mandant_id = @mandant_id_schwyz AND e1.einstellung_key = einstellung.einstellung_key
) AND gemeinde_id IS NULL;

INSERT INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
		   UNHEX(REPLACE(UUID(), '-', '')), # id
		   '2018-01-01 00:00:00', # timestamp_erstellt
		   '2018-01-01 00:00:00', # timestamp_mutiert
		   'flyway', # user_erstellt
		   'flyway', # user_mutiert
		   0, # version
		   'FALL_NUMMER', # sequence_type
		   100, # current_value
		   @mandant_id_schwyz);


# Unbekannte Institutionen
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   'Unbekannte Kita' as name,
				   @mandant_id_solothurn as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', ''))
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   'Unbekannte TFO' as name,
				   @mandant_id_solothurn as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', ''))
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   null as gemeinde,
				   '21.0' as hausnummer,
				   'CH' as land,
				   null as organisation,
				   'Bern' as ort,
				   '3022.0' as plz,
				   'Nussbaumstrasse' as strasse,
				   null as zusatzzeile
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', ''))
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   null as gemeinde,
				   '21.0' as hausnummer,
				   'CH' as land,
				   null as organisation,
				   'Bern' as ort,
				   '3022.0' as plz,
				   'Nussbaumstrasse' as strasse,
				   null as zusatzzeile
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', ''))
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
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite)
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'KITA' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', '')) as institution_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000012', '-', ''))
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
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite)
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'TAGESFAMILIEN' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', '')) as institution_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000013', '-', ''))
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   'Unbekannte Tagesschule' as name,
				   @mandant_id_solothurn as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', ''))
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   null as gemeinde,
				   '21.0' as hausnummer,
				   'CH' as land,
				   null as organisation,
				   'Bern' as ort,
				   '3022.0' as plz,
				   'Nussbaumstrasse' as strasse,
				   null as zusatzzeile
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', ''))
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
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'TAGESSCHULE' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', '')) as institution_id,
				   null as institution_stammdaten_betreuungsgutscheine_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite,
				   null as oeffnungszeiten
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000014', '-', ''))
)
LIMIT 1;
