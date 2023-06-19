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
import {TSAbstractEntity} from '../../../../models/TSAbstractEntity';

export class TSFerienbetreuungBerechnung extends TSAbstractEntity {

    private _pauschaleBetreuungstag: number;
    private _pauschaleBetreuungstagSonderschueler: number;

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
    private _eigenleistungenGemeinde: number;

    private _isDelegationsmodell: boolean;

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
        if (EbeguUtil.isNullOrUndefined(this._eigenleistungenGemeinde)) {
            this._eigenleistungenGemeinde = 0;
        }
        this._totalLeistungenLeistungsvertrag = this._sockelbeitrag
            + this._beitraegeNachAnmeldungen
            - this._vorfinanzierteKantonsbeitraege
            + this._eigenleistungenGemeinde;
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
        if (this._isDelegationsmodell) {
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
        this._sockelbeitrag = this.convertPossibleStringToNumber(value);
    }

    public get beitraegeNachAnmeldungen(): number {
        return this._beitraegeNachAnmeldungen;
    }

    public set beitraegeNachAnmeldungen(value: number) {
        this._beitraegeNachAnmeldungen = this.convertPossibleStringToNumber(value);
    }

    public get vorfinanzierteKantonsbeitraege(): number {
        return this._vorfinanzierteKantonsbeitraege;
    }

    public set vorfinanzierteKantonsbeitraege(value: number) {
        this._vorfinanzierteKantonsbeitraege = this.convertPossibleStringToNumber(value);
    }

    public get eigenleistungenGemeinde(): number {
        return this._eigenleistungenGemeinde;
    }

    public set eigenleistungenGemeinde(value: number) {
        this._eigenleistungenGemeinde = this.convertPossibleStringToNumber(value);
    }

    public get isDelegationsmodell(): boolean {
        return this._isDelegationsmodell;
    }

    public set isDelegationsmodell(value: boolean) {
        this._isDelegationsmodell = value;
    }

    public set totalKosten(value: number) {
        this._totalKosten = value;
    }

    public get totalKosten(): number {
        return this._totalKosten;
    }

    public set totalLeistungenLeistungsvertrag(value: number) {
        this._totalLeistungenLeistungsvertrag = value;
    }

    public get totalLeistungenLeistungsvertrag(): number {
        return this._totalLeistungenLeistungsvertrag;
    }

    public set betreuungstageKinderDieserGemeindeMinusSonderschueler(value: number) {
        this._betreuungstageKinderDieserGemeindeMinusSonderschueler = value;
    }

    public get betreuungstageKinderDieserGemeindeMinusSonderschueler(): number {
        return this._betreuungstageKinderDieserGemeindeMinusSonderschueler;
    }

    public set betreuungstageKinderAndererGemeindeMinusSonderschueler(value: number) {
        this._betreuungstageKinderAndererGemeindeMinusSonderschueler = value;
    }

    public get betreuungstageKinderAndererGemeindeMinusSonderschueler(): number {
        return this._betreuungstageKinderAndererGemeindeMinusSonderschueler;
    }

    public set totalKantonsbeitrag(value: number) {
        this._totalKantonsbeitrag = value;
    }

    public get totalKantonsbeitrag(): number {
        return this._totalKantonsbeitrag;
    }
    public set totalEinnahmen(value: number) {
        this._totalEinnahmen = value;
    }

    public get totalEinnahmen(): number {
        return this._totalEinnahmen;
    }
    public set beitragFuerKinderDerAnbietendenGemeinde(value: number) {
        this._beitragFuerKinderDerAnbietendenGemeinde = value;
    }
    public get beitragFuerKinderDerAnbietendenGemeinde(): number {
        return this._beitragFuerKinderDerAnbietendenGemeinde;
    }
    public set beteiligungDurchAnbietendeGemeinde(value: number) {
        this._beteiligungDurchAnbietendeGemeinde = value;
    }
    public get beteiligungDurchAnbietendeGemeinde(): number {
        return this._beteiligungDurchAnbietendeGemeinde;
    }
    public set beteiligungZuTief(value: boolean) {
        this._beteiligungZuTief = value;
    }
    public get beteiligungZuTief(): boolean {
        return this._beteiligungZuTief;
    }
    public set pauschaleBetreuungstagSonderschueler(value: number) {
        this._pauschaleBetreuungstagSonderschueler = value;
    }
    public set pauschaleBetreuungstag(value: number) {
        this._pauschaleBetreuungstag = value;
    }

    public getVorausschlicherKantonsbetrag(): number {
        if (this._beteiligungZuTief) {
            return 0;
        }

        return this._totalKantonsbeitrag;
    }

    private convertPossibleStringToNumber(val: any): number {
        if (isNaN(val) || EbeguUtil.isNullOrUndefined(val)) {
            return 0;
        }
        return parseFloat(val);
    }
}
