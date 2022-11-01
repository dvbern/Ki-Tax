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
