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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {EbeguUtil} from '../../../../utils/EbeguUtil';

export class TSFerienbetreuungBerechnung {

    private readonly _pauschaleBetreuungstag: number;
    private readonly _pauschaleBetreuungstagSonderschueler: number;

    // INPUTS
    private _personalkosten: number;
    private _sachkosten: number;
    private _verpflegungskosten: number;
    private _weitereKosten: number;

    private _anzahlBetreuungstageKinderBern: number;
    private _betreuungstageKinderDieserGemeinde: number;
    private _betreuungstageKinderDieserGemeindeSonderschueler: number;
    private _betreuungstageKinderAndererGemeinde: number;
    private _betreuungstageKinderAndererGemeindenSonderschueler: number;

    private _einnahmenElterngebuehren: number;
    private _weitereEinnahmen: number;
    private _sockelbeitrag: number;
    private _beitraegeNachAnmeldungen: number;
    private _vorfinanzierteKantonsbeitraege: number;

    private _isDeleagtionsmodell: boolean;

    // BERECHNUNGEN
    private _totalKosten: number;
    private _totalLeistungenLeistungsvertrag: number;

    private _betreuungstageKinderDieserGemeindeMinusSonderschueler: number;
    private _betreuungstageKinderAndererGemeindeMinusSonderschueler: number;

    private _totalKantonsbeitrag: number;

    private _totalEinnahmen: number;

    private _beitragFuerKinderDerAnbietendenGemeinde: number;
    private _beteiligungDurchAnbietendeGemeinde: number;
    private _beteiligungZuTief = false;

    public constructor(pauschale: number, pauschaleSonderschueler: number) {
        this._pauschaleBetreuungstag = pauschale;
        this._pauschaleBetreuungstagSonderschueler = pauschaleSonderschueler;
    }

    public calculate(): void {
        this.calculateTotalKosten();
        this.calculateTotalLeistungenLeistungsvertrag();
        this.calculateKinderAnbietendeGemeindeMinusSonderschueler();
        this.calculateKinderAndereGemeindeMinusSonderschueler();
        this.calculateTotalKantonsbeitrag();
        this.calculateTotalEinnahmen();
        this.calculateBeteiligungen();
    }

