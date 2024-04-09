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
update einstellung
set value = 'KEINE'
where einstellung_key = 'FACHSTELLEN_TYP'
and gesuchsperiode_id in
(select gesuchsperiode.id from gesuchsperiode
    join mandant m on gesuchsperiode.mandant_id = m.id
where mandant_identifier = 'SCHWYZ');

update einstellung
set value = 'KEINE'
where einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE'
  and gesuchsperiode_id in
      (select gesuchsperiode.id from gesuchsperiode
       join mandant m on gesuchsperiode.mandant_id = m.id
       where mandant_identifier = 'SCHWYZ');

update einstellung
set value = 'true'
where einstellung_key = 'ZEMIS_DISABLED'
  and gesuchsperiode_id in
      (select gesuchsperiode.id from gesuchsperiode
       join mandant m on gesuchsperiode.mandant_id = m.id
       where mandant_identifier = 'SCHWYZ');


