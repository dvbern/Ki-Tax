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

SET @mandant_schwyz_id = (SELECT id FROM mandant WHERE mandant_identifier = 'SCHWYZ');

UPDATE einstellung
SET value = 'SCHWYZ'
WHERE gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_schwyz_id) AND
	  einstellung_key = 'FINANZIELLE_SITUATION_TYP';
