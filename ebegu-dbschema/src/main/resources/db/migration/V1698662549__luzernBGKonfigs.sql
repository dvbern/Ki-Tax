/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

update einstellung set value = 'true'
where gesuchsperiode_id =
      (select gesuchsperiode.id from gesuchsperiode
          join mandant on gesuchsperiode.mandant_id = mandant.id
           where mandant_identifier = 'LUZERN' and gesuchsperiode.gueltig_ab = '2023-08-01')
and einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN';

update einstellung set value =  160
    where gesuchsperiode_id in (
        select gesuchsperiode.id from gesuchsperiode
        join mandant on gesuchsperiode.mandant_id = mandant.id
        where mandant_identifier = 'LUZERN') and
    einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG';

update einstellung set value =  130
where gesuchsperiode_id in (
    select gesuchsperiode.id from gesuchsperiode
    join mandant on gesuchsperiode.mandant_id = mandant.id
    where mandant_identifier = 'LUZERN') and
einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG';

update einstellung set value =  130
where gesuchsperiode_id in (
    select gesuchsperiode.id from gesuchsperiode
    join mandant on gesuchsperiode.mandant_id = mandant.id
    where mandant_identifier = 'LUZERN') and
einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG';

update einstellung set value =  16.3
where gesuchsperiode_id in (
    select gesuchsperiode.id from gesuchsperiode
    join mandant on gesuchsperiode.mandant_id = mandant.id
    where mandant_identifier = 'LUZERN') and
einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD';

update einstellung set value =  12.4
where gesuchsperiode_id in (
    select gesuchsperiode.id from gesuchsperiode
    join mandant on gesuchsperiode.mandant_id = mandant.id
    where mandant_identifier = 'LUZERN') and
einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD';

update einstellung set value =  12.4
where gesuchsperiode_id in (
    select gesuchsperiode.id from gesuchsperiode
    join mandant on gesuchsperiode.mandant_id = mandant.id
    where mandant_identifier = 'LUZERN') and
einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD';



