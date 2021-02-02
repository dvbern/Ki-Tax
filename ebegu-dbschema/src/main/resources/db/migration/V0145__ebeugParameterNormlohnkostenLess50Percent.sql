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

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gesuchsperiode_id)
 ( SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) as id, NOW() as timestamp_erstellt, NOW() as timestamp_mutiert, 'ebegu' as user_erstellt, 'ebegu' as user_mutiert, '0' as version, 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' as einstellungkey, 5.2 as value, id as gesuchsperiode_id FROM gesuchsperiode);
