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

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN lastenausgleichberechtigte_betreuungsstunden DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN stunden_mehr_als_50_prozent_ausgebildete_berechnet DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN stunden_weniger_als_50_prozent_ausgebildete_berechnet DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN normlohnkosten_betreuung_berechnet DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN lastenausgleichsberechtiger_betrag DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN kostenbeitrag_gemeinde DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN kostenueberschuss_gemeinde DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN erwarteter_kostenbeitrag_gemeinde DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN schlusszahlung DECIMAL(19,2);

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN lastenausgleichberechtigte_betreuungsstunden DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN stunden_mehr_als_50_prozent_ausgebildete_berechnet DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN stunden_weniger_als_50_prozent_ausgebildete_berechnet DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN normlohnkosten_betreuung_berechnet DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN lastenausgleichsberechtiger_betrag DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN kostenbeitrag_gemeinde DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN kostenueberschuss_gemeinde DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN erwarteter_kostenbeitrag_gemeinde DECIMAL(19,2);
ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN schlusszahlung DECIMAL(19,2);

UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET lastenausgleichberechtigte_betreuungsstunden = geleistete_betreuungsstunden_besondere_beduerfnisse + geleistete_betreuungsstunden_ohne_besondere_beduerfnisse;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET stunden_mehr_als_50_prozent_ausgebildete_berechnet = davon_stunden_zu_normlohn_mehr_als50prozent_ausgebildete * 10.39;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET stunden_weniger_als_50_prozent_ausgebildete_berechnet = davon_stunden_zu_normlohn_weniger_als50prozent_ausgebildete * 5.2;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET normlohnkosten_betreuung_berechnet = stunden_weniger_als_50_prozent_ausgebildete_berechnet + stunden_mehr_als_50_prozent_ausgebildete_berechnet;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET lastenausgleichsberechtiger_betrag = normlohnkosten_betreuung_berechnet - einnahmen_elterngebuehren;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET kostenbeitrag_gemeinde = gesamt_kosten_tagesschule - lastenausgleichsberechtiger_betrag - einnahmen_elterngebuehren - einnnahmen_verpflegung - einnahmen_subventionen_dritter;
# negative werte hier nicht speichern
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET kostenbeitrag_gemeinde = NULL WHERE kostenbeitrag_gemeinde < 0;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET kostenueberschuss_gemeinde = gesamt_kosten_tagesschule - lastenausgleichsberechtiger_betrag - einnahmen_elterngebuehren - einnnahmen_verpflegung - einnahmen_subventionen_dritter;
# strict positive werte hier nicht speichern
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET kostenueberschuss_gemeinde = NULL WHERE kostenueberschuss_gemeinde >= 0;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET erwarteter_kostenbeitrag_gemeinde = gesamt_kosten_tagesschule * 0.2;
UPDATE lastenausgleich_tagesschule_angaben_gemeinde SET schlusszahlung = lastenausgleichsberechtiger_betrag - erste_rate_ausbezahlt;
