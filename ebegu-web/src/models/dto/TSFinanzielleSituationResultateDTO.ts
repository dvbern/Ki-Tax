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

export class TSFinanzielleSituationResultateDTO {
    private _geschaeftsgewinnDurchschnittGesuchsteller1: number;
    private _geschaeftsgewinnDurchschnittGesuchsteller2: number;
    private _einkommenBeiderGesuchsteller: number;
    private _nettovermoegenFuenfProzent: number;
    private _anrechenbaresEinkommen: number;
    private _abzuegeBeiderGesuchsteller: number;
    private _massgebendesEinkVorAbzFamGr: number;
    private _massgebendesEinkVorAbzFamGrGS1: number;
    private _massgebendesEinkVorAbzFamGrGS2: number;
    private _bruttolohnJahrGS1: number;
    private _bruttolohnJahrGS2: number;
    private _einkommenGS1: number;
    private _einkommenGS2: number;
    private _abzuegeGS1: number;
    private _abzuegeGS2: number;
    private _vermoegenXPercentAnrechenbarGS1: number;
    private _vermoegenXPercentAnrechenbarGS2: number;

    public constructor(
        geschaeftsgewinnDurchschnittGesuchsteller1?: number,
        geschaeftsgewinnDurchschnittGesuchsteller2?: number,
        einkommenBeiderGesuchsteller?: number,
        nettovermoegenFuenfProzent?: number,
        anrechenbaresEinkommen?: number,
        abzuegeBeiderGesuchsteller?: number,
        massgebendesEinkVorAbzFamGr?: number
    ) {
        this._geschaeftsgewinnDurchschnittGesuchsteller1 =
            geschaeftsgewinnDurchschnittGesuchsteller1;
        this._geschaeftsgewinnDurchschnittGesuchsteller2 =
            geschaeftsgewinnDurchschnittGesuchsteller2;
        this._einkommenBeiderGesuchsteller = einkommenBeiderGesuchsteller;
        this._nettovermoegenFuenfProzent = nettovermoegenFuenfProzent;
        this._anrechenbaresEinkommen = anrechenbaresEinkommen;
        this._abzuegeBeiderGesuchsteller = abzuegeBeiderGesuchsteller;
        this._massgebendesEinkVorAbzFamGr = massgebendesEinkVorAbzFamGr;
    }

    public get geschaeftsgewinnDurchschnittGesuchsteller1(): number {
        return this._geschaeftsgewinnDurchschnittGesuchsteller1;
    }

    public set geschaeftsgewinnDurchschnittGesuchsteller1(value: number) {
        this._geschaeftsgewinnDurchschnittGesuchsteller1 = value;
    }

    public get geschaeftsgewinnDurchschnittGesuchsteller2(): number {
        return this._geschaeftsgewinnDurchschnittGesuchsteller2;
    }

    public set geschaeftsgewinnDurchschnittGesuchsteller2(value: number) {
        this._geschaeftsgewinnDurchschnittGesuchsteller2 = value;
    }

    public get einkommenBeiderGesuchsteller(): number {
        return this._einkommenBeiderGesuchsteller;
    }

    public set einkommenBeiderGesuchsteller(value: number) {
        this._einkommenBeiderGesuchsteller = value;
    }

    public get nettovermoegenFuenfProzent(): number {
        return this._nettovermoegenFuenfProzent;
    }

    public set nettovermoegenFuenfProzent(value: number) {
        this._nettovermoegenFuenfProzent = value;
    }

    public get anrechenbaresEinkommen(): number {
        return this._anrechenbaresEinkommen;
    }

    public set anrechenbaresEinkommen(value: number) {
        this._anrechenbaresEinkommen = value;
    }

    public get abzuegeBeiderGesuchsteller(): number {
        return this._abzuegeBeiderGesuchsteller;
    }

    public set abzuegeBeiderGesuchsteller(value: number) {
        this._abzuegeBeiderGesuchsteller = value;
    }

    public get massgebendesEinkVorAbzFamGr(): number {
        return this._massgebendesEinkVorAbzFamGr;
    }

    public set massgebendesEinkVorAbzFamGr(value: number) {
        this._massgebendesEinkVorAbzFamGr = value;
    }
    public get massgebendesEinkVorAbzFamGrGS1(): number {
        return this._massgebendesEinkVorAbzFamGrGS1;
    }

    public set massgebendesEinkVorAbzFamGrGS1(value: number) {
        this._massgebendesEinkVorAbzFamGrGS1 = value;
    }

    public get massgebendesEinkVorAbzFamGrGS2(): number {
        return this._massgebendesEinkVorAbzFamGrGS2;
    }

    public set massgebendesEinkVorAbzFamGrGS2(value: number) {
        this._massgebendesEinkVorAbzFamGrGS2 = value;
    }

    public get bruttolohnJahrGS1(): number {
        return this._bruttolohnJahrGS1;
    }

    public set bruttolohnJahrGS1(value: number) {
        this._bruttolohnJahrGS1 = value;
    }

    public get bruttolohnJahrGS2(): number {
        return this._bruttolohnJahrGS2;
    }

    public set bruttolohnJahrGS2(value: number) {
        this._bruttolohnJahrGS2 = value;
    }

    public get einkommenGS1(): number {
        return this._einkommenGS1;
    }

    public set einkommenGS1(value: number) {
        this._einkommenGS1 = value;
    }

    public get einkommenGS2(): number {
        return this._einkommenGS2;
    }

    public set einkommenGS2(value: number) {
        this._einkommenGS2 = value;
    }

    public get abzuegeGS1(): number {
        return this._abzuegeGS1;
    }

    public set abzuegeGS1(value: number) {
        this._abzuegeGS1 = value;
    }

    public get abzuegeGS2(): number {
        return this._abzuegeGS2;
    }

    public set abzuegeGS2(value: number) {
        this._abzuegeGS2 = value;
    }

    public get vermoegenXPercentAnrechenbarGS1(): number {
        return this._vermoegenXPercentAnrechenbarGS1;
    }

    public set vermoegenXPercentAnrechenbarGS1(value: number) {
        this._vermoegenXPercentAnrechenbarGS1 = value;
    }

    public get vermoegenXPercentAnrechenbarGS2(): number {
        return this._vermoegenXPercentAnrechenbarGS2;
    }

    public set vermoegenXPercentAnrechenbarGS2(value: number) {
        this._vermoegenXPercentAnrechenbarGS2 = value;
    }
}
