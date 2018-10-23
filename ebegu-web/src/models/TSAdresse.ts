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

import {TSAdressetyp} from './enums/TSAdressetyp';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSDateRange} from './types/TSDateRange';

export default class TSAdresse extends TSAbstractDateRangedEntity {

    private _strasse: string;
    private _hausnummer: string;
    private _zusatzzeile: string;
    private _plz: string;
    private _ort: string;
    private _land: string;
    private _gemeinde: string;
    private _adresseTyp: TSAdressetyp = TSAdressetyp.WOHNADRESSE;
    private _nichtInGemeinde: boolean;
    private _organisation: string;

    public constructor(
        strasse?: string,
        hausnummer?: string,
        zusatzzeile?: string,
        plz?: string,
        ort?: string,
        land?: string,
        gemeinde?: string,
        gueltigkeit?: TSDateRange,
        adresseTyp?: TSAdressetyp,
        nichtInGemeinde?: boolean,
        organisation?: string,
    ) {
        super(gueltigkeit);
        this._strasse = strasse;
        this._hausnummer = hausnummer;
        this._zusatzzeile = zusatzzeile;
        this._plz = plz;
        this._ort = ort;
        this._land = land || 'CH';
        this._gemeinde = gemeinde;
        this._adresseTyp = adresseTyp;
        this._nichtInGemeinde = nichtInGemeinde;
        this._organisation = organisation;
    }

    /**
     * Diese Methode sollte nur benutzt werden, um Wohnadressen zu vergleichen
     */
    public isSameWohnAdresse(other: TSAdresse): boolean {
        return (
            this._strasse === other.strasse &&
            this._hausnummer === other.hausnummer &&
            this._zusatzzeile === other.zusatzzeile &&
            this._plz === other.plz &&
            this._ort === other.ort &&
            this._land === other.land &&
            this._gemeinde === other.gemeinde &&
            this._adresseTyp === other.adresseTyp &&
            this._nichtInGemeinde === other.nichtInGemeinde &&
            this.gueltigkeit.gueltigAb.isSame(other.gueltigkeit.gueltigAb)
            // gueltigBis wird nicht gecheckt, da es nur relevant ist, wann sie eingezogen sind
        );
    }

    public copy(toCopy: TSAdresse): void {
        this._strasse = toCopy.strasse;
        this._hausnummer = toCopy.hausnummer;
        this._zusatzzeile = toCopy.zusatzzeile;
        this._plz = toCopy.plz;
        this._ort = toCopy.ort;
        this._land = toCopy.land;
        this._gemeinde = toCopy.gemeinde;
        this._adresseTyp = toCopy.adresseTyp;
        this._nichtInGemeinde = toCopy.nichtInGemeinde;
        if (!this.gueltigkeit) {
            this.gueltigkeit = new TSDateRange();
        }
        this.gueltigkeit.gueltigAb = toCopy.gueltigkeit.gueltigAb;
        this.gueltigkeit.gueltigBis = toCopy.gueltigkeit.gueltigBis;
    }

    public get strasse(): string {
        return this._strasse;
    }

    public set strasse(value: string) {
        this._strasse = value;
    }

    public get hausnummer(): string {
        return this._hausnummer;
    }

    public set hausnummer(value: string) {
        this._hausnummer = value;
    }

    public get zusatzzeile(): string {
        return this._zusatzzeile;
    }

    public set zusatzzeile(value: string) {
        this._zusatzzeile = value;
    }

    public get plz(): string {
        return this._plz;
    }

    public set plz(value: string) {
        this._plz = value;
    }

    public get ort(): string {
        return this._ort;
    }

    public set ort(value: string) {
        this._ort = value;
    }

    public get land(): string {
        return this._land;
    }

    public set land(value: string) {
        this._land = value;
    }

    public get gemeinde(): string {
        return this._gemeinde;
    }

    public set gemeinde(value: string) {
        this._gemeinde = value;
    }

    public get adresseTyp(): TSAdressetyp {
        return this._adresseTyp;
    }

    public set adresseTyp(value: TSAdressetyp) {
        this._adresseTyp = value;
    }

    public get nichtInGemeinde(): boolean {
        return this._nichtInGemeinde;
    }

    public set nichtInGemeinde(value: boolean) {
        this._nichtInGemeinde = value;
    }

    public get organisation(): string {
        return this._organisation;
    }

    public set organisation(value: string) {
        this._organisation = value;
    }
}
