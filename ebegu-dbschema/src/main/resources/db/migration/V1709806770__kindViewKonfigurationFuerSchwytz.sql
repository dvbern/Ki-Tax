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

UPDATE einstellung set value = 'UNABHAENGING' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_id and gemeinde_id is null;
UPDATE einstellung set value = 'PRIMARSTUFE' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' and gemeinde_id is null;
