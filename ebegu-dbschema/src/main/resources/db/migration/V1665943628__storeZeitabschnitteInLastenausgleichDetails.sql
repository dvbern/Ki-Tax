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

CREATE TABLE lastenausgleich_detail_zeitabschnitt (
	lastenausgleich_detail_id BINARY(16) NOT NULL,
	zeitabschnitt_id BINARY(16) NOT NULL
);

CREATE TABLE lastenausgleich_detail_zeitabschnitt_aud (
	rev INTEGER NOT NULL,
	lastenausgleich_detail_id BINARY(16) NOT NULL,
	zeitabschnitt_id BINARY(16) NOT NULL,
	revtype TINYINT,
	PRIMARY KEY (rev, lastenausgleich_detail_id, zeitabschnitt_id)
);

ALTER TABLE lastenausgleich_detail_zeitabschnitt
	ADD CONSTRAINT UK_lastenausgleich_detail_zeitabschnitt_id UNIQUE (zeitabschnitt_id);

ALTER TABLE lastenausgleich_detail_aud
	ADD CONSTRAINT FK_lastenausgleich_detail_rev
		FOREIGN KEY (rev)
			REFERENCES revinfo (rev);

ALTER TABLE lastenausgleich_detail_zeitabschnitt
	ADD CONSTRAINT FK_lastenausgleich_detail_verfuegung_zeitabschnitt_id
		FOREIGN KEY (zeitabschnitt_id)
			REFERENCES verfuegung_zeitabschnitt (id);

ALTER TABLE lastenausgleich_detail_zeitabschnitt
	ADD CONSTRAINT FK_lastenausgleich_detail_id
		FOREIGN KEY (lastenausgleich_detail_id)
			REFERENCES lastenausgleich_detail (id);

ALTER TABLE lastenausgleich_detail_zeitabschnitt_aud
	ADD CONSTRAINT FK_lastenausgleich_detail_zeitabschnitt_rev
		FOREIGN KEY (rev)
			REFERENCES revinfo (rev);

