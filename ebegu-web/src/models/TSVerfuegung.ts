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

import {
    getZahlungsstatusIgnorieren,
    TSVerfuegungZeitabschnittZahlungsstatus
} from './enums/TSVerfuegungZeitabschnittZahlungsstatus';
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
    private _veraenderungVerguenstigungGegenueberVorgaenger: number;
    private _ignorable: boolean;
    private _korrekturAusbezahltInstitution: number;
    private _korrekturAusbezahltEltern: number;

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

    public get veraenderungVerguenstigungGegenueberVorgaenger(): number {
        return this._veraenderungVerguenstigungGegenueberVorgaenger;
    }

    public set veraenderungVerguenstigungGegenueberVorgaenger(value: number) {
        this._veraenderungVerguenstigungGegenueberVorgaenger = value;
    }

    public get ignorable(): boolean {
        return this._ignorable;
    }

    public set ignorable(value: boolean) {
        this._ignorable = value;
    }

    public get korrekturAusbezahltInstitution(): number {
        return this._korrekturAusbezahltInstitution;
    }

    public set korrekturAusbezahltInstitution(value: number) {
        this._korrekturAusbezahltInstitution = value;
    }
    public get korrekturAusbezahltEltern(): number {
        return this._korrekturAusbezahltEltern;
    }

    public set korrekturAusbezahltEltern(value: number) {
        this._korrekturAusbezahltEltern = value;
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
    public fragenObIgnorieren(showIfVerrechnetAberKeineBetreuung: boolean): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const zeitabschnitt = this._zeitabschnitte[i];
            // Wir muessen alle Zeitabschnitte kontrollieren, die in irgendeiner Form schon verrechnet
            // oder behandelt waren, also nicht NEU sind
            // Entsprechend muss sichergestellt werden, dass wenn die Ignorieren-Frage mit "uebernehmen"
            // beantwortet wurde, die betroffenen Zeitabschnitte nicht NEU sondern  VERRECHNEND sind.
            // Sonst wird die Frage in einem solchen Fall nicht wieder gestellt!
            // eslint-disable-next-line
            if (zeitabschnitt.zahlungsstatusInstitution !== TSVerfuegungZeitabschnittZahlungsstatus.NEU
                    && !zeitabschnitt.sameAusbezahlteVerguenstigung) {
                if (showIfVerrechnetAberKeineBetreuung
                    || zeitabschnitt.zahlungsstatusInstitution !== TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET_KEINE_BETREUUNG) {
                    // Sobald es mindestens an einem verrechneten Abschnitt eine Aenderung gibt, muss die Frage
                    // gestellt werden
                    return true;
                }
            }
        }
        return false;
    }

    public fragenObIgnorierenMahlzeiten(): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const zeitabschnitt = this._zeitabschnitte[i];
            // Wir muessen alle Zeitabschnitte kontrollieren, die in irgendeiner Form schon verrechnet
            // oder behandelt waren, also nicht NEU sind
            // Entsprechend muss sichergestellt werden, dass wenn die Ignorieren-Frage mit "uebernehmen"
            // beantwortet wurde, die betroffenen Zeitabschnitte nicht NEU sondern  VERRECHNEND sind.
            // Sonst wird die Frage in einem solchen Fall nicht wieder gestellt!
            // eslint-disable-next-line
            if (zeitabschnitt.zahlungsstatusAntragsteller !== TSVerfuegungZeitabschnittZahlungsstatus.NEU
                && !zeitabschnitt.sameAusbezahlteMahlzeiten) {
                // Sobald es mindestens an einem verrechneten Abschnitt eine Aenderung gibt, muss die Frage
                // gestellt werden
                return true;
            }
        }
        return false;
    }

    public isAlreadyIgnored(): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            const datenVeraeandert = !abschnitt.sameAusbezahlteVerguenstigung;
            const alreadyIgnored = abschnitt.zahlungsstatusInstitution === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT
                || abschnitt.zahlungsstatusInstitution === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT;
            if (datenVeraeandert && alreadyIgnored) {
                return true;
            }
        }
        return false;
    }

    public isAlreadyIgnoredMahlzeiten(): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            const datenVeraeandert = !abschnitt.sameAusbezahlteMahlzeiten;
            const alreadyIgnored = abschnitt.zahlungsstatusAntragsteller === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT
                || abschnitt.zahlungsstatusAntragsteller === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT;
            if (datenVeraeandert && alreadyIgnored) {
                return true;
            }
        }
        return false;
    }

    public isAlreadyIgnorierend(): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            const datenVeraeandert = !abschnitt.sameAusbezahlteVerguenstigung;
            const alreadyIgnored = getZahlungsstatusIgnorieren().includes(abschnitt.zahlungsstatusInstitution);
            if (datenVeraeandert && alreadyIgnored) {
                return true;
            }
        }
        return false;
    }

    public isAlreadyIgnorierendMahlzeiten(): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            const datenVeraeandert = !abschnitt.sameAusbezahlteMahlzeiten;
            const alreadyIgnored = getZahlungsstatusIgnorieren().includes(abschnitt.zahlungsstatusAntragsteller);
            if (datenVeraeandert && alreadyIgnored) {
                return true;
            }
        }
        return false;
    }

    public mahlzeitenChangedSincePreviousVerfuegung(): boolean {
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            const abschnitt = this._zeitabschnitte[i];
            if (!abschnitt.sameVerfuegteMahlzeitenVerguenstigung) {
                return true;
            }
        }
        return false;
    }
}
