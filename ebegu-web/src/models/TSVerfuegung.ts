/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {TSVerfuegungZeitabschnittZahlungsstatus} from './enums/TSVerfuegungZeitabschnittZahlungsstatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSVerfuegungZeitabschnitt} from './TSVerfuegungZeitabschnitt';

export class TSVerfuegung extends TSAbstractMutableEntity {

    private _generatedBemerkungen: string;
    private _manuelleBemerkungen: string;
    private _zeitabschnitte: Array<TSVerfuegungZeitabschnitt>;
    private _kategorieNormal: boolean;
    private _kategorieMaxEinkommen: boolean;
    private _kategorieKeinPensum: boolean;
    private _kategorieNichtEintreten: boolean;

    public constructor() {
        super();
    }

    public get generatedBemerkungen(): string {
        return this._generatedBemerkungen;
    }

    public set generatedBemerkungen(value: string) {
        this._generatedBemerkungen = value;
    }

    public get manuelleBemerkungen(): string {
        return this._manuelleBemerkungen;
    }

    public set manuelleBemerkungen(value: string) {
        this._manuelleBemerkungen = value;
    }

    public get zeitabschnitte(): Array<TSVerfuegungZeitabschnitt> {
        return this._zeitabschnitte;
    }

    public set zeitabschnitte(value: Array<TSVerfuegungZeitabschnitt>) {
        this._zeitabschnitte = value;
    }

    public get kategorieNormal(): boolean {
        return this._kategorieNormal;
    }

    public set kategorieNormal(value: boolean) {
        this._kategorieNormal = value;
    }

    public get kategorieMaxEinkommen(): boolean {
        return this._kategorieMaxEinkommen;
    }

    public set kategorieMaxEinkommen(value: boolean) {
        this._kategorieMaxEinkommen = value;
    }

    public get kategorieKeinPensum(): boolean {
        return this._kategorieKeinPensum;
    }

    public set kategorieKeinPensum(value: boolean) {
        this._kategorieKeinPensum = value;
    }

    public get kategorieNichtEintreten(): boolean {
        return this._kategorieNichtEintreten;
    }

    public set kategorieNichtEintreten(value: boolean) {
        this._kategorieNichtEintreten = value;
    }

    /**
     * Checks whether all Zeitabschnitte have the same data as the previous (vorgaenger) Verfuegung.
     */
    public areSameVerfuegteVerfuegungsrelevanteDaten(): boolean {
        return this._zeitabschnitte.every(za => za.sameVerfuegteVerfuegungsrelevanteDaten);
    }

    /**
     * Checks whether all Zeitabschnitte that have been paid (verrechnet)
     * have the same Verguenstigung as the previous (vorgaenger) Verfuegung.
     * All Ignorierte Zeitabschnitte must be ignored because they will always be ignored
     * Entscheidet, ob die Frage nach dem Ignorieren gestellt werden soll
     */
    public fragenObIgnorieren(): boolean {
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const zeitabschnitt = this._zeitabschnitte[i];
            // Wir muessen alle Zeitabschnitte kontrollieren, die in irgendeiner Form schon verrechnet
            // oder behandelt waren, also nicht NEU sind
            // Entsprechend muss sichergestellt werden, dass wenn die Ignorieren-Frage mit "uebernehmen"
            // beantwortet wurde, die betroffenen Zeitabschnitte nicht NEU sondern  VERRECHNEND sind.
            // Sonst wird die Frage in einem solchen Fall nicht wieder gestellt!
            // tslint:disable-next-line:early-exit
            if (zeitabschnitt.zahlungsstatus !== TSVerfuegungZeitabschnittZahlungsstatus.NEU
                    && !zeitabschnitt.sameAusbezahlteVerguenstigung) {
                // Sobald es mindestens an einem verrechneten Abschnitt eine Aenderung gibt, muss die Frage
                // gestellt werden
                return true;
            }
        }
        return false;
    }

    public fragenObIgnorierenMahlzeiten(): boolean {
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const zeitabschnitt = this._zeitabschnitte[i];
            // Wir muessen alle Zeitabschnitte kontrollieren, die in irgendeiner Form schon verrechnet
            // oder behandelt waren, also nicht NEU sind
            // Entsprechend muss sichergestellt werden, dass wenn die Ignorieren-Frage mit "uebernehmen"
            // beantwortet wurde, die betroffenen Zeitabschnitte nicht NEU sondern  VERRECHNEND sind.
            // Sonst wird die Frage in einem solchen Fall nicht wieder gestellt!
            // tslint:disable-next-line:early-exit
            if (zeitabschnitt.zahlungsstatusMahlzeitenverguenstigung !== TSVerfuegungZeitabschnittZahlungsstatus.NEU
                && !zeitabschnitt.sameAusbezahlteMahlzeiten) {
                // Sobald es mindestens an einem verrechneten Abschnitt eine Aenderung gibt, muss die Frage
                // gestellt werden
                return true;
            }
        }
        return false;
    }

    public isAlreadyIgnored(): boolean {
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            const datenVeraeandert = !abschnitt.sameAusbezahlteVerguenstigung;
            const alreadyIgnored = abschnitt.zahlungsstatus === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT
                || abschnitt.zahlungsstatus === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT;
            if (datenVeraeandert && alreadyIgnored) {
                return true;
            }
        }
        return false;
    }

    public isAlreadyIgnoredMahlzeiten(): boolean {
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            const datenVeraeandert = !abschnitt.sameAusbezahlteMahlzeiten;
            const alreadyIgnored = abschnitt.zahlungsstatusMahlzeitenverguenstigung === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT
                || abschnitt.zahlungsstatusMahlzeitenverguenstigung === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT;
            if (datenVeraeandert && alreadyIgnored) {
                return true;
            }
        }
        return false;
    }

    public mahlzeitenChangedSincePreviousVerfuegung(): boolean {
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            if (!abschnitt.sameVerfuegteMahlzeitenVerguenstigung) {
                return true;
            }
        }
        return false;
    }
}
