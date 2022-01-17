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

import {TSAbstractFinanzielleSituation} from './TSAbstractFinanzielleSituation';
import {TSFinanzielleSituationSelbstdeklaration} from './TSFinanzielleSituationSelbstdeklaration';

export class TSFinanzielleSituation extends TSAbstractFinanzielleSituation {


    private _steuerveranlagungErhalten: boolean = false;
    private _steuererklaerungAusgefuellt: boolean = false;
    private _steuerdatenZugriff: boolean;
    private _geschaeftsgewinnBasisjahrMinus2: number;
    private _geschaeftsgewinnBasisjahrMinus1: number;
    private _quellenbesteuert: boolean;
    private _gemeinsameStekVorjahr: boolean;
    private _alleinigeStekVorjahr: boolean;
    private _veranlagt: boolean;
    private _bruttoertraegeVermoegen: number;
    private _nettoertraegeErbengemeinschaft: number;
    private _nettoVermoegen: number;
    private _einkommenInVereinfachtemVerfahrenAbgerechnet: boolean;
    private _amountEinkommenInVereinfachtemVerfahrenAbgerechnet: number;
    private _gewinnungskosten: number;
    private _abzugSchuldzinsen: number;
    private _selbstdeklaration: TSFinanzielleSituationSelbstdeklaration;

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
    public get quellenbesteuert(): boolean {
        return this._quellenbesteuert;
    }

    public set quellenbesteuert(value: boolean) {
        this._quellenbesteuert = value;
    }

    public get selbstdeklaration(): TSFinanzielleSituationSelbstdeklaration {
        return this._selbstdeklaration;
    }

    public set selbstdeklaration(value: TSFinanzielleSituationSelbstdeklaration) {
        this._selbstdeklaration = value;
    }

    public get gewinnungskosten(): number {
        return this._gewinnungskosten;
    }

    public set gewinnungskosten(value: number) {
        this._gewinnungskosten = value;
    }

    public get amountEinkommenInVereinfachtemVerfahrenAbgerechnet(): number {
        return this._amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
    }

    public set amountEinkommenInVereinfachtemVerfahrenAbgerechnet(value: number) {
        this._amountEinkommenInVereinfachtemVerfahrenAbgerechnet = value;
    }

    public get einkommenInVereinfachtemVerfahrenAbgerechnet(): boolean {
        return this._einkommenInVereinfachtemVerfahrenAbgerechnet;
    }

    public set einkommenInVereinfachtemVerfahrenAbgerechnet(value: boolean) {
        this._einkommenInVereinfachtemVerfahrenAbgerechnet = value;
    }

    public get nettoVermoegen(): number {
        return this._nettoVermoegen;
    }

    public set nettoVermoegen(value: number) {
        this._nettoVermoegen = value;
    }

    public get nettoertraegeErbengemeinschaft(): number {
        return this._nettoertraegeErbengemeinschaft;
    }

    public set nettoertraegeErbengemeinschaft(value: number) {
        this._nettoertraegeErbengemeinschaft = value;
    }

    public get bruttoertraegeVermoegen(): number {
        return this._bruttoertraegeVermoegen;
    }

    public set bruttoertraegeVermoegen(value: number) {
        this._bruttoertraegeVermoegen = value;
    }

    public get abzugSchuldzinsen(): number {
        return this._abzugSchuldzinsen;
    }

    public set abzugSchuldzinsen(value: number) {
        this._abzugSchuldzinsen = value;
    }

    public isSelbstaendig(): boolean {
        return (this.geschaeftsgewinnBasisjahr !== null && this.geschaeftsgewinnBasisjahr !== undefined)
            || (this._geschaeftsgewinnBasisjahrMinus1 !== null && this._geschaeftsgewinnBasisjahrMinus1 !== undefined)
            || (this._geschaeftsgewinnBasisjahrMinus2 !== null && this._geschaeftsgewinnBasisjahrMinus2 !== undefined);
    }
}
