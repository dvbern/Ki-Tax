/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

update familiensituation
set keine_mahlzeitenverguenstigung_beantragt = TRUE
where familiensituation.id in (
	select familiensituation.id from familiensituation
									 join familiensituation_container on familiensituation.id = familiensituation_container.familiensituationja_id
									 join gesuch on familiensituation_container.id = gesuch.familiensituation_container_id
									 join dossier d ON gesuch.dossier_id = d.id
									 join fall f ON d.fall_id = f.id
									 join mandant on f.mandant_id = mandant.id
	where mandant_identifier = 'SOLOTHURN' or mandant_identifier = 'LUZERN'
)