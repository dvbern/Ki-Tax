/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

alter table bgcalculation_result add column sozialhilfe_akzeptiert bit not null default false;
alter table bgcalculation_result_aud add column sozialhilfe_akzeptiert bit;

-- Neues Flag muss korrekt gesetzt werden.
-- Das Update erfolgt in mehreren Schritten:
-- Zuerst werden alle Geminden ohne Sozilahilfe Zeiträume aktualisiert. Es gilt folgende Regel:
--   - Das Flag wird in allen Zeitabschnitten der Verfügung auf true gestzt, wenn FinSit Akzeptiert und Sozialhilfebezüger
--   - 4 Update Querys (1x Kita BGCalcAsiv, 1x Kita BGCalcGemiende, 1x TS BGCalcAsiv und 1x TS BGCalcGemeinde)

-- Danach werden alle Geminden mit Sozilahilfe Zeiträume aktualisiert. Es gilt folgende Regel:
--   - Das Flag wird auf true gestzt, wenn FinSit Akzeptiert und Sozialhilfebezüger aber nur in diesem Zeitabschnitten in welcher
--     die Gültigkeits des Zeitabschnittes mit der Gültigkeit des Sozialhilfezeitraums überlappt
--   - 4 Update Querys (1x Kita BGCalcAsiv, 1x Kita BGCalcGemiende, 1x TS BGCalcAsiv und 1x TS BGCalcGemeinde)

-- gemeinden ohne soz-zeiträume
-- -- kita
-- -- -- asiv
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_asiv_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join betreuung on verfuegung.betreuung_id = betreuung.id
                                            join kind_container on betreuung.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            left join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and sozialhilfe_zeitraum_container.id is null
);

-- -- -- gemeinde
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_gemeinde_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join betreuung on verfuegung.betreuung_id = betreuung.id
                                            join kind_container on betreuung.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            left join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and sozialhilfe_zeitraum_container.id is null);

-- -- ts
-- -- -- asiv
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_asiv_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join anmeldung_tagesschule on verfuegung.anmeldung_tagesschule_id = anmeldung_tagesschule.id
                                            join kind_container on anmeldung_tagesschule.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            left join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and sozialhilfe_zeitraum_container.id is null
);

-- -- -- gemeinde
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_gemeinde_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join anmeldung_tagesschule on verfuegung.anmeldung_tagesschule_id = anmeldung_tagesschule.id
                                            join kind_container on anmeldung_tagesschule.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            left join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and sozialhilfe_zeitraum_container.id is null);

-- gemeinden mit sozzeiträume
-- -- kita
-- -- -- asiv
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_asiv_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join betreuung on verfuegung.betreuung_id = betreuung.id
                                            join kind_container on betreuung.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
                                            join sozialhilfe_zeitraum on sozialhilfe_zeitraum_container.sozialhilfe_zeitraumja_id = sozialhilfe_zeitraum.id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and (
            (verfuegung_zeitabschnitt.gueltig_ab >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_ab <= sozialhilfe_zeitraum.gueltig_bis) or
            (verfuegung_zeitabschnitt.gueltig_bis >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_bis <= sozialhilfe_zeitraum.gueltig_bis))
);

-- -- -- gemeinde
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_gemeinde_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join betreuung on verfuegung.betreuung_id = betreuung.id
                                            join kind_container on betreuung.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
                                            join sozialhilfe_zeitraum on sozialhilfe_zeitraum_container.sozialhilfe_zeitraumja_id = sozialhilfe_zeitraum.id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and (
            (verfuegung_zeitabschnitt.gueltig_ab >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_ab <= sozialhilfe_zeitraum.gueltig_bis) or
            (verfuegung_zeitabschnitt.gueltig_bis >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_bis <= sozialhilfe_zeitraum.gueltig_bis))
);

-- -- ts
-- -- -- asiv
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_asiv_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join anmeldung_tagesschule on verfuegung.anmeldung_tagesschule_id = anmeldung_tagesschule.id
                                            join kind_container on anmeldung_tagesschule.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
                                            join sozialhilfe_zeitraum on sozialhilfe_zeitraum_container.sozialhilfe_zeitraumja_id = sozialhilfe_zeitraum.id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and (
            (verfuegung_zeitabschnitt.gueltig_ab >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_ab <= sozialhilfe_zeitraum.gueltig_bis) or
            (verfuegung_zeitabschnitt.gueltig_bis >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_bis <= sozialhilfe_zeitraum.gueltig_bis))
);

-- -- -- gemeinde
update bgcalculation_result
set sozialhilfe_akzeptiert = true
where id in (
    select bgcalculation_result.id from verfuegung_zeitabschnitt
                                            join bgcalculation_result  on verfuegung_zeitabschnitt.bg_calculation_result_gemeinde_id = bgcalculation_result.id
                                            join verfuegung on verfuegung_zeitabschnitt.verfuegung_id = verfuegung.id
                                            join anmeldung_tagesschule on verfuegung.anmeldung_tagesschule_id = anmeldung_tagesschule.id
                                            join kind_container on anmeldung_tagesschule.kind_id = kind_container.id
                                            join gesuch on kind_container.gesuch_id = gesuch.id
                                            join familiensituation_container on gesuch.familiensituation_container_id = familiensituation_container.id
                                            join familiensituation on familiensituation_container.familiensituationja_id = familiensituation.id
                                            join sozialhilfe_zeitraum_container on familiensituation_container.id = sozialhilfe_zeitraum_container.familiensituation_container_id
                                            join sozialhilfe_zeitraum on sozialhilfe_zeitraum_container.sozialhilfe_zeitraumja_id = sozialhilfe_zeitraum.id
    where familiensituation.sozialhilfe_bezueger = true and fin_sit_status = 'AKZEPTIERT' and (
            (verfuegung_zeitabschnitt.gueltig_ab >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_ab <= sozialhilfe_zeitraum.gueltig_bis) or
            (verfuegung_zeitabschnitt.gueltig_bis >= sozialhilfe_zeitraum.gueltig_ab and verfuegung_zeitabschnitt.gueltig_bis <= sozialhilfe_zeitraum.gueltig_bis))
);



