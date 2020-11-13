/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import * as moment from 'moment';
import {EbeguUtil} from '../utils/EbeguUtil';
import {TSBetreuungsangebotTyp} from './enums/TSBetreuungsangebotTyp';
import {TSRueckforderungInstitutionTyp} from './enums/TSRueckforderungInstitutionTyp';
import {TSRueckforderungStatus} from './enums/TSRueckforderungStatus';
import {TSSprache} from './enums/TSSprache';
import {TSAbstractEntity} from './TSAbstractEntity';
import {TSInstitutionStammdatenSummary} from './TSInstitutionStammdatenSummary';
import {TSRueckforderungMitteilung} from './TSRueckforderungMitteilung';

export class TSRueckforderungFormular extends TSAbstractEntity {

    private _institutionStammdaten: TSInstitutionStammdatenSummary;
    private _rueckforderungMitteilungen: TSRueckforderungMitteilung[];
    private _status: TSRueckforderungStatus;
    public hasBeenProvisorisch: boolean = false;
    private _stufe1KantonKostenuebernahmeAnzahlStunden: number;
    private _stufe1InstitutionKostenuebernahmeAnzahlStunden: number;
    private _stufe2KantonKostenuebernahmeAnzahlStunden: number;
    private _stufe2InstitutionKostenuebernahmeAnzahlStunden: number;
    private _stufe1KantonKostenuebernahmeAnzahlTage: number;
    private _stufe1InstitutionKostenuebernahmeAnzahlTage: number;
    private _stufe2KantonKostenuebernahmeAnzahlTage: number;
    private _stufe2InstitutionKostenuebernahmeAnzahlTage: number;
    private _stufe1KantonKostenuebernahmeBetreuung: number;
    private _stufe1InstitutionKostenuebernahmeBetreuung: number;
    private _stufe2KantonKostenuebernahmeBetreuung: number;
    private _stufe2InstitutionKostenuebernahmeBetreuung: number;
    private _stufe1FreigabeBetrag: number;
    private _stufe1FreigabeDatum: moment.Moment;
    private _stufe1FreigabeAusbezahltAm: moment.Moment;
    private _stufe2VerfuegungBetrag: number;
    private _stufe2VerfuegungDatum: moment.Moment;
    private _stufe2VerfuegungAusbezahltAm: moment.Moment;
    private _verantwortlicherName: string;
    private _uncheckedDocuments: string;
    public institutionTyp: TSRueckforderungInstitutionTyp;
    public extendedEinreichefrist: moment.Moment;
    public relevantEinreichungsfrist: moment.Moment;
    public betragEntgangeneElternbeitraege: number;
    public betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten: number; // Kita in TAGE, TFO in STUNDEN
    public anzahlNichtAngeboteneEinheiten: number; // Neu: Rueckerstattung fuer nicht angebotene Einheiten
    public kurzarbeitBeantragt: boolean;
    public kurzarbeitBetrag: number;
    public kurzarbeitDefinitivVerfuegt: boolean;
    public kurzarbeitKeinAntragBegruendung: string;
    public kurzarbeitSonstiges: string;
    public coronaErwerbsersatzBeantragt: boolean;
    public coronaErwerbsersatzBetrag: number;
    public coronaErwerbsersatzDefinitivVerfuegt: boolean;
    public coronaErwerbsersatzKeinAntragBegruendung: string;
    public coronaErwerbsersatzSonstiges: string;
    public korrespondenzSprache: TSSprache;
    public bemerkungFuerVerfuegung: string;
    public beschwerdeBetrag: number;
    public beschwerdeBemerkung: string;

    public constructor() {
        super();
    }

    public get institutionStammdaten(): TSInstitutionStammdatenSummary {
        return this._institutionStammdaten;
    }

    public set institutionStammdaten(value: TSInstitutionStammdatenSummary) {
        this._institutionStammdaten = value;
    }

    public get rueckforderungMitteilungen(): TSRueckforderungMitteilung[] {
        return this._rueckforderungMitteilungen;
    }

    public set rueckforderungMitteilungen(value: TSRueckforderungMitteilung[]) {
        this._rueckforderungMitteilungen = value;
    }

    public get status(): TSRueckforderungStatus {
        return this._status;
    }

