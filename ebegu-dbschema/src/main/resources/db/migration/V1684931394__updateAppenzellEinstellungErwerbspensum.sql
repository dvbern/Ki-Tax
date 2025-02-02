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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

update einstellung inner join mandant on einstellung.mandant_id = mandant.id
set value = '20'
where mandant.mandant_identifier = 'APPENZELL_AUSSERRHODEN'
		and einstellung_key like 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT';


update einstellung
set einstellung.value = '20'
where einstellung.id in (select einstellung.id from gesuchsperiode
         join einstellung on gesuchsperiode.id = einstellung.gesuchsperiode_id
where gesuchsperiode.mandant_id = (select id from mandant where mandant_identifier = 'APPENZELL_AUSSERRHODEN')
		and einstellung_key like 'MIN_ERWERBSPENSUM_EINGESCHULT');

