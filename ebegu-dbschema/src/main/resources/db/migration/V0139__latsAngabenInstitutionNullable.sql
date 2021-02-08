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

alter table lastenausgleich_tagesschule_angaben_institution modify anzahl_eingeschriebene_kinder decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify anzahl_eingeschriebene_kinder_basisstufe decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify anzahl_eingeschriebene_kinder_kindergarten decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify anzahl_eingeschriebene_kinder_mit_besonderen_beduerfnissen decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify anzahl_eingeschriebene_kinder_primarstufe decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify betreuungsverhaeltnis_eingehalten bit null;

alter table lastenausgleich_tagesschule_angaben_institution modify durchschnitt_kinder_pro_tag_fruehbetreuung decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify durchschnitt_kinder_pro_tag_mittag decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify durchschnitt_kinder_pro_tag_nachmittag1 decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify durchschnitt_kinder_pro_tag_nachmittag2 decimal(19,2) null;

alter table lastenausgleich_tagesschule_angaben_institution modify ernaehrungs_grundsaetze_eingehalten bit null;

alter table lastenausgleich_tagesschule_angaben_institution modify is_lehrbetrieb bit null;

alter table lastenausgleich_tagesschule_angaben_institution modify raeumliche_voraussetzungen_eingehalten bit null;

alter table lastenausgleich_tagesschule_angaben_institution modify schule_auf_basis_organisatorisches_konzept bit null;

alter table lastenausgleich_tagesschule_angaben_institution modify schule_auf_basis_paedagogisches_konzept bit null;

