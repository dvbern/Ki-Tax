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
import {TSUnterhaltsvereinbarungAnswer} from './enums/TSUnterhaltsvereinbarungAnswer';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSAdresse} from './TSAdresse';
import {TSGesuchsperiode} from './TSGesuchsperiode';

export class TSFamiliensituation extends TSAbstractMutableEntity {

    private _familienstatus: TSFamilienstatus;
    private _gemeinsameSteuererklaerung: boolean;
    private _aenderungPer: moment.Moment;
    private _startKonkubinat: moment.Moment;
    private _sozialhilfeBezueger: boolean;
    private _verguenstigungGewuenscht: boolean;
    private _keineMahlzeitenverguenstigungBeantragt: boolean;
    private _ibanMahlzeiten: string;
    private _kontoinhaberMahlzeiten: string;
    private _abweichendeZahlungsadresseMahlzeiten: boolean;
    private _zahlungsadresseMahlzeiten: TSAdresse;

    private _ibanInfoma: string;
    private _kontoinhaberInfoma: string;
    private _abweichendeZahlungsadresseInfoma: boolean;
    private _zahlungsadresseInfoma: TSAdresse;
    private _infomaKreditorennummer: string;
    private _infomaBankcode: string;
    private _gesuchstellerKardinalitaet: TSGesuchstellerKardinalitaet;
    private _fkjvFamSit: boolean;
    private _minDauerKonkubinat: number;
    private _unterhaltsvereinbarung: TSUnterhaltsvereinbarungAnswer;
    private _unterhaltsvereinbarungBemerkung: string;
    private _geteilteObhut: boolean;

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

    public get ibanMahlzeiten(): string {
        return this._ibanMahlzeiten;
    }

    public set ibanMahlzeiten(value: string) {
        this._ibanMahlzeiten = value;
    }

    public get kontoinhaberMahlzeiten(): string {
        return this._kontoinhaberMahlzeiten;
    }

    public set kontoinhaberMahlzeiten(value: string) {
        this._kontoinhaberMahlzeiten = value;
    }

    public get abweichendeZahlungsadresseMahlzeiten(): boolean {
        return this._abweichendeZahlungsadresseMahlzeiten;
    }

    public set abweichendeZahlungsadresseMahlzeiten(value: boolean) {
        this._abweichendeZahlungsadresseMahlzeiten = value;
    }

    public get zahlungsadresseMahlzeiten(): TSAdresse {
        return this._zahlungsadresseMahlzeiten;
    }

    public set zahlungsadresseMahlzeiten(value: TSAdresse) {
        this._zahlungsadresseMahlzeiten = value;
    }

    public get ibanInfoma(): string {
        return this._ibanInfoma;
    }

    public set ibanInfoma(value: string) {
        this._ibanInfoma = value;
    }

    public get kontoinhaberInfoma(): string {
        return this._kontoinhaberInfoma;
    }

    public set kontoinhaberInfoma(value: string) {
        this._kontoinhaberInfoma = value;
    }

    public get abweichendeZahlungsadresseInfoma(): boolean {
        return this._abweichendeZahlungsadresseInfoma;
    }

    public set abweichendeZahlungsadresseInfoma(value: boolean) {
        this._abweichendeZahlungsadresseInfoma = value;
    }

    public get zahlungsadresseInfoma(): TSAdresse {
        return this._zahlungsadresseInfoma;
    }

    public set zahlungsadresseInfoma(value: TSAdresse) {
        this._zahlungsadresseInfoma = value;
    }

    public get infomaKreditorennummer(): string {
        return this._infomaKreditorennummer;
    }

    public set infomaKreditorennummer(value: string) {
        this._infomaKreditorennummer = value;
    }

    public get infomaBankcode(): string {
        return this._infomaBankcode;
    }

    public set infomaBankcode(value: string) {
        this._infomaBankcode = value;
    }

