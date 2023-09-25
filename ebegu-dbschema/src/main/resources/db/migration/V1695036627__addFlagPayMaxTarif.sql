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
alter table bgcalculation_result
add column verguenstigung_gewuenscht bit
not null default true;

alter table bgcalculation_result_aud
add column verguenstigung_gewuenscht bit;

# bg asiv results updaten
update bgcalculation_result
set verguenstigung_gewuenscht = false
where id in (select verfuegung_zeitabschnitt.bg_calculation_result_asiv_id as id from familiensituation
        join familiensituation_container fc on familiensituation.id = fc.familiensituationja_id
        join gesuch on fc.id = gesuch.familiensituation_container_id
        join kind_container on gesuch.id = kind_container.gesuch_id
        join betreuung on kind_container.id = betreuung.kind_id
        join verfuegung on betreuung.id = verfuegung.betreuung_id
        join verfuegung_zeitabschnitt on verfuegung.id = verfuegung_zeitabschnitt.verfuegung_id
    where familiensituation.verguenstigung_gewuenscht = false);

# bg gemeinde result updaten
update bgcalculation_result
set verguenstigung_gewuenscht = false
where id in (
    select verfuegung_zeitabschnitt.bg_calculation_result_gemeinde_id as id from familiensituation
        join familiensituation_container fc on familiensituation.id = fc.familiensituationja_id
        join gesuch on fc.id = gesuch.familiensituation_container_id
        join kind_container on gesuch.id = kind_container.gesuch_id
        join betreuung on kind_container.id = betreuung.kind_id
        join verfuegung on betreuung.id = verfuegung.betreuung_id
        join verfuegung_zeitabschnitt on verfuegung.id = verfuegung_zeitabschnitt.verfuegung_id
    where familiensituation.verguenstigung_gewuenscht = false);

# ts asiv
update bgcalculation_result
set verguenstigung_gewuenscht = false
where id in (select verfuegung_zeitabschnitt.bg_calculation_result_asiv_id as id from familiensituation
        join familiensituation_container fc on familiensituation.id = fc.familiensituationja_id
        join gesuch on fc.id = gesuch.familiensituation_container_id
        join kind_container on gesuch.id = kind_container.gesuch_id
        join anmeldung_tagesschule on kind_container.id = anmeldung_tagesschule.kind_id
        join verfuegung on anmeldung_tagesschule.id = verfuegung.anmeldung_tagesschule_id
        join verfuegung_zeitabschnitt on verfuegung.id = verfuegung_zeitabschnitt.verfuegung_id
where familiensituation.verguenstigung_gewuenscht = false);

# bg gemeinde result updaten
update bgcalculation_result
set verguenstigung_gewuenscht = false
where id in (
    select verfuegung_zeitabschnitt.bg_calculation_result_gemeinde_id as id from familiensituation
        join familiensituation_container fc on familiensituation.id = fc.familiensituationja_id
        join gesuch on fc.id = gesuch.familiensituation_container_id
        join kind_container on gesuch.id = kind_container.gesuch_id
        join anmeldung_tagesschule on kind_container.id = anmeldung_tagesschule.kind_id
        join verfuegung on anmeldung_tagesschule.id = verfuegung.anmeldung_tagesschule_id
        join verfuegung_zeitabschnitt on verfuegung.id = verfuegung_zeitabschnitt.verfuegung_id
where familiensituation.verguenstigung_gewuenscht = false);
