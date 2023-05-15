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
alter table gemeinde_stammdaten_gesuchsperiode_aud
drop column if exists merkblatt_anmeldung_tagesschule_de;

alter table gemeinde_stammdaten_gesuchsperiode_aud
drop column if exists merkblatt_anmeldung_tagesschule_fr;

alter table gesuchsperiode_aud
drop column if exists verfuegung_erlaeuterungen_de;

alter table gesuchsperiode_aud
drop column if exists verfuegung_erlaeuterungen_fr;

alter table gesuchsperiode_aud
drop column if exists vorlage_merkblatt_ts_de;

alter table gesuchsperiode_aud
drop column if exists vorlage_merkblatt_ts_fr;

alter table gesuchsperiode_aud
drop column if exists vorlage_verfuegung_lats_de;

alter table gesuchsperiode_aud
drop column if exists vorlage_verfuegung_lats_fr;

alter table gesuchsperiode_aud
drop column if exists vorlage_verfuegung_ferienbetreuung_de;

alter table gesuchsperiode_aud
drop column if exists vorlage_verfuegung_ferienbetreuung_fr;




