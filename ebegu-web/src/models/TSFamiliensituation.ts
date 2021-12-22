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

import * as moment from 'moment';
import {TSFamilienstatus} from './enums/TSFamilienstatus';
import {TSGesuchstellerKardinalitaet} from './enums/TSGesuchstellerKardinalitaet';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSAdresse} from './TSAdresse';

export class TSFamiliensituation extends TSAbstractMutableEntity {
    private _familienstatus: TSFamilienstatus;
    private _gemeinsameSteuererklaerung: boolean;
    private _aenderungPer: moment.Moment;
    private _startKonkubinat: moment.Moment;
    private _sozialhilfeBezueger: boolean;
    private _verguenstigungGewuenscht: boolean;
    private _keineMahlzeitenverguenstigungBeantragt: boolean;
    private _iban: string;
    private _kontoinhaber: string;
    private _abweichendeZahlungsadresse: boolean;
    private _zahlungsadresse: TSAdresse;
    private _gesuchstellerKardinalitaet: TSGesuchstellerKardinalitaet;
    private _fkjvFamSit: boolean;
    private _minDauerKonkubinat: number;

    public constructor() {
        super();
    }

    public get familienstatus(): TSFamilienstatus {
        return this._familienstatus;
    }

    public set familienstatus(familienstatus: TSFamilienstatus) {
        this._familienstatus = familienstatus;
    }

    public get gemeinsameSteuererklaerung(): boolean {
        return this._gemeinsameSteuererklaerung;
    }

    public set gemeinsameSteuererklaerung(value: boolean) {
        this._gemeinsameSteuererklaerung = value;
    }

    public get aenderungPer(): moment.Moment {
        return this._aenderungPer;
    }

    public set aenderungPer(value: moment.Moment) {
        this._aenderungPer = value;
    }

    public get startKonkubinat(): moment.Moment {
        return this._startKonkubinat;
    }

    public set startKonkubinat(value: moment.Moment) {
        this._startKonkubinat = value;
    }

    public get sozialhilfeBezueger(): boolean {
        return this._sozialhilfeBezueger;
    }

    public set sozialhilfeBezueger(value: boolean) {
        this._sozialhilfeBezueger = value;
    }

    public get verguenstigungGewuenscht(): boolean {
        return this._verguenstigungGewuenscht;
    }

    public set verguenstigungGewuenscht(value: boolean) {
        this._verguenstigungGewuenscht = value;
    }

    public get keineMahlzeitenverguenstigungBeantragt(): boolean {
        return this._keineMahlzeitenverguenstigungBeantragt;
    }

    public set keineMahlzeitenverguenstigungBeantragt(value: boolean) {
        this._keineMahlzeitenverguenstigungBeantragt = value;
    }

    public get iban(): string {
        return this._iban;
    }

    public set iban(value: string) {
        this._iban = value;
    }

    public get kontoinhaber(): string {
        return this._kontoinhaber;
    }

    public set kontoinhaber(value: string) {
        this._kontoinhaber = value;
    }

    public get abweichendeZahlungsadresse(): boolean {
        return this._abweichendeZahlungsadresse;
    }

    public set abweichendeZahlungsadresse(value: boolean) {
        this._abweichendeZahlungsadresse = value;
    }

    public get zahlungsadresse(): TSAdresse {
        return this._zahlungsadresse;
    }

    public set zahlungsadresse(value: TSAdresse) {
        this._zahlungsadresse = value;
    }

    public get gesuchstellerKardinalitaet(): TSGesuchstellerKardinalitaet {
        return this._gesuchstellerKardinalitaet;
    }

    public set gesuchstellerKardinalitaet(value: TSGesuchstellerKardinalitaet) {
        this._gesuchstellerKardinalitaet = value;
    }

    public hasSecondGesuchsteller(referenzdatum: moment.Moment): boolean {
        switch (this.familienstatus) {
            case TSFamilienstatus.ALLEINERZIEHEND:
            case TSFamilienstatus.PFLEGEFAMILIE:
                if (!this.fkjvFamSit) {
                    return false;
                }
                return this.gesuchstellerKardinalitaet === TSGesuchstellerKardinalitaet.ZU_ZWEIT;
            case TSFamilienstatus.VERHEIRATET:
            case TSFamilienstatus.KONKUBINAT:
                return true;
            case TSFamilienstatus.KONKUBINAT_KEIN_KIND:
                const ref = moment(referenzdatum); // must copy otherwise source is also subtracted
                const xBack = ref
                    .subtract(this.minDauerKonkubinat, 'years')  // x years for konkubinat
                    .subtract(1, 'month'); // 1 month for rule
                return !this.startKonkubinat || !this.startKonkubinat.isAfter(xBack);
            default:
                throw new Error(`hasSecondGesuchsteller is not implemented for status ${this.familienstatus}`);
        }
    }

    public isSameFamiliensituation(other: TSFamiliensituation): boolean {
        let same = this.familienstatus === other.familienstatus;
        if (same && this.familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            same = this.startKonkubinat.isSame(other.startKonkubinat);
        }
        return same;
    }

    public revertFamiliensituation(other: TSFamiliensituation): void {
        this.familienstatus = other.familienstatus;
        this.startKonkubinat = other.startKonkubinat;
    }

    public get fkjvFamSit(): boolean {
        return this._fkjvFamSit;
    }

    public set fkjvFamSit(value: boolean) {
        this._fkjvFamSit = value;
    }

    public get minDauerKonkubinat(): number {
        return this._minDauerKonkubinat;
    }

    public set minDauerKonkubinat(value: number) {
        this._minDauerKonkubinat = value;
    }
}
