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

alter table lastenausgleich_tagesschule_angaben_gemeinde add column betreuungsstunden_dokumentiert_und_ueberprueft_bemerkung varchar(255);
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column betreuungsstunden_dokumentiert_und_ueberprueft_bemerkung varchar(255);

alter table lastenausgleich_tagesschule_angaben_gemeinde add column elterngebuehren_gemaess_verordnung_berechnet_bemerkung varchar(255);
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column elterngebuehren_gemaess_verordnung_berechnet_bemerkung varchar(255);

alter table lastenausgleich_tagesschule_angaben_gemeinde add column einkommen_eltern_belegt_bemerkung varchar(255);
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column einkommen_eltern_belegt_bemerkung varchar(255);

alter table lastenausgleich_tagesschule_angaben_gemeinde add column maximal_tarif_bemerkung varchar(255);
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column maximal_tarif_bemerkung varchar(255);

alter table lastenausgleich_tagesschule_angaben_gemeinde add column mindestens50prozent_betreuungszeit_bemerkung varchar(255);
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column mindestens50prozent_betreuungszeit_bemerkung varchar(255);

alter table lastenausgleich_tagesschule_angaben_gemeinde add column ausbildungen_mitarbeitende_belegt_bemerkung varchar(255);
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column ausbildungen_mitarbeitende_belegt_bemerkung varchar(255);

alter table lastenausgleich_tagesschule_angaben_gemeinde add column geleistete_betreuungsstunden_besondere_volksschulangebot decimal(19,2) default 0;
alter table lastenausgleich_tagesschule_angaben_gemeinde_aud add column geleistete_betreuungsstunden_besondere_volksschulangebot decimal(19,2);
alter table lastenausgleich_tagesschule_angaben_gemeinde modify column geleistete_betreuungsstunden_besondere_volksschulangebot decimal(19,2);

alter table lastenausgleich_tagesschule_angaben_institution add column anzahl_eingeschriebene_kinder_volksschulangebot decimal(19,2) default 0;
alter table lastenausgleich_tagesschule_angaben_institution_aud add column anzahl_eingeschriebene_kinder_volksschulangebot decimal(19,2);
alter table lastenausgleich_tagesschule_angaben_institution modify column anzahl_eingeschriebene_kinder_volksschulangebot decimal(19,2);

alter table lastenausgleich_tagesschule_angaben_institution add column oeffnungszeiten JSON;
alter table lastenausgleich_tagesschule_angaben_institution_aud add column oeffnungszeiten JSON;
