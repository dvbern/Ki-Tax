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

CREATE TABLE IF NOT EXISTS alle_faelle_view_kind (
    kind_id BINARY(16) NOT NULL,
	antrag_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (kind_id)
);

ALTER TABLE alle_faelle_view_kind
	ADD CONSTRAINT FK_alle_faelle_view_kind_antrag_id
		FOREIGN KEY (antrag_id)
			REFERENCES alle_faelle_view(antrag_id);


CREATE INDEX IX_alle_faelle_view_kind_kind_id ON alle_faelle_view_kind(kind_id);

alter table alle_faelle_view
drop column kinder;