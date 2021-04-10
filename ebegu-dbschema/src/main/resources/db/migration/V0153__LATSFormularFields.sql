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

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN IF NOT EXISTS
	tagesschule_teilweise_geschlossen BIT(1) NULL DEFAULT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN IF NOT EXISTS
	tagesschule_teilweise_geschlossen BIT(1) NULL DEFAULT NULL;


ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN IF NOT EXISTS
	rueckerstattungen_elterngebuehren_schliessung DECIMAL(19,2) NULL DEFAULT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN IF NOT EXISTS
	rueckerstattungen_elterngebuehren_schliessung DECIMAL(19,2) NULL DEFAULT NULL;


ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN IF NOT EXISTS
	erste_rate_ausbezahlt DECIMAL(19,2) NULL DEFAULT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN IF NOT EXISTS
	erste_rate_ausbezahlt DECIMAL(19,2) NULL DEFAULT NULL;


ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN IF NOT EXISTS
	ueberschuss_erzielt BIT(1) NULL DEFAULT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN IF NOT EXISTS
	ueberschuss_erzielt BIT(1) NULL DEFAULT NULL;


ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN IF NOT EXISTS
	ueberschuss_verwendung VARCHAR(255) NULL DEFAULT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN IF NOT EXISTS
	ueberschuss_verwendung VARCHAR(255) NULL DEFAULT NULL;