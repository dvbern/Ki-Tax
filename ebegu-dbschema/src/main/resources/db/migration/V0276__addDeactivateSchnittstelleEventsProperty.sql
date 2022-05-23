/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

# bern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
								  version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('d4a81288-5cc7-11ec-93ed-f4390979fa3e', '-', '')),
		UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), '2021-12-14 12:00:00', '2021-12-14 12:00:00',
		'flyway', 'flyway', 0, NULL, 'SCHNITTSTELLE_EVENTS_AKTIVIERT', 'true');

# solothurn
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
								  version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('5d759f85-5cc8-11ec-93ed-f4390979fa3e', '-', '')),
		UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', '')), '2021-12-14 12:00:00', '2021-12-14 12:00:00',
		'flyway', 'flyway', 0, NULL, 'SCHNITTSTELLE_EVENTS_AKTIVIERT', 'false');

# luzern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
								  version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('532c58af-5cc8-11ec-93ed-f4390979fa3e', '-', '')),
		UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')), '2021-12-14 12:00:00', '2021-12-14 12:00:00',
		'flyway', 'flyway', 0, NULL, 'SCHNITTSTELLE_EVENTS_AKTIVIERT', 'true');
