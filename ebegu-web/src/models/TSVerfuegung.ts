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

import TSAbstractEntity from './TSAbstractEntity';
import TSVerfuegungZeitabschnitt from './TSVerfuegungZeitabschnitt';
import {TSVerfuegungZeitabschnittZahlungsstatus} from './enums/TSVerfuegungZeitabschnittZahlungsstatus';

export default class TSVerfuegung extends TSAbstractEntity {

    private _generatedBemerkungen: string;
    private _manuelleBemerkungen: string;
    private _zeitabschnitte: Array<TSVerfuegungZeitabschnitt>;
    private _kategorieNormal: boolean;
    private _kategorieMaxEinkommen: boolean;
    private _kategorieKeinPensum: boolean;
    private _kategorieZuschlagZumErwerbspensum: boolean;
    private _kategorieNichtEintreten: boolean;

    constructor(generatedBemerkungen?: string, manuelleBemerkungen?: string, zeitabschnitte?: Array<TSVerfuegungZeitabschnitt>,
                kategorieNormal?: boolean, kategorieMaxEinkommen?: boolean, kategorieKeinPensum?: boolean, kategorieZuschlagZumErwerbspensum?: boolean,
                kategorieNichtEintreten?: boolean) {
        super();
        this._generatedBemerkungen = generatedBemerkungen;
        this._manuelleBemerkungen = manuelleBemerkungen;
        this._zeitabschnitte = zeitabschnitte;
        this._kategorieNormal = kategorieNormal;
        this._kategorieMaxEinkommen = kategorieMaxEinkommen;
        this._kategorieKeinPensum = kategorieKeinPensum;
        this._kategorieZuschlagZumErwerbspensum = kategorieZuschlagZumErwerbspensum;
        this._kategorieNichtEintreten = kategorieNichtEintreten;
    }

    get generatedBemerkungen(): string {
        return this._generatedBemerkungen;
    }

    set generatedBemerkungen(value: string) {
        this._generatedBemerkungen = value;
    }

    get manuelleBemerkungen(): string {
        return this._manuelleBemerkungen;
    }

    set manuelleBemerkungen(value: string) {
        this._manuelleBemerkungen = value;
    }

    get zeitabschnitte(): Array<TSVerfuegungZeitabschnitt> {
        return this._zeitabschnitte;
    }

    set zeitabschnitte(value: Array<TSVerfuegungZeitabschnitt>) {
        this._zeitabschnitte = value;
    }

    get kategorieNormal(): boolean {
        return this._kategorieNormal;
    }

    set kategorieNormal(value: boolean) {
        this._kategorieNormal = value;
    }

    get kategorieMaxEinkommen(): boolean {
        return this._kategorieMaxEinkommen;
    }

    set kategorieMaxEinkommen(value: boolean) {
        this._kategorieMaxEinkommen = value;
    }

    get kategorieKeinPensum(): boolean {
        return this._kategorieKeinPensum;
    }

    set kategorieKeinPensum(value: boolean) {
        this._kategorieKeinPensum = value;
    }

    get kategorieZuschlagZumErwerbspensum(): boolean {
        return this._kategorieZuschlagZumErwerbspensum;
    }

    set kategorieZuschlagZumErwerbspensum(value: boolean) {
        this._kategorieZuschlagZumErwerbspensum = value;
    }

    get kategorieNichtEintreten(): boolean {
        return this._kategorieNichtEintreten;
    }

    set kategorieNichtEintreten(value: boolean) {
        this._kategorieNichtEintreten = value;
    }

    /**
     * Checks whether all Zeitabschnitte have the same data as the previous (vorgaenger) Verfuegung.
     */
    public areSameVerfuegungsdaten(): boolean {
        return this._zeitabschnitte.every(za => za.sameVerfuegungsdaten);
    }

    /**
     * Checks whether all Zeitabschnitte that have been paid (verrechnet or ignored)
     * have the same Verguenstigung as the previous (vorgaenger) Verfuegung.
     */
    public isSameVerrechneteVerguenstigung(): boolean {
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this._zeitabschnitte.length; i++) {
            if (this._zeitabschnitte[i].sameVerguenstigung !== true
                && (this._zeitabschnitte[i].zahlungsstatus === TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET
                    || this._zeitabschnitte[i].zahlungsstatus === TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT
                    || this._zeitabschnitte[i].zahlungsstatus === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT
                    || this._zeitabschnitte[i].zahlungsstatus === TSVerfuegungZeitabschnittZahlungsstatus.IGNORIEREND)) {
                return false;
            }
        }
        return true;
    }
}
