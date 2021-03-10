/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {TSAbstractEntity} from '../TSAbstractEntity';

export class TSLastenausgleichTagesschuleAngabenGemeinde extends TSAbstractEntity {

    // A: Allgemeine Angaben
    public bedarfBeiElternAbgeklaert: boolean;
    public angebotFuerFerienbetreuungVorhanden: boolean;
    public angebotVerfuegbarFuerAlleSchulstufen: boolean;
    public begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen: string;

    // B: Abrechnung
    public geleisteteBetreuungsstundenOhneBesondereBeduerfnisse: number;
    public geleisteteBetreuungsstundenBesondereBeduerfnisse: number;
    public davonStundenZuNormlohnMehrAls50ProzentAusgebildete: number;
    public davonStundenZuNormlohnWenigerAls50ProzentAusgebildete: number;
    public einnahmenElterngebuehren: number;

    // C: Kostenbeteiligung Gemeinde
    public gesamtKostenTagesschule: number;
    public einnnahmenVerpflegung: number;
    public einnahmenSubventionenDritter: number;

    // D: Angaben zu weiteren Kosten und Ertraegen
    public bemerkungenWeitereKostenUndErtraege: string;

    // E: Kontrollfragen
    public betreuungsstundenDokumentiertUndUeberprueft: boolean;
    public elterngebuehrenGemaessVerordnungBerechnet: boolean;
    public einkommenElternBelegt: boolean;
    public maximalTarif: boolean;
    public mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal: boolean;
    public ausbildungenMitarbeitendeBelegt: boolean;

    // Bemerkungen
    public bemerkungen: string;
    public status: string;
}
