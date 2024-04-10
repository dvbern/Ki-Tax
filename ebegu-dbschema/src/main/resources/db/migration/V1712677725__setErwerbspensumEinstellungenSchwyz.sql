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

UPDATE einstellung
SET value = 'SCHWYZ'
WHERE einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM'
	AND gesuchsperiode_id IN
		(SELECT gesuchsperiode.id FROM gesuchsperiode
									   JOIN mandant m ON gesuchsperiode.mandant_id = m.id
		 WHERE mandant_identifier = 'SCHWYZ');


UPDATE einstellung
SET value = '0'
WHERE einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG'
	AND gesuchsperiode_id IN
		(SELECT gesuchsperiode.id FROM gesuchsperiode
									   JOIN mandant m ON gesuchsperiode.mandant_id = m.id
		 WHERE mandant_identifier = 'SCHWYZ');

