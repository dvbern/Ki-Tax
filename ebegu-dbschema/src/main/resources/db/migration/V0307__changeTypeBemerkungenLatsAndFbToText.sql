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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN begruendung_wenn_angebot_nicht_verfuegbar_fuer_alle_schulstufen text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN bemerkungen text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN ausbildungen_mitarbeitende_belegt_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN bemerkungen_weitere_kosten_und_ertraege text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN betreuungsstunden_dokumentiert_und_ueberprueft_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN elterngebuehren_gemaess_verordnung_berechnet_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN einkommen_eltern_belegt_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN maximal_tarif_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
MODIFY COLUMN mindestens50prozent_betreuungszeit_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN begruendung_wenn_angebot_nicht_verfuegbar_fuer_alle_schulstufen text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN bemerkungen text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN ausbildungen_mitarbeitende_belegt_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN bemerkungen_weitere_kosten_und_ertraege text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN betreuungsstunden_dokumentiert_und_ueberprueft_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN elterngebuehren_gemaess_verordnung_berechnet_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN einkommen_eltern_belegt_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN maximal_tarif_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud
MODIFY COLUMN mindestens50prozent_betreuungszeit_bemerkung text;

ALTER TABLE lastenausgleich_tagesschule_angaben_institution
MODIFY COLUMN bemerkungen text;

ALTER TABLE lastenausgleich_tagesschule_angaben_institution_aud
MODIFY COLUMN bemerkungen text;


