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

import {TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus} from '../enums/TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';

export class TSLastenausgleichTagesschuleAngabenGemeinde extends TSAbstractEntity {
    public status: TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus;

    // A: Allgemeine Angaben
    public bedarfBeiElternAbgeklaert: boolean;
    public angebotFuerFerienbetreuungVorhanden: boolean;
    public angebotVerfuegbarFuerAlleSchulstufen: boolean;
    public begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen: string;

    // B: Abrechnung
    public geleisteteBetreuungsstundenOhneBesondereBeduerfnisse: number;
    public geleisteteBetreuungsstundenBesondereBeduerfnisse: number;
    public geleisteteBetreuungsstundenBesondereVolksschulangebot: number;
    public davonStundenZuNormlohnMehrAls50ProzentAusgebildete: number;
    public davonStundenZuNormlohnWenigerAls50ProzentAusgebildete: number;
    public einnahmenElterngebuehren: number;
    public einnahmenElterngebuehrenVolksschulangebot: number;
    public tagesschuleTeilweiseGeschlossen: boolean;
    public rueckerstattungenElterngebuehrenSchliessung: number;
    public ersteRateAusbezahlt: number;
    // C: Kostenbeteiligung Gemeinde
    public gesamtKostenTagesschule: number;
    public einnnahmenVerpflegung: number;
    public ueberschussErzielt: boolean;
    public ueberschussVerwendung: string;

    public einnahmenSubventionenDritter: number;

    // D: Angaben zu weiteren Kosten und Ertraegen
    public bemerkungenWeitereKostenUndErtraege: string;

    // E: Kontrollfragen
    public betreuungsstundenDokumentiertUndUeberprueft: boolean;
    public betreuungsstundenDokumentiertUndUeberprueftBemerkung: string;
    public elterngebuehrenGemaessVerordnungBerechnet: boolean;
    public elterngebuehrenGemaessVerordnungBerechnetBemerkung: string;
    public einkommenElternBelegt: boolean;
    public einkommenElternBelegtBemerkung: string;
    public maximalTarif: boolean;
    public maximalTarifBemerkung: string;
    public mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal: boolean;
    public mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung: string;
    public ausbildungenMitarbeitendeBelegt: boolean;
    public ausbildungenMitarbeitendeBelegtBemerkung: string;

    // Bemerkungen
    public bemerkungen: string;
    public bemerkungStarkeVeraenderung: string;

    // Berechnungen
    public lastenausgleichberechtigteBetreuungsstunden: number;
    public davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet: number;
    public davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet: number;
    public normlohnkostenBetreuungBerechnet: number;
    public lastenausgleichsberechtigerBetrag: number;
    public kostenbeitragGemeinde: number;
    public kostenueberschussGemeinde: number;
    public erwarteterKostenbeitragGemeinde: number;
    public schlusszahlung: number;

    public isInBearbeitung(): boolean {
        return [
            TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG,
            TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.VALIDIERUNG_FEHLGESCHLAGEN
        ].includes(this.status);
    }

    public isAbgeschlossen(): boolean {
        return (
            this.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN
        );
    }
}
