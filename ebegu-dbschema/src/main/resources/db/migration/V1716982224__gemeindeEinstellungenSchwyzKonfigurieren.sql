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
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' and mandant_id = @mandant_id_schwyz;

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' and mandant_id = @mandant_id_schwyz;

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' and mandant_id = @mandant_id_schwyz;

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' and mandant_id = @mandant_id_schwyz;

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' and mandant_id = @mandant_id_schwyz;

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED'and mandant_id = @mandant_id_schwyz;

update einstellung set value = '20'
where einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' and mandant_id = @mandant_id_schwyz;

update einstellung set value = '20'
where einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT'and mandant_id = @mandant_id_schwyz;

update einstellung set value = 'false'
where einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG'and mandant_id = @mandant_id_schwyz;
