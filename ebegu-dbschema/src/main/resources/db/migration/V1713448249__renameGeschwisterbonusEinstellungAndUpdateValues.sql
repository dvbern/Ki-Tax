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
SET einstellung_key = 'GESCHWISTERNBONUS_TYP'
WHERE einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT';

UPDATE einstellung
SET value = 'LUZERN'
WHERE einstellung_key = 'GESCHWISTERNBONUS_TYP' AND value = 'true';

UPDATE einstellung
SET value = 'NONE'
WHERE einstellung_key = 'GESCHWISTERNBONUS_TYP' AND value = 'false';

UPDATE einstellung
SET erklaerung = 'Welcher Geschwisternbonus-Typ soll aktiviert werden. Mögliche Werte sind LUZERN, SCHWYZ und NONE. Mit NONE wird kein Geschwisternbonus aktiviert'
WHERE einstellung_key = 'GESCHWISTERNBONUS_TYP';

UPDATE einstellung
SET value = 'SCHWYZ'
WHERE einstellung_key = 'GESCHWISTERNBONUS_TYP' AND
	  gesuchsperiode_id IN
	  (SELECT gesuchsperiode.id
	   FROM gesuchsperiode
			JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	   WHERE mandant_identifier = 'SCHWYZ');