    public set status(value: TSRueckforderungStatus) {
        this._status = value;
    }

    public get stufe1KantonKostenuebernahmeAnzahlStunden(): number {
        return this._stufe1KantonKostenuebernahmeAnzahlStunden;
    }

    public set stufe1KantonKostenuebernahmeAnzahlStunden(value: number) {
        this._stufe1KantonKostenuebernahmeAnzahlStunden = value;
    }

    public get stufe1InstitutionKostenuebernahmeAnzahlStunden(): number {
        return this._stufe1InstitutionKostenuebernahmeAnzahlStunden;
    }

    public set stufe1InstitutionKostenuebernahmeAnzahlStunden(value: number) {
        this._stufe1InstitutionKostenuebernahmeAnzahlStunden = value;
    }

    public get stufe2KantonKostenuebernahmeAnzahlStunden(): number {
        return this._stufe2KantonKostenuebernahmeAnzahlStunden;
    }

    public set stufe2KantonKostenuebernahmeAnzahlStunden(value: number) {
        this._stufe2KantonKostenuebernahmeAnzahlStunden = value;
    }

    public get stufe2InstitutionKostenuebernahmeAnzahlStunden(): number {
        return this._stufe2InstitutionKostenuebernahmeAnzahlStunden;
    }

    public set stufe2InstitutionKostenuebernahmeAnzahlStunden(value: number) {
        this._stufe2InstitutionKostenuebernahmeAnzahlStunden = value;
    }

    public get stufe1KantonKostenuebernahmeAnzahlTage(): number {
        return this._stufe1KantonKostenuebernahmeAnzahlTage;
    }

    public set stufe1KantonKostenuebernahmeAnzahlTage(value: number) {
        this._stufe1KantonKostenuebernahmeAnzahlTage = value;
    }

    public get stufe1InstitutionKostenuebernahmeAnzahlTage(): number {
        return this._stufe1InstitutionKostenuebernahmeAnzahlTage;
    }

    public set stufe1InstitutionKostenuebernahmeAnzahlTage(value: number) {
        this._stufe1InstitutionKostenuebernahmeAnzahlTage = value;
    }

    public get stufe2KantonKostenuebernahmeAnzahlTage(): number {
        return this._stufe2KantonKostenuebernahmeAnzahlTage;
    }

    public set stufe2KantonKostenuebernahmeAnzahlTage(value: number) {
        this._stufe2KantonKostenuebernahmeAnzahlTage = value;
    }

    public get stufe2InstitutionKostenuebernahmeAnzahlTage(): number {
        return this._stufe2InstitutionKostenuebernahmeAnzahlTage;
    }

    public set stufe2InstitutionKostenuebernahmeAnzahlTage(value: number) {
        this._stufe2InstitutionKostenuebernahmeAnzahlTage = value;
    }

    public get stufe1KantonKostenuebernahmeBetreuung(): number {
        return this._stufe1KantonKostenuebernahmeBetreuung;
    }

    public set stufe1KantonKostenuebernahmeBetreuung(value: number) {
        this._stufe1KantonKostenuebernahmeBetreuung = value;
    }

    public get stufe1InstitutionKostenuebernahmeBetreuung(): number {
        return this._stufe1InstitutionKostenuebernahmeBetreuung;
    }

    public set stufe1InstitutionKostenuebernahmeBetreuung(value: number) {
        this._stufe1InstitutionKostenuebernahmeBetreuung = value;
    }

    public get stufe2KantonKostenuebernahmeBetreuung(): number {
        return this._stufe2KantonKostenuebernahmeBetreuung;
    }

    public set stufe2KantonKostenuebernahmeBetreuung(value: number) {
        this._stufe2KantonKostenuebernahmeBetreuung = value;
    }

    public get stufe2InstitutionKostenuebernahmeBetreuung(): number {
        return this._stufe2InstitutionKostenuebernahmeBetreuung;
    }

    public set stufe2InstitutionKostenuebernahmeBetreuung(value: number) {
        this._stufe2InstitutionKostenuebernahmeBetreuung = value;
    }

    public get stufe1FreigabeBetrag(): number {
        return this._stufe1FreigabeBetrag;
    }

