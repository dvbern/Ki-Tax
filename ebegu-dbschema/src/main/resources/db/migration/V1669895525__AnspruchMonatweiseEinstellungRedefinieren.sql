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

UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'FKJV_ANSPRUCH_MONATSWEISE' AND
		gesuchsperiode_id IN (SELECT id
							  FROM gesuchsperiode
							  WHERE mandant_id = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', '')));

UPDATE einstellung
SET einstellung_key = 'ANSPRUCH_MONATSWEISE'
WHERE einstellung_key = 'FKJV_ANSPRUCH_MONATSWEISE';
