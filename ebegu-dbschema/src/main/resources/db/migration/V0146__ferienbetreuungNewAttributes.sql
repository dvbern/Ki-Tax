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

ALTER TABLE ferienbetreuung_angaben ADD COLUMN seit_wann_ferienbetreuungen VARCHAR(255);
ALTER TABLE ferienbetreuung_angaben ADD COLUMN vermerk_auszahlung VARCHAR(255);
ALTER TABLE ferienbetreuung_angaben ADD COLUMN betreuung_erfolgt_tagsueber BIT;
ALTER TABLE ferienbetreuung_angaben ADD COLUMN betreuung_durch_personen_mit_erfahrung BIT;
ALTER TABLE ferienbetreuung_angaben ADD COLUMN anzahl_kinder_angemessen BIT;
ALTER TABLE ferienbetreuung_angaben ADD COLUMN betreuungsschluessel DECIMAL(19, 2);

ALTER TABLE ferienbetreuung_angaben DROP COLUMN aufwand_betreuungspersonal;
ALTER TABLE ferienbetreuung_angaben DROP COLUMN zusaetzlicher_aufwand_leitung_admin;

ALTER TABLE ferienbetreuung_angaben_aud ADD COLUMN seit_wann_ferienbetreuungen VARCHAR(255);
ALTER TABLE ferienbetreuung_angaben_aud ADD COLUMN vermerk_auszahlung VARCHAR(255);
ALTER TABLE ferienbetreuung_angaben_aud ADD COLUMN betreuung_erfolgt_tagsueber BIT;
ALTER TABLE ferienbetreuung_angaben_aud ADD COLUMN betreuung_durch_personen_mit_erfahrung BIT;
ALTER TABLE ferienbetreuung_angaben_aud ADD COLUMN anzahl_kinder_angemessen BIT;
ALTER TABLE ferienbetreuung_angaben_aud ADD COLUMN betreuungsschluessel DECIMAL(19, 2);

ALTER TABLE ferienbetreuung_angaben_aud DROP COLUMN aufwand_betreuungspersonal;
ALTER TABLE ferienbetreuung_angaben_aud DROP COLUMN zusaetzlicher_aufwand_leitung_admin;



