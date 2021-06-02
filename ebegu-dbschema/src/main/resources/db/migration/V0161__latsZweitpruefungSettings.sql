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

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, value)
	VALUES ((SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))), NOW(), NOW(), 'flyway', 'flyway', 0, 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE', '0.5');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, value)
	VALUES ((SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))), NOW(), NOW(), 'flyway', 'flyway', 0, 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR', '0.5');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, value)
	VALUES ((SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))), NOW(), NOW(), 'flyway', 'flyway', 0, 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE', '100');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, value)
	VALUES ((SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))), NOW(), NOW(), 'flyway', 'flyway', 0, 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR', '100');