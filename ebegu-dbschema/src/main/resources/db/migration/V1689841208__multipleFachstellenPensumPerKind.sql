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

# Add Constraint on Pensum Fachstelle to kind
ALTER TABLE ebegu.pensum_fachstelle
	ADD COLUMN kind_id BINARY(16);
ALTER TABLE ebegu.pensum_fachstelle_aud
	ADD COLUMN kind_id BINARY(16);

ALTER TABLE ebegu.pensum_fachstelle
	ADD CONSTRAINT FK_pensum_fachstelle_kind_id FOREIGN KEY (kind_id) REFERENCES ebegu.kind(id);

# Set kind_id for all existing pensum_fachstelle
UPDATE ebegu.pensum_fachstelle INNER JOIN kind ON pensum_fachstelle.id = kind.pensum_fachstelle_id
SET pensum_fachstelle.kind_id = kind.id
WHERE TRUE;

# DROP one-to-one FK to pensum_fachstelle on kind
ALTER TABLE kind DROP CONSTRAINT FK_kind_pensum_fachstelle_id;
ALTER TABLE kind DROP COLUMN pensum_fachstelle_id;
ALTER TABLE ebegu.kind_aud DROP COLUMN pensum_fachstelle_id;