    public get gesuchstellerKardinalitaet(): TSGesuchstellerKardinalitaet {
        return this._gesuchstellerKardinalitaet;
    }

    public set gesuchstellerKardinalitaet(value: TSGesuchstellerKardinalitaet) {
        this._gesuchstellerKardinalitaet = value;
    }

    public hasSecondGesuchsteller(endOfPeriode: moment.Moment): boolean {
        switch (this.familienstatus) {
            case TSFamilienstatus.ALLEINERZIEHEND:
                if (!this.fkjvFamSit) {
                    return false;
                }
                return this.hasSecondGesuchstellerFKJV();
            case TSFamilienstatus.KONKUBINAT_KEIN_KIND:
                // falls das Konkubinat irgendwann in der Periode länger als 2 Jahre dauert,
                // benötigen wir sowieso einen zweiten Gesuchsteller
                if (this.konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(endOfPeriode)) {
                    return true;
                }
                // falls Konkubinat kürzer als zwei Jahre ist, wird ein Fragebaum für FKJV angezeigt. Wir
                // schauen ob die Antworten dort einen zweiten Antragsteller verlangen
                if (this.fkjvFamSit) {
                    return this.hasSecondGesuchstellerFKJV();
                }
                return false;
            case TSFamilienstatus.VERHEIRATET:
            case TSFamilienstatus.KONKUBINAT:
                return true;
            default:
                throw new Error(`hasSecondGesuchsteller is not implemented for status ${this.familienstatus}`);
        }
    }

    /**
     * Wir schauen, ob es einen Zeitabschnitt in der Periode gibt, an dem das Konkubinat noch nicht X Jahre alt ist.
     * Z.B. Periode 22/23 und Start Konkubinat 1.11.2020: Zwischen 1.8.2022 und 31.10.2022 ist das Konkubinat jünger
     * als 2 Jahre und die Funktion gibt true zurück.
     */
    public konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(periode: TSGesuchsperiode): boolean {
        if (!this.startKonkubinat) {
            return false;
        }
        const konkubinatEndOfMonth = moment(this.startKonkubinat).endOf('month');
        const konkubinatPlusYears = konkubinatEndOfMonth.add(this.minDauerKonkubinat, 'years');
        return konkubinatPlusYears.isAfter(periode.gueltigkeit.gueltigAb);
    }

    /**
     * Wir prüfen, ob das Konkubinat irgendwann in der Periode mindestens zwei Jahre alt ist.
     * z.B. Periode 22/23, Start Konkubinat 1.11.2020 => zwei Jahre am 1.11.2022 erreicht => true
     */
    public konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(endOfPeriode: moment.Moment): boolean {
        const konkubinatEndOfMonth = moment(this.startKonkubinat).endOf('month');
        const konkubinatPlusYears = konkubinatEndOfMonth.add(this.minDauerKonkubinat, 'years');
        return konkubinatPlusYears.isSameOrBefore(endOfPeriode);
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

    public get unterhaltsvereinbarung(): TSUnterhaltsvereinbarungAnswer {
        return this._unterhaltsvereinbarung;
    }

    public set unterhaltsvereinbarung(value: TSUnterhaltsvereinbarungAnswer) {
        this._unterhaltsvereinbarung = value;
    }

    public get unterhaltsvereinbarungBemerkung(): string {
        return this._unterhaltsvereinbarungBemerkung;
    }

    public set unterhaltsvereinbarungBemerkung(value: string) {
        this._unterhaltsvereinbarungBemerkung = value;
    }

    public get geteilteObhut(): boolean {
        return this._geteilteObhut;
    }

    public set geteilteObhut(value: boolean) {
        this._geteilteObhut = value;
    }

    private hasSecondGesuchstellerFKJV(): boolean {
        if (this.geteilteObhut) {
            return this.gesuchstellerKardinalitaet === TSGesuchstellerKardinalitaet.ZU_ZWEIT;
        }

        return this.unterhaltsvereinbarung === TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
    }
}
