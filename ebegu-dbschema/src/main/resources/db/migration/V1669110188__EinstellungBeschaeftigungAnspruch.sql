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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

update einstellung
set einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM',
    version = version + 1,
    value = 'ABHAENGING',
    erklaerung = 'Die Einstellung kann 3 Werte haben ABHAENGING, UNABHAENGING oder MINIMUM. Abhängig = Der Anspruch hängt vom Beschäftigungspensum ab (wie in Bern) Unabhängig = Der Anspruch hängt nicht vom Beschäftigungspensum ab (wie in Solothurn) Minimal = Es muss ein minimum an Beschäftigungspensum erreicht werden um einen Anspruch zu erhalten (wie in Grenchen)'
where einstellung_key = 'ANSPRUCH_UNABHAENGIG_BESCHAEFTIGUNGPENSUM'
and value = 'false';

update einstellung
set einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM',
	version = version + 1,
	value = 'UNABHAENGING',
	erklaerung = 'Die Einstellung kann 3 Werte haben ABHAENGING, UNABHAENGING oder MINIMUM. Abhängig = Der Anspruch hängt vom Beschäftigungspensum ab (wie in Bern) Unabhängig = Der Anspruch hängt nicht vom Beschäftigungspensum ab (wie in Solothurn) Minimal = Es muss ein minimum an Beschäftigungspensum erreicht werden um einen Anspruch zu erhalten (wie in Grenchen)'
where einstellung_key = 'ANSPRUCH_UNABHAENGIG_BESCHAEFTIGUNGPENSUM'
		and value = 'true'