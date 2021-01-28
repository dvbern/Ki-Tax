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

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY angebot_fuer_ferienbetreuung_vorhanden BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY angebot_verfuegbar_fuer_alle_schulstufen BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY ausbildungen_mitarbeitende_belegt BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY bedarf_bei_eltern_abgeklaert BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY betreuungsstunden_dokumentiert_und_ueberprueft BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY davon_stunden_zu_normlohn_mehr_als50prozent_ausgebildete DECIMAL(19,2) NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY davon_stunden_zu_normlohn_weniger_als50prozent_ausgebildete DECIMAL(19,2) NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY einkommen_eltern_belegt BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY einnahmen_elterngebuehren DECIMAL(19,2) NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY einnahmen_subventionen_dritter DECIMAL(19,2) NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY einnnahmen_verpflegung DECIMAL(19,2) NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY elterngebuehren_gemaess_verordnung_berechnet BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY geleistete_betreuungsstunden_besondere_beduerfnisse BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY geleistete_betreuungsstunden_ohne_besondere_beduerfnisse BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY gesamt_kosten_tagesschule BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY maximal_tarif BIT NULL;

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde
	MODIFY mindestens50prozent_betreuungszeit_durch_ausgebildetes_personal BIT NULL;