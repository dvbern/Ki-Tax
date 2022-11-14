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

-- Unverwendete Spalte
DROP INDEX IX_alle_faelle_view_verantwortlicher_gemeinde_id ON alle_faelle_view;
ALTER TABLE alle_faelle_view drop column verantwortlicher_gemeinde_id;
ALTER TABLE alle_faelle_view drop column verantwortlicher_gemeinde;

-- Problemen mit Nomenklatur
ALTER TABLE alle_faelle_view drop column fallnummer,
ALTER TABLE alle_faelle_view add column fall_nummer VARCHAR(255) NOT NULL,
ALTER TABLE alle_faelle_view drop column eingangsdatum;
ALTER TABLE alle_faelle_view drop column eingangsdatum_stv;
ALTER TABLE alle_faelle_view add column eingangsdatum DATE;
ALTER TABLE alle_faelle_view add column eingangsdatumstv DATE;
DROP INDEX IX_alle_faelle_view_verantwortlicher_bg_id ON alle_faelle_view;
DROP INDEX IX_alle_faelle_view_verantwortlicher_ts_id ON alle_faelle_view;
ALTER TABLE alle_faelle_view drop column verantwortlicher_bg_id;
ALTER TABLE alle_faelle_view drop column verantwortlicher_bg;
ALTER TABLE alle_faelle_view drop column verantwortlicher_ts_id;
ALTER TABLE alle_faelle_view drop column verantwortlicher_ts;
ALTER TABLE alle_faelle_view add column verantwortlicherbgid BINARY(16);
ALTER TABLE alle_faelle_view add column verantwortlicherbg VARCHAR(255);
ALTER TABLE alle_faelle_view add column	verantwortlichertsid BINARY(16);
ALTER TABLE alle_faelle_view add column verantwortlicherts VARCHAR(255);

CREATE INDEX IX_alle_faelle_view_verantwortlicherbg_id ON alle_faelle_view(verantwortlicherbgid);
CREATE INDEX IX_alle_faelle_view_verantwortlicherts_id ON alle_faelle_view(verantwortlichertsid);

-- Datentyp Problem
DROP INDEX IX_alle_faelle_view_besitzer_id ON alle_faelle_view;
ALTER TABLE alle_faelle_view drop column besitzer_id;
ALTER TABLE alle_faelle_view add column besitzer_id BINARY(16);
CREATE INDEX IX_alle_faelle_view_besitzer_id ON alle_faelle_view(besitzer_id);

-- Many To Many Table Creation
CREATE TABLE alle_faelle_view_institution (
	antrag_id     BINARY(16) NOT NULL,
	institution_id BINARY(16) NOT NULL,
	PRIMARY KEY (antrag_id, institution_id)
);

ALTER TABLE alle_faelle_view_institution
	ADD CONSTRAINT FK_alle_faelle_view_institution_id
		FOREIGN KEY (institution_id)
			REFERENCES institution(id);

ALTER TABLE alle_faelle_view_institution
	ADD CONSTRAINT FK_alle_faelle_view_antrag_id
		FOREIGN KEY (antrag_id)
			REFERENCES alle_faelle_view(antrag_id);

CREATE INDEX IX_alle_faelle_view_antrag_id ON alle_faelle_view_institution(antrag_id);
CREATE INDEX IX_alle_faelle_view_institution_id ON alle_faelle_view_institution(institution_id);