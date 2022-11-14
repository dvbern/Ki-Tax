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

# rename mahlzeiten to auszahlungsdaten
alter table familiensituation change auszahlungsdaten_mahlzeiten_id auszahlungsdaten_id binary(16);
alter table familiensituation_aud change auszahlungsdaten_mahlzeiten_id auszahlungsdaten_id binary(16);

alter table familiensituation change abweichende_zahlungsadresse_mahlzeiten abweichende_zahlungsadresse BIT NOT NULL DEFAULT FALSE;
alter table familiensituation_aud change abweichende_zahlungsadresse_mahlzeiten abweichende_zahlungsadresse BIT;

# copy auszahlungsdaten infoma to auszahlugnsdaten
update familiensituation set auszahlungsdaten_id=auszahlungsdaten_infoma_id
where auszahlungsdaten_infoma_id is not null and auszahlungsdaten_id is null;

update familiensituation_aud set auszahlungsdaten_id=auszahlungsdaten_infoma_id
where auszahlungsdaten_infoma_id is not null and auszahlungsdaten_id is null;

# copy flag only when true
update familiensituation set abweichende_zahlungsadresse = abweichende_zahlungsadresse_infoma
where abweichende_zahlungsadresse_infoma = true and abweichende_zahlungsadresse = false;

update familiensituation_aud set abweichende_zahlungsadresse = abweichende_zahlungsadresse_infoma
where abweichende_zahlungsadresse_infoma = true and abweichende_zahlungsadresse = false;

# delete column auzahlungsdaten_infoma
alter table familiensituation drop column abweichende_zahlungsadresse_infoma;
alter table familiensituation_aud drop column abweichende_zahlungsadresse_infoma;