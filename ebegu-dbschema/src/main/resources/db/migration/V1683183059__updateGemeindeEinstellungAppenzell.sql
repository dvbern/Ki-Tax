
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

# gemeinde min erwerbspensum korrekt setzen
update einstellung inner join gemeinde on einstellung.gemeinde_id = gemeinde.id
	inner join mandant on einstellung.mandant_id = mandant.id
set value = '20'
where mandant_identifier = 'APPENZELL_AUSSERRHODEN'
		and einstellung_key like 'GEMEINDE_MIN_ERWERBSPENSUM%';

# gemeinde erwerbspenum korrekt setzen
update einstellung set value = '0'
where einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG'
		and mandant_id = unhex('5b9e6fa4399111eda63db05cda43de9c');

# falsche einstellungen bei mandant appenzell löschen. Das sind periodeneinstellungen, die auch direkt auf
# dem mandanten gespeichert wurden
delete
from einstellung where gemeinde_id is null and mandant_id is not null
		and einstellung_key not like '%gemeinde%'
		and mandant_id = unhex('5b9e6fa4399111eda63db05cda43de9c');



update einstellung set value = '20' where gemeinde_id = unhex('b3e44f85399911eda63db05cda43de9c') and einstellung_key like 'GEMEINDE_MIN_ERWERBSPENSUM%';
update einstellung inner join mandant m on einstellung.mandant_id = m.id set value = '20' where mandant_identifier = 'APPENZELL_AUSSERRHODEN' and einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT'
