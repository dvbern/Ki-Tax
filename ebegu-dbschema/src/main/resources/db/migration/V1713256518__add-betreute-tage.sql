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

ALTER TABLE betreuungsmitteilung_pensum
	ADD betreute_tage DECIMAL(19, 2);
ALTER TABLE betreuungsmitteilung_pensum_aud
	ADD betreute_tage DECIMAL(19, 2);

ALTER TABLE betreuungspensum
	ADD betreute_tage DECIMAL(19, 2);
ALTER TABLE betreuungspensum_aud
	ADD betreute_tage DECIMAL(19, 2);

ALTER TABLE betreuungspensum_abweichung
	ADD betreute_tage DECIMAL(19, 2);
ALTER TABLE betreuungspensum_abweichung_aud
	ADD betreute_tage DECIMAL(19, 2);
