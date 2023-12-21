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
import {EbeguUtil} from '../utils/EbeguUtil';
import {TSAbstractFinanzielleSituation} from './TSAbstractFinanzielleSituation';

export class TSFinanzielleSituation extends TSAbstractFinanzielleSituation {

    private _steuerveranlagungErhalten: boolean = false;
    private _steuererklaerungAusgefuellt: boolean = false;
    private _steuerdatenZugriff: boolean;
    private _automatischePruefungErlaubt: boolean;
    private _geschaeftsgewinnBasisjahrMinus2: number;
    private _geschaeftsgewinnBasisjahrMinus1: number;
    private _quellenbesteuert: boolean;
    private _gemeinsameStekVorjahr: boolean;
    private _alleinigeStekVorjahr: boolean;
    private _veranlagt: boolean;
    private _veranlagtVorjahr: boolean;
    private _abzuegeKinderAusbildung: number;
    private _unterhaltsBeitraege: number;
    private _bruttoLohn: number;
    private _momentanSelbststaendig: boolean;
    private _steuerdatenAbfrageTimestamp: moment.Moment;
    private _ersatzeinkommenBezogen: boolean;
    private _ersatzeinkommenBasisjahr: number;
    private _ersatzeinkommenBasisjahrMinus1: number;
    private _ersatzeinkommenBasisjahrMinus2: number;

    public constructor() {
        super();
    }

    public get steuerveranlagungErhalten(): boolean {
        return this._steuerveranlagungErhalten;
    }

    public set steuerveranlagungErhalten(value: boolean) {
        this._steuerveranlagungErhalten = value;
    }

    public get steuererklaerungAusgefuellt(): boolean {
        return this._steuererklaerungAusgefuellt;
    }

    public set steuererklaerungAusgefuellt(value: boolean) {
        this._steuererklaerungAusgefuellt = value;
    }

    public set steuerdatenZugriff(value: boolean) {
        this._steuerdatenZugriff = value;
    }

    public get steuerdatenZugriff(): boolean {
        return this._steuerdatenZugriff;
    }

    public get geschaeftsgewinnBasisjahrMinus2(): number {
        return this._geschaeftsgewinnBasisjahrMinus2;
    }

    public set geschaeftsgewinnBasisjahrMinus2(value: number) {
        this._geschaeftsgewinnBasisjahrMinus2 = value;
    }

    public get geschaeftsgewinnBasisjahrMinus1(): number {
        return this._geschaeftsgewinnBasisjahrMinus1;
    }

    public set geschaeftsgewinnBasisjahrMinus1(value: number) {
        this._geschaeftsgewinnBasisjahrMinus1 = value;
    }

    public get gemeinsameStekVorjahr(): boolean {
        return this._gemeinsameStekVorjahr;
    }

    public set gemeinsameStekVorjahr(value: boolean) {
        this._gemeinsameStekVorjahr = value;
    }

    public get alleinigeStekVorjahr(): boolean {
        return this._alleinigeStekVorjahr;
    }

    public set alleinigeStekVorjahr(value: boolean) {
        this._alleinigeStekVorjahr = value;
    }

    public get veranlagt(): boolean {
        return this._veranlagt;
    }

    public set veranlagt(value: boolean) {
        this._veranlagt = value;
    }

    public get veranlagtVorjahr(): boolean {
        return this._veranlagtVorjahr;
    }

    public set veranlagtVorjahr(value: boolean) {
        this._veranlagtVorjahr = value;
    }

    public get quellenbesteuert(): boolean {
        return this._quellenbesteuert;
    }

    public set quellenbesteuert(value: boolean) {
        this._quellenbesteuert = value;
    }

    public get unterhaltsBeitraege(): number {
        return this._unterhaltsBeitraege;
    }

    public set unterhaltsBeitraege(value: number) {
        this._unterhaltsBeitraege = value;
    }
    public get abzuegeKinderAusbildung(): number {
        return this._abzuegeKinderAusbildung;
    }

    public set abzuegeKinderAusbildung(value: number) {
        this._abzuegeKinderAusbildung = value;
    }

    public get bruttoLohn(): number {
        return this._bruttoLohn;
    }

    public set bruttoLohn(value: number) {
        this._bruttoLohn = value;
    }

    public get momentanSelbststaendig(): boolean {
        return this._momentanSelbststaendig;
    }

    public set momentanSelbststaendig(value: boolean) {
        this._momentanSelbststaendig = value;
    }

    public get automatischePruefungErlaubt(): boolean {
        return this._automatischePruefungErlaubt;
    }

    public set automatischePruefungErlaubt(value: boolean) {
        this._automatischePruefungErlaubt = value;
    }

    public get steuerdatenAbfrageTimestamp(): moment.Moment {
        return this._steuerdatenAbfrageTimestamp;
    }

    public set steuerdatenAbfrageTimestamp(value: moment.Moment) {
        this._steuerdatenAbfrageTimestamp = value;
    }

    public get ersatzeinkommenBezogen(): boolean {
        return this._ersatzeinkommenBezogen;
    }

    public set ersatzeinkommenBezogen(value: boolean) {
        this._ersatzeinkommenBezogen = value;
    }

    public get ersatzeinkommenBasisjahr(): number {
        return this._ersatzeinkommenBasisjahr;
    }

    public set ersatzeinkommenBasisjahr(value: number) {
        this._ersatzeinkommenBasisjahr = value;
    }

    public get ersatzeinkommenBasisjahrMinus1(): number {
        return this._ersatzeinkommenBasisjahrMinus1;
    }

    public set ersatzeinkommenBasisjahrMinus1(value: number) {
        this._ersatzeinkommenBasisjahrMinus1 = value;
    }

    public get ersatzeinkommenBasisjahrMinus2(): number {
        return this._ersatzeinkommenBasisjahrMinus2;
    }

    public set ersatzeinkommenBasisjahrMinus2(value: number) {
        this._ersatzeinkommenBasisjahrMinus2 = value;
    }

    public isSelbstaendig(): boolean {
        return (this.geschaeftsgewinnBasisjahr !== null && this.geschaeftsgewinnBasisjahr !== undefined)
            || (this._geschaeftsgewinnBasisjahrMinus1 !== null && this._geschaeftsgewinnBasisjahrMinus1 !== undefined)
            || (this._geschaeftsgewinnBasisjahrMinus2 !== null && this._geschaeftsgewinnBasisjahrMinus2 !== undefined);
    }

    public hasErsatzeinkommenSelbststaendigkeit(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.ersatzeinkommenBasisjahr)
            || EbeguUtil.isNotNullOrUndefined(this.ersatzeinkommenBasisjahrMinus1)
            || EbeguUtil.isNotNullOrUndefined(this.ersatzeinkommenBasisjahrMinus2);
    }
}
