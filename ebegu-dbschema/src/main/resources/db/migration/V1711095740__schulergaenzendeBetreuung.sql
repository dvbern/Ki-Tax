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
SET @gesuchsperiode_id = UNHEX(REPLACE('1b0ed338-b3d2-11ee-829a-0242ac160002', '-', ''));

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'SCHULERGAENZENDE_BETREUUNGEN' AS einstellungkey,
			'false' AS value,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'SCHULERGAENZENDE_BETREUUNGEN' and gemeinde_id is null;


alter table betreuungspensum add betreuung_in_ferienzeit BIT;
alter table betreuungspensum_aud add betreuung_in_ferienzeit BIT;
alter table betreuungsmitteilung_pensum add betreuung_in_ferienzeit BIT;
alter table betreuungsmitteilung_pensum_aud add betreuung_in_ferienzeit BIT;
