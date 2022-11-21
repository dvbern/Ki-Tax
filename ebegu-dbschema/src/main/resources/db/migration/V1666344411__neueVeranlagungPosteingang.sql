/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' AS einstellungkey,
			'0' AS VALUE,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

ALTER TABLE mitteilung
	ADD COLUMN steuerdaten_response_id BINARY(16);
ALTER TABLE mitteilung_aud
	ADD COLUMN steuerdaten_response_id BINARY(16);

ALTER TABLE mitteilung
	ADD CONSTRAINT FK_mitteilung_steuerdaten_response_id
		FOREIGN KEY (steuerdaten_response_id)
			REFERENCES steuerdaten_response(id);

INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
					  VALUES (UNHEX(REPLACE('99999999-2222-2222-2222-222222222222', '-', '')), current_timestamp, current_timestamp, 'flyway',
	'flyway',0, null, 'kibon.technical@dvbern.ch', 'kibon', 'kibon', '', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220','-', '')), null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
						  VALUES (UNHEX(REPLACE('99999999-2222-2222-2222-222222222223', '-', '')), current_timestamp, current_timestamp, 'flyway',
							'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', UNHEX(REPLACE('99999999-2222-2222-2222-222222222222', '-', '')), null, null);
