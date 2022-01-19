/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export  class TSFinanzielleSituationSelbstdeklaration extends TSAbstractMutableEntity {
    private _einkunftErwerb: number;
    private _einkunftVersicherung: number;
    private _einkunftAusgleichskassen: number;
    private _einkunftWertschriften: number;
    private _einkunftUnterhaltsbeitragSteuerpflichtige: number;
    private _einkunftUnterhaltsbeitragKinder: number;
    private _einkunftUeberige: number;
    private _einkunftLiegenschaften: number;
    private _abzugBerufsauslagen: number;
    private _abzugSchuldzinsen: number;
    private _abzugUnterhaltsbeitragEhepartner: number;
    private _abzugUnterhaltsbeitragKinder: number;
    private _abzugRentenleistungen: number;
    private _abzugSaeule3A: number;
    private _abzugVersicherungspraemien: number;
    private _abzugKrankheitsUnfallKosten: number;
    private _abzugFreiweiligeZuwendungPartien: number;
    private _abzugKinderVorschule: number;
    private _abzugKinderSchule: number;
    private _abzugKinderAuswaertigerAufenthalt: number;
    private _abzugEigenbetreuung: number;
    private _abzugFremdbetreuung: number;
    private _abzugErwerbsunfaehigePersonen: number;
    private _vermoegen: number;
    private _abzugSteuerfreierBetragErwachsene: number;
    private _abzugSteuerfreierBetragKinder: number;

    public get einkunftErwerb(): number {
        return this._einkunftErwerb;
    }

    public set einkunftErwerb(value: number) {
        this._einkunftErwerb = value;
    }

    public get einkunftVersicherung(): number {
        return this._einkunftVersicherung;
    }

    public set einkunftVersicherung(value: number) {
        this._einkunftVersicherung = value;
    }

    public get einkunftAusgleichskassen(): number {
        return this._einkunftAusgleichskassen;
    }

    public set einkunftAusgleichskassen(value: number) {
        this._einkunftAusgleichskassen = value;
    }

    public get einkunftWertschriften(): number {
        return this._einkunftWertschriften;
    }

    public set einkunftWertschriften(value: number) {
        this._einkunftWertschriften = value;
    }

    public get einkunftUnterhaltsbeitragSteuerpflichtige(): number {
        return this._einkunftUnterhaltsbeitragSteuerpflichtige;
    }

    public set einkunftUnterhaltsbeitragSteuerpflichtige(value: number) {
        this._einkunftUnterhaltsbeitragSteuerpflichtige = value;
    }

    public get einkunftUnterhaltsbeitragKinder(): number {
        return this._einkunftUnterhaltsbeitragKinder;
    }

    public set einkunftUnterhaltsbeitragKinder(value: number) {
        this._einkunftUnterhaltsbeitragKinder = value;
    }

    public get einkunftUeberige(): number {
        return this._einkunftUeberige;
    }

    public set einkunftUeberige(value: number) {
        this._einkunftUeberige = value;
    }

    public get einkunftLiegenschaften(): number {
        return this._einkunftLiegenschaften;
    }

    public set einkunftLiegenschaften(value: number) {
        this._einkunftLiegenschaften = value;
    }

    public get abzugBerufsauslagen(): number {
        return this._abzugBerufsauslagen;
    }

    public set abzugBerufsauslagen(value: number) {
        this._abzugBerufsauslagen = value;
    }

    public get abzugSchuldzinsen(): number {
        return this._abzugSchuldzinsen;
    }

    public set abzugSchuldzinsen(value: number) {
        this._abzugSchuldzinsen = value;
    }

    public get abzugUnterhaltsbeitragEhepartner(): number {
        return this._abzugUnterhaltsbeitragEhepartner;
    }

    public set abzugUnterhaltsbeitragEhepartner(value: number) {
        this._abzugUnterhaltsbeitragEhepartner = value;
    }

    public get abzugUnterhaltsbeitragKinder(): number {
        return this._abzugUnterhaltsbeitragKinder;
    }

    public set abzugUnterhaltsbeitragKinder(value: number) {
        this._abzugUnterhaltsbeitragKinder = value;
    }

    public get abzugRentenleistungen(): number {
        return this._abzugRentenleistungen;
    }

    public set abzugRentenleistungen(value: number) {
        this._abzugRentenleistungen = value;
    }

    public get abzugSaeule3A(): number {
        return this._abzugSaeule3A;
    }

    public set abzugSaeule3A(value: number) {
        this._abzugSaeule3A = value;
    }

    public get abzugVersicherungspraemien(): number {
        return this._abzugVersicherungspraemien;
    }

    public set abzugVersicherungspraemien(value: number) {
        this._abzugVersicherungspraemien = value;
    }

    public get abzugKrankheitsUnfallKosten(): number {
        return this._abzugKrankheitsUnfallKosten;
    }

    public set abzugKrankheitsUnfallKosten(value: number) {
        this._abzugKrankheitsUnfallKosten = value;
    }

    public get abzugFreiweiligeZuwendungPartien(): number {
        return this._abzugFreiweiligeZuwendungPartien;
    }

    public set abzugFreiweiligeZuwendungPartien(value: number) {
        this._abzugFreiweiligeZuwendungPartien = value;
    }

    public get abzugKinderVorschule(): number {
        return this._abzugKinderVorschule;
    }

    public set abzugKinderVorschule(value: number) {
        this._abzugKinderVorschule = value;
    }

    public get abzugKinderSchule(): number {
        return this._abzugKinderSchule;
    }

    public set abzugKinderSchule(value: number) {
        this._abzugKinderSchule = value;
    }

    public get abzugKinderAuswaertigerAufenthalt(): number {
        return this._abzugKinderAuswaertigerAufenthalt;
    }

    public set abzugKinderAuswaertigerAufenthalt(value: number) {
        this._abzugKinderAuswaertigerAufenthalt = value;
    }

    public get abzugEigenbetreuung(): number {
        return this._abzugEigenbetreuung;
    }

    public set abzugEigenbetreuung(value: number) {
        this._abzugEigenbetreuung = value;
    }

    public get abzugFremdbetreuung(): number {
        return this._abzugFremdbetreuung;
    }

    public set abzugFremdbetreuung(value: number) {
        this._abzugFremdbetreuung = value;
    }

    public get abzugErwerbsunfaehigePersonen(): number {
        return this._abzugErwerbsunfaehigePersonen;
    }

    public set abzugErwerbsunfaehigePersonen(value: number) {
        this._abzugErwerbsunfaehigePersonen = value;
    }

    public get vermoegen(): number {
        return this._vermoegen;
    }

    public set vermoegen(value: number) {
        this._vermoegen = value;
    }

    public get abzugSteuerfreierBetragErwachsene(): number {
        return this._abzugSteuerfreierBetragErwachsene;
    }

    public set abzugSteuerfreierBetragErwachsene(value: number) {
        this._abzugSteuerfreierBetragErwachsene = value;
    }

    public get abzugSteuerfreierBetragKinder(): number {
        return this._abzugSteuerfreierBetragKinder;
    }

    public set abzugSteuerfreierBetragKinder(value: number) {
        this._abzugSteuerfreierBetragKinder = value;
    }

}
