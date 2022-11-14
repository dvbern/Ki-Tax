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

ALTER TABLE einstellung
ADD if not exists erklaerung varchar(4000);

ALTER TABLE einstellung_aud
ADD if not exists erklaerung varchar(4000);

ALTER TABLE application_property
ADD if not exists erklaerung varchar(4000);

ALTER TABLE application_property_aud
ADD if not exists erklaerung varchar(4000);

update einstellung
set erklaerung = 'Auf der Tagesschulanmeldung werden zusätzliche Angaben zu \"Essen\", \"Allergien und Unverträglichkeiten\" sowie \"Notfallnummer\" erfragt.'
WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG'