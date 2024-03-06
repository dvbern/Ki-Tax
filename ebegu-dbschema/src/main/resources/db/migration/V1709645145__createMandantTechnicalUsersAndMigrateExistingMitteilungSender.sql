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

SET @mutatio_user_id_be = UNHEX(REPLACE('88888888-2222-2222-2222-222222222222', '-', ''));
SET @mutation_user_id_ar = UNHEX(REPLACE('88888888-2223-2222-2222-222222222222', '-', ''));
SET @mutation_user_id_lu = UNHEX(REPLACE('88888888-2224-2222-2222-222222222222', '-', ''));
SET @mutation_user_id_solothurn = UNHEX(REPLACE('88888888-2225-2222-2222-222222222222', '-', ''));
SET @mutation_user_id_sz = UNHEX(REPLACE('88888888-2226-2222-2222-222222222222', '-', ''));

SET @veranlagung_user_id_be = UNHEX(REPLACE('99999999-2222-2222-2222-222222222222', '-', ''));
SET @veranlagung_user_id_ar = UNHEX(REPLACE('99999999-2223-2222-2222-222222222222', '-', ''));
SET @veranlagung_user_id_lu = UNHEX(REPLACE('99999999-2224-2222-2222-222222222222', '-', ''));
SET @veranlagung_user_id_so = UNHEX(REPLACE('99999999-2225-2222-2222-222222222222', '-', ''));
SET @veranlagung_user_id_sz = UNHEX(REPLACE('99999999-2226-2222-2222-222222222222', '-', ''));

SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));
SET @mandant_id_luzern = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));


/* Appenzell Ausserrhoden */
/* Mutation */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@mutation_user_id_ar, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'betreuungEvent.ar@dvbern.ch', 'BetreuungsEvent', 'Mutation AR', '', @mandant_id_ar, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @mutation_user_id_ar, null, null);

/* Veranlagung */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@veranlagung_user_id_ar, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'kibon.technical.ar@dvbern.ch', 'kibon', 'kibon AR', '', @mandant_id_ar, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @veranlagung_user_id_ar, null, null);

/* Existierende Fälle migrieren. Muss für Veranlagungsitteilung nicht gemacht werden, weil die Stand heute nur in Bern aktiv ist */
UPDATE mitteilung
	INNER JOIN betreuung ON mitteilung.betreuung_id = betreuung.id
	INNER JOIN kind_container kc on betreuung.kind_id = kc.id
	INNER JOIN gesuch ON kc.gesuch_id = gesuch.id
	INNER JOIN dossier ON gesuch.dossier_id = dossier.id
	INNER JOIN fall ON dossier.fall_id = fall.id
SET mitteilung.sender_id = @mutation_user_id_ar
WHERE mitteilung.sender_id = @mutatio_user_id_be AND fall.mandant_id = @mandant_id_ar;




/* Luzern */
/* Mutation */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@mutation_user_id_lu, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'betreuungEvent.lu@dvbern.ch', 'BetreuungsEvent', 'Mutation LU', '', @mandant_id_luzern, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @mutation_user_id_lu, null, null);

/* Veranlagung */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@veranlagung_user_id_lu, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'kibon.technical.lu@dvbern.ch', 'kibon', 'kibon LU', '', @mandant_id_luzern, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @veranlagung_user_id_lu, null, null);

/* Existierende Fälle migrieren. Muss für Veranlagungsitteilung nicht gemacht werden, weil die Stand heute nur in Bern aktiv ist */
UPDATE mitteilung
	INNER JOIN betreuung ON mitteilung.betreuung_id = betreuung.id
	INNER JOIN kind_container kc on betreuung.kind_id = kc.id
	INNER JOIN gesuch ON kc.gesuch_id = gesuch.id
	INNER JOIN dossier ON gesuch.dossier_id = dossier.id
	INNER JOIN fall ON dossier.fall_id = fall.id
SET mitteilung.sender_id = @mutation_user_id_lu
WHERE mitteilung.sender_id = @mutatio_user_id_be AND fall.mandant_id = @mandant_id_luzern;



/* Solothurn */
/* Mutation */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@mutation_user_id_solothurn, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'betreuungEvent.so@dvbern.ch', 'BetreuungsEvent', 'Mutation SO', '', @mandant_id_solothurn, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @mutation_user_id_solothurn, null, null);

/* Veranlagung */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@veranlagung_user_id_so, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'kibon.technical.so@dvbern.ch', 'kibon', 'kibon SO', '', @mandant_id_solothurn, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @veranlagung_user_id_so, null, null);

/* Existierende Fälle migrieren. Muss für Veranlagungsitteilung nicht gemacht werden, weil die Stand heute nur in Bern aktiv ist */
UPDATE mitteilung
	INNER JOIN betreuung ON mitteilung.betreuung_id = betreuung.id
	INNER JOIN kind_container kc on betreuung.kind_id = kc.id
	INNER JOIN gesuch ON kc.gesuch_id = gesuch.id
	INNER JOIN dossier ON gesuch.dossier_id = dossier.id
	INNER JOIN fall ON dossier.fall_id = fall.id
SET mitteilung.sender_id = @mutation_user_id_solothurn
WHERE mitteilung.sender_id = @mutatio_user_id_be AND fall.mandant_id = @mandant_id_solothurn;


/* Schwyz */
/* Mutation */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@mutation_user_id_sz, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'betreuungEvent.sz@dvbern.ch', 'BetreuungsEvent', 'Mutation SZ', '', @mandant_id_schwyz, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @mutation_user_id_sz, null, null);

/* Veranlagung */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@veranlagung_user_id_sz, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'kibon.technical.sz@dvbern.ch', 'kibon', 'kibon SZ', '', @mandant_id_schwyz, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @veranlagung_user_id_sz, null, null);

/* Existierende Fälle migrieren. Muss für Veranlagungsitteilung nicht gemacht werden, weil die Stand heute nur in Bern aktiv ist */
UPDATE mitteilung
	INNER JOIN betreuung ON mitteilung.betreuung_id = betreuung.id
	INNER JOIN kind_container kc on betreuung.kind_id = kc.id
	INNER JOIN gesuch ON kc.gesuch_id = gesuch.id
	INNER JOIN dossier ON gesuch.dossier_id = dossier.id
	INNER JOIN fall ON dossier.fall_id = fall.id
SET mitteilung.sender_id = @mutation_user_id_sz
WHERE mitteilung.sender_id = @mutatio_user_id_be AND fall.mandant_id = @mandant_id_schwyz;