    public set stufe1FreigabeBetrag(value: number) {
        this._stufe1FreigabeBetrag = value;
    }

    public get stufe1FreigabeDatum(): moment.Moment {
        return this._stufe1FreigabeDatum;
    }

    public set stufe1FreigabeDatum(value: moment.Moment) {
        this._stufe1FreigabeDatum = value;
    }

    public get stufe1FreigabeAusbezahltAm(): moment.Moment {
        return this._stufe1FreigabeAusbezahltAm;
    }

    public set stufe1FreigabeAusbezahltAm(value: moment.Moment) {
        this._stufe1FreigabeAusbezahltAm = value;
    }

    public get stufe2VerfuegungBetrag(): number {
        return this._stufe2VerfuegungBetrag;
    }

    public set stufe2VerfuegungBetrag(value: number) {
        this._stufe2VerfuegungBetrag = value;
    }

    public get stufe2VerfuegungDatum(): moment.Moment {
        return this._stufe2VerfuegungDatum;
    }

    public set stufe2VerfuegungDatum(value: moment.Moment) {
        this._stufe2VerfuegungDatum = value;
    }

    public get stufe2VerfuegungAusbezahltAm(): moment.Moment {
        return this._stufe2VerfuegungAusbezahltAm;
    }

    public set stufe2VerfuegungAusbezahltAm(value: moment.Moment) {
        this._stufe2VerfuegungAusbezahltAm = value;
    }

    public get verantwortlicherName(): string {
        return this._verantwortlicherName;
    }

    public set verantwortlicherName(value: string) {
        this._verantwortlicherName = value;
    }

    public get uncheckedDocuments(): string {
        return this._uncheckedDocuments;
    }

    public set uncheckedDocuments(value: string) {
        this._uncheckedDocuments = value;
    }

    public isKurzarbeitProzessBeendet(): boolean {
        return EbeguUtil.isNullOrUndefined(this.kurzarbeitBeantragt)
        || EbeguUtil.isNotNullAndFalse(this.kurzarbeitBeantragt)
        || (EbeguUtil.isNotNullAndTrue(this.kurzarbeitDefinitivVerfuegt));
    }

    public isCoronaErwerbsersatzProzessBeendet(): boolean {
        return EbeguUtil.isNullOrUndefined(this.coronaErwerbsersatzBeantragt)
            || EbeguUtil.isNotNullAndFalse(this.coronaErwerbsersatzBeantragt)
            || (EbeguUtil.isNotNullAndTrue(this.coronaErwerbsersatzDefinitivVerfuegt));
    }

    public isStufe1(): boolean {
        return EbeguUtil.isNullOrUndefined(this.institutionTyp);
    }

    public isOeffentlich(): boolean {
        return !this.isStufe1() && this.institutionTyp === TSRueckforderungInstitutionTyp.OEFFENTLICH;
    }

    public isPrivat(): boolean {
        return !this.isStufe1() && this.institutionTyp === TSRueckforderungInstitutionTyp.PRIVAT;
    }

    public isKitaAngebot(): boolean {
        return this.institutionStammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA;
    }

    public calculateProvisorischerBetrag(): number {
        let result = 0;
        // tslint:disable-next-line:prefer-conditional-expression
        if (this.isStufe1() || this.isOeffentlich()) {
            result = this.calculateProvisorischerBetragStufe1OrOeffentlich();
        } else {
            result = this.calculateProvisorischerBetragStufe2Privat();
        }
        result = Math.max(result, 0);
        return EbeguUtil.roundToFiveRappen(result);
    }

    private calculateProvisorischerBetragStufe1OrOeffentlich(): number {
        const kostenuebernahmeBetreuung =
            EbeguUtil.isNotNullOrUndefined(this.getActiveKostenuebernahmeBetreuung())
            ? this.getActiveKostenuebernahmeBetreuung() : 0;
        const kostenuebernahmeAnzahlTage =
            EbeguUtil.isNotNullOrUndefined(this.getActiveKostenuebernahmeAnzahlTage())
            ? this.getActiveKostenuebernahmeAnzahlTage() : 0;
        const kostenuebernahmeAnzahlStunden =
            EbeguUtil.isNotNullOrUndefined(this.getActiveKostenuebernahmeAnzahlStunden())
            ? this.getActiveKostenuebernahmeAnzahlStunden() : 0;
        if (this.isKitaAngebot()) {
            return kostenuebernahmeAnzahlTage + kostenuebernahmeBetreuung;
        }
        return kostenuebernahmeAnzahlStunden + kostenuebernahmeBetreuung;
    }