    private calculateTotalKosten(): void {
        if (EbeguUtil.isNullOrUndefined(this._personalkosten)) {
            this._personalkosten = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._sachkosten)) {
            this._sachkosten = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._verpflegungskosten)) {
            this._verpflegungskosten = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._weitereKosten)) {
            this._weitereKosten = 0;
        }
        this._totalKosten = this._personalkosten
            + this._sachkosten
            + this._verpflegungskosten
            + this._weitereKosten;
    }

    private calculateTotalLeistungenLeistungsvertrag(): void {
        if (EbeguUtil.isNullOrUndefined(this._sockelbeitrag)) {
            this._sockelbeitrag = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._beitraegeNachAnmeldungen)) {
            this._beitraegeNachAnmeldungen = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._vorfinanzierteKantonsbeitraege)) {
            this._vorfinanzierteKantonsbeitraege = 0;
        }
        this._totalLeistungenLeistungsvertrag = this._sockelbeitrag
            + this.beitraegeNachAnmeldungen
            - this._vorfinanzierteKantonsbeitraege;
    }

    private calculateKinderAnbietendeGemeindeMinusSonderschueler(): void {
        if (EbeguUtil.isNullOrUndefined(this._betreuungstageKinderDieserGemeinde)) {
            this._betreuungstageKinderDieserGemeinde = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._betreuungstageKinderDieserGemeindeSonderschueler)) {
            this._betreuungstageKinderDieserGemeindeSonderschueler = 0;
        }
        this._betreuungstageKinderDieserGemeindeMinusSonderschueler =
            this._betreuungstageKinderDieserGemeinde - this._betreuungstageKinderDieserGemeindeSonderschueler;
    }

    private calculateKinderAndereGemeindeMinusSonderschueler(): void {
        if (EbeguUtil.isNullOrUndefined(this._betreuungstageKinderAndererGemeinde)) {
            this._betreuungstageKinderAndererGemeinde = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._betreuungstageKinderAndererGemeindenSonderschueler)) {
            this._betreuungstageKinderAndererGemeindenSonderschueler = 0;
        }
        this._betreuungstageKinderAndererGemeindeMinusSonderschueler =
            this._betreuungstageKinderAndererGemeinde - this._betreuungstageKinderAndererGemeindenSonderschueler;
    }

    private calculateTotalKantonsbeitrag(): void {
        this._totalKantonsbeitrag =
            this._betreuungstageKinderDieserGemeindeMinusSonderschueler * this._pauschaleBetreuungstag
        + this._betreuungstageKinderDieserGemeindeSonderschueler * this._pauschaleBetreuungstagSonderschueler
        + this._betreuungstageKinderAndererGemeindeMinusSonderschueler * this._pauschaleBetreuungstag
        + this._betreuungstageKinderAndererGemeindenSonderschueler * this._pauschaleBetreuungstagSonderschueler;
        this._totalKantonsbeitrag = EbeguUtil.roundToFiveRappen(this._totalKantonsbeitrag);
    }

    private calculateTotalEinnahmen(): void {
        if (EbeguUtil.isNullOrUndefined(this._einnahmenElterngebuehren)) {
            this._einnahmenElterngebuehren = 0;
        }
        if (EbeguUtil.isNullOrUndefined(this._weitereEinnahmen)) {
            this._weitereEinnahmen = 0;
        }
        this._totalEinnahmen = this._einnahmenElterngebuehren + this._weitereEinnahmen;
    }

    private calculateBeteiligungen(): void {
        this._beitragFuerKinderDerAnbietendenGemeinde =
            this._betreuungstageKinderDieserGemeindeMinusSonderschueler * this._pauschaleBetreuungstag
            + this._betreuungstageKinderDieserGemeindeSonderschueler * this._pauschaleBetreuungstagSonderschueler;
        this._beitragFuerKinderDerAnbietendenGemeinde =
            EbeguUtil.roundToFiveRappen(this._beitragFuerKinderDerAnbietendenGemeinde);

        this._beteiligungDurchAnbietendeGemeinde = this.calculateBeteiligungDurchAnbietendeGemeinde();
        this._beteiligungZuTief = this._beteiligungDurchAnbietendeGemeinde < this._beitragFuerKinderDerAnbietendenGemeinde;
    }

    private calculateBeteiligungDurchAnbietendeGemeinde(): number {
        if (this._isDeleagtionsmodell) {
            return this._totalLeistungenLeistungsvertrag;
        }
        return this._totalKosten - this._totalKantonsbeitrag - this._totalEinnahmen;
    }

    public get personalkosten(): number {
        return this._personalkosten;
    }

    public set personalkosten(value: number) {
        this._personalkosten = this.convertPossibleStringToNumber(value);
    }

    public get sachkosten(): number {
        return this._sachkosten;
    }

    public set sachkosten(value: number) {
        this._sachkosten = this.convertPossibleStringToNumber(value);
    }

    public get verpflegungskosten(): number {
        return this._verpflegungskosten;
    }

    public set verpflegungskosten(value: number) {
        this._verpflegungskosten = this.convertPossibleStringToNumber(value);
    }

    public get weitereKosten(): number {
        return this._weitereKosten;
    }

    public set weitereKosten(value: number) {
        this._weitereKosten = this.convertPossibleStringToNumber(value);
    }

    public get anzahlBetreuungstageKinderBern(): number {
        return this._anzahlBetreuungstageKinderBern;
    }

    public set anzahlBetreuungstageKinderBern(value: number) {
        this._anzahlBetreuungstageKinderBern = this.convertPossibleStringToNumber(value);
    }

    public get betreuungstageKinderDieserGemeinde(): number {
        return this._betreuungstageKinderDieserGemeinde;
    }

    public set betreuungstageKinderDieserGemeinde(value: number) {
        this._betreuungstageKinderDieserGemeinde = this.convertPossibleStringToNumber(value);
    }

    public get betreuungstageKinderDieserGemeindeSonderschueler(): number {
        return this._betreuungstageKinderDieserGemeindeSonderschueler;
    }

    public set betreuungstageKinderDieserGemeindeSonderschueler(value: number) {
        this._betreuungstageKinderDieserGemeindeSonderschueler = this.convertPossibleStringToNumber(value);
    }

    public get betreuungstageKinderAndererGemeinde(): number {
        return this._betreuungstageKinderAndererGemeinde;
    }

    public set betreuungstageKinderAndererGemeinde(value: number) {
        this._betreuungstageKinderAndererGemeinde = this.convertPossibleStringToNumber(value);
    }

    public get betreuungstageKinderAndererGemeindenSonderschueler(): number {
        return this._betreuungstageKinderAndererGemeindenSonderschueler;
    }

    public set betreuungstageKinderAndererGemeindenSonderschueler(value: number) {
        this._betreuungstageKinderAndererGemeindenSonderschueler = this.convertPossibleStringToNumber(value);
    }

    public get einnahmenElterngebuehren(): number {
        return this._einnahmenElterngebuehren;
    }

    public set einnahmenElterngebuehren(value: number) {
        this._einnahmenElterngebuehren = this.convertPossibleStringToNumber(value);
    }

    public get weitereEinnahmen(): number {
        return this._weitereEinnahmen;
    }

    public set weitereEinnahmen(value: number) {
        this._weitereEinnahmen = this.convertPossibleStringToNumber(value);
    }

    public get sockelbeitrag(): number {
        return this._sockelbeitrag;
    }

    public set sockelbeitrag(value: number) {
        this._sockelbeitrag = value;
    }

    public get beitraegeNachAnmeldungen(): number {
        return this._beitraegeNachAnmeldungen;
    }

    public set beitraegeNachAnmeldungen(value: number) {
        this._beitraegeNachAnmeldungen = value;
    }

    public get vorfinanzierteKantonsbeitraege(): number {
        return this._vorfinanzierteKantonsbeitraege;
    }

    public set vorfinanzierteKantonsbeitraege(value: number) {
        this._vorfinanzierteKantonsbeitraege = value;
    }

    public get isDeleagtionsmodell(): boolean {
        return this._isDeleagtionsmodell;
    }

    public set isDeleagtionsmodell(value: boolean) {
        this._isDeleagtionsmodell = value;
    }

    public get totalKosten(): number {
        return this._totalKosten;
    }

    public get totalLeistungenLeistungsvertrag(): number {
        return this._totalLeistungenLeistungsvertrag;
    }

    public get betreuungstageKinderDieserGemeindeMinusSonderschueler(): number {
        return this._betreuungstageKinderDieserGemeindeMinusSonderschueler;
    }

    public get betreuungstageKinderAndererGemeindeMinusSonderschueler(): number {
        return this._betreuungstageKinderAndererGemeindeMinusSonderschueler;
    }

    public get totalKantonsbeitrag(): number {
        return this._totalKantonsbeitrag;
    }

    public get totalEinnahmen(): number {
        return this._totalEinnahmen;
    }

    public get beitragFuerKinderDerAnbietendenGemeinde(): number {
        return this._beitragFuerKinderDerAnbietendenGemeinde;
    }

    public get beteiligungDurchAnbietendeGemeinde(): number {
        return this._beteiligungDurchAnbietendeGemeinde;
    }

    public get beteiligungZuTief(): boolean {
        return this._beteiligungZuTief;
    }

    private convertPossibleStringToNumber(val: any): number {
        if (isNaN(val) || EbeguUtil.isNullOrUndefined(val)) {
            return 0;
        }
        return parseFloat(val);
    }
}
