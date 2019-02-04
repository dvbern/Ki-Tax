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
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';

export default class TSVerfuegungZeitabschnitt extends TSAbstractDateRangedEntity {

    private _erwerbspensumGS1: number;
    private _erwerbspensumGS2: number;
    private _betreuungspensum: number;
    private _fachstellenpensum: number;
    private _anspruchspensumRest: number;
    private _anspruchberechtigtesPensum: number;
    private _bgPensum: number;
    private _betreuungsstunden: number;
    private _vollkosten: number;
    private _verguenstigungOhneBeruecksichtigungVollkosten: number;
    private _verguenstigungOhneBeruecksichtigungMinimalbeitrag: number;
    private _verguenstigung: number;
    private _minimalerElternbeitrag: number;
    private _elternbeitrag: number;
    private _abzugFamGroesse: number;
    private _famGroesse: number;
    private _massgebendesEinkommenVorAbzugFamgr: number;
    private _bemerkungen: string;
    private _zahlungsstatus: TSVerfuegungZeitabschnittZahlungsstatus;
    private _einkommensjahr: number;
    private _kategorieMaxEinkommen: boolean;
    private _kategorieKeinPensum: boolean;
    private _zuSpaetEingereicht: boolean;
    private _sameVerfuegungsdaten: boolean;
    private _sameVerguenstigung: boolean;

    public get erwerbspensumGS1(): number {
        return this._erwerbspensumGS1;
    }

    public set erwerbspensumGS1(value: number) {
        this._erwerbspensumGS1 = value;
    }

    public get erwerbspensumGS2(): number {
        return this._erwerbspensumGS2;
    }

    public set erwerbspensumGS2(value: number) {
        this._erwerbspensumGS2 = value;
    }

    public get betreuungspensum(): number {
        return this._betreuungspensum;
    }

    public set betreuungspensum(value: number) {
        this._betreuungspensum = value;
    }

    public get fachstellenpensum(): number {
        return this._fachstellenpensum;
    }

    public set fachstellenpensum(value: number) {
        this._fachstellenpensum = value;
    }

    public get anspruchspensumRest(): number {
        return this._anspruchspensumRest;
    }

    public set anspruchspensumRest(value: number) {
        this._anspruchspensumRest = value;
    }

    public get anspruchberechtigtesPensum(): number {
        return this._anspruchberechtigtesPensum;
    }

    public set anspruchberechtigtesPensum(value: number) {
        this._anspruchberechtigtesPensum = value;
    }

    public get bgPensum(): number {
        return this._bgPensum;
    }

    public set bgPensum(value: number) {
        this._bgPensum = value;
    }

    public get betreuungsstunden(): number {
        return this._betreuungsstunden;
    }

    public set betreuungsstunden(value: number) {
        this._betreuungsstunden = value;
    }

    public get vollkosten(): number {
        return this._vollkosten;
    }

    public set vollkosten(value: number) {
        this._vollkosten = value;
    }

    public get verguenstigungOhneBeruecksichtigungVollkosten(): number {
        return this._verguenstigungOhneBeruecksichtigungVollkosten;
    }

    public set verguenstigungOhneBeruecksichtigungVollkosten(value: number) {
        this._verguenstigungOhneBeruecksichtigungVollkosten = value;
    }

    public get verguenstigungOhneBeruecksichtigungMinimalbeitrag(): number {
        return this._verguenstigungOhneBeruecksichtigungMinimalbeitrag;
    }

    public set verguenstigungOhneBeruecksichtigungMinimalbeitrag(value: number) {
        this._verguenstigungOhneBeruecksichtigungMinimalbeitrag = value;
    }

    public get verguenstigung(): number {
        return this._verguenstigung;
    }

    public set verguenstigung(value: number) {
        this._verguenstigung = value;
    }

    public get minimalerElternbeitrag(): number {
        return this._minimalerElternbeitrag;
    }

    public set minimalerElternbeitrag(value: number) {
        this._minimalerElternbeitrag = value;
    }

    public get elternbeitrag(): number {
        return this._elternbeitrag;
    }

    public set elternbeitrag(value: number) {
        this._elternbeitrag = value;
    }

    public get abzugFamGroesse(): number {
        return this._abzugFamGroesse;
    }

    public set abzugFamGroesse(value: number) {
        this._abzugFamGroesse = value;
    }

    public get massgebendesEinkommenVorAbzugFamgr(): number {
        return this._massgebendesEinkommenVorAbzugFamgr;
    }

    public set massgebendesEinkommenVorAbzugFamgr(value: number) {
        this._massgebendesEinkommenVorAbzugFamgr = value;
    }

    public get bemerkungen(): string {
        return this._bemerkungen;
    }

    public set bemerkungen(value: string) {
        this._bemerkungen = value;
    }

    public get zahlungsstatus(): TSVerfuegungZeitabschnittZahlungsstatus {
        return this._zahlungsstatus;
    }

    public set zahlungsstatus(value: TSVerfuegungZeitabschnittZahlungsstatus) {
        this._zahlungsstatus = value;
    }

    public get famGroesse(): number {
        return this._famGroesse;
    }

    public set famGroesse(value: number) {
        this._famGroesse = value;
    }

    public get einkommensjahr(): number {
        return this._einkommensjahr;
    }

    public set einkommensjahr(value: number) {
        this._einkommensjahr = value;
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

    public get zuSpaetEingereicht(): boolean {
        return this._zuSpaetEingereicht;
    }

    public set zuSpaetEingereicht(value: boolean) {
        this._zuSpaetEingereicht = value;
    }

    public get sameVerfuegungsdaten(): boolean {
        return this._sameVerfuegungsdaten;
    }

    public set sameVerfuegungsdaten(value: boolean) {
        this._sameVerfuegungsdaten = value;
    }

    public get sameVerguenstigung(): boolean {
        return this._sameVerguenstigung;
    }

    public set sameVerguenstigung(value: boolean) {
        this._sameVerguenstigung = value;
    }

    public getMinimalerElternbeitragGekuerzt(): number {
        const vollkostenMinusVerguenstigung = this.vollkosten - this.verguenstigungOhneBeruecksichtigungMinimalbeitrag;
        if (vollkostenMinusVerguenstigung > this.minimalerElternbeitrag) {
            return 0;
        }
        return this.minimalerElternbeitrag - vollkostenMinusVerguenstigung;
    }
}