    private calculateProvisorischerBetragStufe2Privat(): number {
        const entgangeneElternbeitraege = EbeguUtil.isNotNullOrUndefined(this.betragEntgangeneElternbeitraege)
            ? this.betragEntgangeneElternbeitraege : 0;
        const entgangeneElternbeitraegeNichtAngeboten = EbeguUtil.isNotNullOrUndefined(this.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten)
            ? this.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten : 0;
        const rueckerstattungNichtAngeboten = EbeguUtil.isNotNullOrUndefined(this.anzahlNichtAngeboteneEinheiten)
            ? this.anzahlNichtAngeboteneEinheiten : 0;
        const kurzarbeitBetrag = EbeguUtil.isNotNullOrUndefined(this.kurzarbeitBetrag) && EbeguUtil.isNotNullAndTrue(this.kurzarbeitBeantragt)
            ? this.kurzarbeitBetrag : 0;
        const coronaErerbsersatzBetrag =
            EbeguUtil.isNotNullOrUndefined(this.coronaErwerbsersatzBetrag)
                && EbeguUtil.isNotNullAndTrue(this.coronaErwerbsersatzBeantragt)
            ? this.coronaErwerbsersatzBetrag : 0;
        let result = 0;
        // (2.1) Privat mit Kurzarbeit
        if (this.kurzarbeitBeantragt) {
            result = entgangeneElternbeitraege - kurzarbeitBetrag - coronaErerbsersatzBetrag;
        } else if (EbeguUtil.isNullOrUndefined(this.anzahlNichtAngeboteneEinheiten)) {
            // (2.2) Privat, ohne Kurzarbeit, ohne nicht angebotene Tage
            result = entgangeneElternbeitraege - coronaErerbsersatzBetrag;
        } else {
            // (2.3) Privat, ohne Kurzarbeit, mit nicht angebotene Tage
            result = entgangeneElternbeitraege
                - entgangeneElternbeitraegeNichtAngeboten
                - coronaErerbsersatzBetrag
                + rueckerstattungNichtAngeboten;
        }
        return result;
    }

    private getActiveKostenuebernahmeBetreuung(): number {
        if (this.isStufe1()) {
            if (EbeguUtil.isNotNullOrUndefined(this.stufe1KantonKostenuebernahmeBetreuung)) {
                return this.stufe1KantonKostenuebernahmeBetreuung;
            }
            return this.stufe1InstitutionKostenuebernahmeBetreuung;
        }
        if (this.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2
            || this.status === TSRueckforderungStatus.VERFUEGT) {
            return this.stufe2KantonKostenuebernahmeBetreuung;
        }
        return this.stufe2InstitutionKostenuebernahmeBetreuung;
    }

    private getActiveKostenuebernahmeAnzahlTage(): number {
        if (this.isStufe1()) {
            if (this.stufe1KantonKostenuebernahmeAnzahlTage) {
                return this.stufe1KantonKostenuebernahmeAnzahlTage;
            }
            return this.stufe1InstitutionKostenuebernahmeAnzahlTage;
        }
        if (this.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2
            || this.status === TSRueckforderungStatus.VERFUEGT) {
            return this.stufe2KantonKostenuebernahmeAnzahlTage;
        }
        return this.stufe2InstitutionKostenuebernahmeAnzahlTage;
    }

    private getActiveKostenuebernahmeAnzahlStunden(): number {
        if (this.isStufe1()) {
            if (this.stufe1KantonKostenuebernahmeAnzahlStunden) {
                return this.stufe1KantonKostenuebernahmeAnzahlStunden;
            }
            return this.stufe1InstitutionKostenuebernahmeAnzahlStunden;
        }
        if (this.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2
            || this.status === TSRueckforderungStatus.VERFUEGT) {
            return this.stufe2KantonKostenuebernahmeAnzahlStunden;
        }
        return this.stufe2InstitutionKostenuebernahmeAnzahlStunden;
    }
}
