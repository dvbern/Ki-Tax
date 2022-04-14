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
INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = 'FREIWILLIGER_KINDERGARTEN', einstellung.user_mutiert='flyway',
    einstellung.timestamp_mutiert=now(), einstellung.version=einstellung.version+1
WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND m.mandant_identifier = 'LUZERN';

