/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '' as name,
				   UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', '')) as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
) AS tmp
WHERE NOT EXISTS(
		SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '' as name,
				   UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', '')) as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
) AS tmp
WHERE NOT EXISTS(
		SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')) as id,
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
		SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')) as id,
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
		SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'KITA' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', '')) as institution_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite
) AS tmp
WHERE NOT EXISTS(
		SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000009', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'TAGESFAMILIEN' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', '')) as institution_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite
) AS tmp
WHERE NOT EXISTS(
		SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   'Unbekannte Tagesschule' as name,
				   UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', '')) as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
) AS tmp
WHERE NOT EXISTS(
		SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')) as id,
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
		SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', ''))
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
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'TAGESSCHULE' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', '')) as institution_id,
				   null as institution_stammdaten_betreuungsgutscheine_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite,
				   null as oeffnungszeiten
) AS tmp
WHERE NOT EXISTS(
		SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000011', '-', ''))
	)
LIMIT 1;
