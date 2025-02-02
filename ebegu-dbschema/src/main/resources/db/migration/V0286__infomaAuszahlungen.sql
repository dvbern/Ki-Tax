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

ALTER TABLE auszahlungsdaten ADD infoma_kreditorennummer VARCHAR(255);
ALTER TABLE auszahlungsdaten_aud ADD infoma_kreditorennummer VARCHAR(255);

alter table auszahlungsdaten CHANGE iban iban VARCHAR(34) NULL;

ALTER TABLE mandant ADD next_infoma_belegnummer BIGINT NOT NULL DEFAULT 1;
ALTER TABLE mandant_aud ADD next_infoma_belegnummer BIGINT NULL;
ALTER TABLE mandant_aud ADD activated BIT NULL;

UPDATE mandant SET next_infoma_belegnummer = 200001 WHERE mandant_identifier = 'LUZERN';

