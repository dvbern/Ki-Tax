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
#  add field to erweiterte_betreuung
ALTER TABLE erweiterte_betreuung
	ADD COLUMN anspruch_fachstelle_wenn_pensum_unterschritten BIT NOT NULL DEFAULT false;

ALTER TABLE erweiterte_betreuung_aud
	ADD COLUMN anspruch_fachstelle_wenn_pensum_unterschritten BIT;
