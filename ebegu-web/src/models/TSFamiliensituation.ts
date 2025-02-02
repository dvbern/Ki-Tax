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
import {TSFamilienstatus} from './enums/TSFamilienstatus';
import {TSGesuchstellerKardinalitaet} from './enums/TSGesuchstellerKardinalitaet';
import {TSUnterhaltsvereinbarungAnswer} from './enums/TSUnterhaltsvereinbarungAnswer';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSAdresse} from './TSAdresse';
import {TSGesuchsperiode} from './TSGesuchsperiode';
import {TSDateRange} from './types/TSDateRange';

export class TSFamiliensituation extends TSAbstractMutableEntity {
    private _familienstatus: TSFamilienstatus;
    private _gemeinsameSteuererklaerung: boolean;
    private _aenderungPer: moment.Moment;
    private _startKonkubinat: moment.Moment;
    private _sozialhilfeBezueger: boolean;
    private _zustaendigeAmtsstelle: string;
    private _nameBetreuer: string;
    private _verguenstigungGewuenscht: boolean;
    private _keineMahlzeitenverguenstigungBeantragt: boolean;
    private _keineMahlzeitenverguenstigungBeantragtEditable: boolean;
    private _iban: string;
    private _kontoinhaber: string;
    private _abweichendeZahlungsadresse: boolean;
    private _zahlungsadresse: TSAdresse;

    private _infomaKreditorennummer: string;
    private _infomaBankcode: string;
    private _gesuchstellerKardinalitaet: TSGesuchstellerKardinalitaet;
    private _fkjvFamSit: boolean;
    private _minDauerKonkubinat: number;
    private _unterhaltsvereinbarung: TSUnterhaltsvereinbarungAnswer;
    private _unterhaltsvereinbarungBemerkung: string;
    private _geteilteObhut: boolean;
    private _partnerIdentischMitVorgesuch: boolean;
    private _gemeinsamerHaushaltMitObhutsberechtigterPerson: boolean;
    private _gemeinsamerHaushaltMitPartner: boolean;
    private _auszahlungAusserhalbVonKibon: boolean;

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

    public get zustaendigeAmtsstelle(): string {
        return this._zustaendigeAmtsstelle;
    }

    public set zustaendigeAmtsstelle(value: string) {
        this._zustaendigeAmtsstelle = value;
    }

    public get nameBetreuer(): string {
        return this._nameBetreuer;
    }

    public set nameBetreuer(value: string) {
        this._nameBetreuer = value;
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

    public get keineMahlzeitenverguenstigungBeantragtEditable(): boolean {
        return this._keineMahlzeitenverguenstigungBeantragtEditable;
    }

    public set keineMahlzeitenverguenstigungBeantragtEditable(value: boolean) {
        this._keineMahlzeitenverguenstigungBeantragtEditable = value;
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
            case TSFamilienstatus.SCHWYZ:
                return (
                    EbeguUtil.isNotNullOrUndefined(
                        this.gesuchstellerKardinalitaet
                    ) &&
                    this.gesuchstellerKardinalitaet ===
                        TSGesuchstellerKardinalitaet.ZU_ZWEIT
                );
            case TSFamilienstatus.APPENZELL:
                return this.hasSecondGesuchstellerAppenzell();
            case TSFamilienstatus.ALLEINERZIEHEND:
                if (!this.fkjvFamSit) {
                    return false;
                }
                return this.hasSecondGesuchstellerFKJV();
            case TSFamilienstatus.KONKUBINAT_KEIN_KIND:
                // falls das Konkubinat irgendwann in der Periode länger als 2 Jahre dauert,
                // benötigen wir sowieso einen zweiten Gesuchsteller
                if (
                    this.konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(
                        endOfPeriode
                    )
                ) {
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
                throw new Error(
                    `hasSecondGesuchsteller is not implemented for status ${this.familienstatus}`
                );
        }
    }

    /**
     * Wir schauen, ob es einen Zeitabschnitt in der Periode gibt, an dem das Konkubinat noch nicht X Jahre alt ist.
     * Z.B. Periode 22/23 und Start Konkubinat 1.11.2020: Zwischen 1.8.2022 und 31.10.2022 ist das Konkubinat jünger
     * als 2 Jahre und die Funktion gibt true zurück.
     */
    public konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
        periode: TSGesuchsperiode
    ): boolean {
        if (!this.startKonkubinat) {
            return false;
        }
        const konkubinatPlusYears =
            this.getStartKonkubinatEndofMonthPlusMinDauer();
        return konkubinatPlusYears.isAfter(periode.gueltigkeit.gueltigAb);
    }

    /**
     * Wir prüfen, ob das Konkubinat irgendwann in der Periode mindestens x Jahre alt ist.
     * z.B. Periode 22/23, Start Konkubinat 1.11.2020 => zwei Jahre am 1.11.2022 erreicht => true
     */
    public konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(
        endOfPeriode: moment.Moment
    ): boolean {
        const konkubinatPlusYears = this.getStartKonkubinatPlusMinDauer();
        return konkubinatPlusYears.isSameOrBefore(endOfPeriode);
    }

    public konkuinatOhneKindBecomesXYearsDuringPeriode(
        periode: TSGesuchsperiode
    ): boolean {
        if (this.familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            return false;
        }
        return (
            this.konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
                periode
            ) &&
            this.konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(
                periode.gueltigkeit.gueltigBis
            )
        );
    }

    public isShortKonkubinatForEntirePeriode(
        periode: TSGesuchsperiode
    ): boolean {
        if (this.familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            return false;
        }

        return periode.gueltigkeit.gueltigBis.isBefore(
            this.getStartKonkubinatPlusMinDauer()
        );
    }

    public getStartKonkubinatPlusMinDauer(): moment.Moment {
        const konkubinat_start: moment.Moment = moment(
            this.startKonkubinat.clone()
        );
        return konkubinat_start.add({years: this.minDauerKonkubinat});
    }

    public getStartKonkubinatEndofMonthPlusMinDauer(): moment.Moment {
        return this.getStartKonkubinatPlusMinDauer().endOf('month');
    }

    public isSameFamiliensituation(other: TSFamiliensituation): boolean {
        let same = EbeguUtil.areSameOrWithoutValue(
            this.familienstatus,
            other.familienstatus
        );
        if (
            same &&
            this.familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND
        ) {
            same = this.startKonkubinat.isSame(other.startKonkubinat);
        }
        if (same && this.fkjvFamSit) {
            same =
                EbeguUtil.areSameOrWithoutValue(
                    this.geteilteObhut,
                    other.geteilteObhut
                ) &&
                EbeguUtil.areSameOrWithoutValue(
                    this.unterhaltsvereinbarung,
                    other.unterhaltsvereinbarung
                ) &&
                EbeguUtil.areSameOrWithoutValue(
                    this.gesuchstellerKardinalitaet,
                    other.gesuchstellerKardinalitaet
                );
        }
        if (this.familienstatus === TSFamilienstatus.APPENZELL) {
            same =
                EbeguUtil.areSameOrWithoutValue(
                    this.geteilteObhut,
                    other.geteilteObhut
                ) &&
                EbeguUtil.areSameOrWithoutValue(
                    this.gemeinsamerHaushaltMitObhutsberechtigterPerson,
                    other.gemeinsamerHaushaltMitObhutsberechtigterPerson
                ) &&
                EbeguUtil.areSameOrWithoutValue(
                    this.gemeinsamerHaushaltMitPartner,
                    other.gemeinsamerHaushaltMitPartner
                );
        }
        if (this.familienstatus === TSFamilienstatus.SCHWYZ) {
            same = EbeguUtil.areSameOrWithoutValue(
                this.gesuchstellerKardinalitaet,
                other.gesuchstellerKardinalitaet
            );
        }
        return same;
    }

    public revertFamiliensituation(other: TSFamiliensituation): void {
        this.familienstatus = other.familienstatus;
        this.startKonkubinat = other.startKonkubinat;
        this.gesuchstellerKardinalitaet = other.gesuchstellerKardinalitaet;
        this.unterhaltsvereinbarung = other.unterhaltsvereinbarung;
        this.geteilteObhut = other.geteilteObhut;
        this.unterhaltsvereinbarungBemerkung =
            other.unterhaltsvereinbarungBemerkung;
        this.gemeinsamerHaushaltMitPartner =
            other.gemeinsamerHaushaltMitPartner;
        this.gemeinsamerHaushaltMitObhutsberechtigterPerson =
            other.gemeinsamerHaushaltMitObhutsberechtigterPerson;
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

    public get partnerIdentischMitVorgesuch(): boolean {
        return this._partnerIdentischMitVorgesuch;
    }

    public set partnerIdentischMitVorgesuch(value: boolean) {
        this._partnerIdentischMitVorgesuch = value;
    }

    public get gemeinsamerHaushaltMitPartner(): boolean {
        return this._gemeinsamerHaushaltMitPartner;
    }

    public set gemeinsamerHaushaltMitPartner(value: boolean) {
        this._gemeinsamerHaushaltMitPartner = value;
    }
    public get gemeinsamerHaushaltMitObhutsberechtigterPerson(): boolean {
        return this._gemeinsamerHaushaltMitObhutsberechtigterPerson;
    }

    public set gemeinsamerHaushaltMitObhutsberechtigterPerson(value: boolean) {
        this._gemeinsamerHaushaltMitObhutsberechtigterPerson = value;
    }

    public get auszahlungAusserhalbVonKibon(): boolean {
        return this._auszahlungAusserhalbVonKibon;
    }

    public set auszahlungAusserhalbVonKibon(value: boolean) {
        this._auszahlungAusserhalbVonKibon = value;
    }

    private hasSecondGesuchstellerFKJV(): boolean {
        if (this.geteilteObhut) {
            return (
                this.gesuchstellerKardinalitaet ===
                TSGesuchstellerKardinalitaet.ZU_ZWEIT
            );
        }

        return (
            this.unterhaltsvereinbarung ===
            TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG
        );
    }

    private hasSecondGesuchstellerAppenzell(): boolean {
        return (
            this.geteilteObhut &&
            this.gemeinsamerHaushaltMitObhutsberechtigterPerson
        );
    }

    public konkubinatGetXYearsInPeriod(gueltigkeit: TSDateRange): boolean {
        if (EbeguUtil.isNullOrUndefined(this.startKonkubinat)) {
            return false;
        }
        return (
            this.getStartKonkubinatPlusMinDauer().isAfter(
                gueltigkeit.gueltigAb
            ) &&
            this.getStartKonkubinatPlusMinDauer().isBefore(
                gueltigkeit.gueltigBis
            )
        );
    }

    public deepCopyTo(target: TSFamiliensituation): TSFamiliensituation {
        super.deepCopyTo(target);
        target.familienstatus = this.familienstatus;
        target.gemeinsameSteuererklaerung = this.gemeinsameSteuererklaerung;
        target.aenderungPer = this.aenderungPer;
        target.startKonkubinat = this.startKonkubinat;
        target.sozialhilfeBezueger = this.sozialhilfeBezueger;
        target.zustaendigeAmtsstelle = this.zustaendigeAmtsstelle;
        target.nameBetreuer = this.nameBetreuer;
        target.verguenstigungGewuenscht = this.verguenstigungGewuenscht;
        target.keineMahlzeitenverguenstigungBeantragt =
            this.keineMahlzeitenverguenstigungBeantragt;
        target.keineMahlzeitenverguenstigungBeantragtEditable =
            this.keineMahlzeitenverguenstigungBeantragtEditable;
        target.iban = this.iban;
        target.kontoinhaber = this.kontoinhaber;
        target.abweichendeZahlungsadresse = this.abweichendeZahlungsadresse;
        if (EbeguUtil.isNotNullOrUndefined(this.zahlungsadresse)) {
            target.zahlungsadresse = new TSAdresse();
            target.zahlungsadresse.deepCopyTo(this.zahlungsadresse);
        }

        target.infomaKreditorennummer = this.infomaKreditorennummer;
        target.infomaBankcode = this.infomaBankcode;
        target.gesuchstellerKardinalitaet = this.gesuchstellerKardinalitaet;
        target.fkjvFamSit = this.fkjvFamSit;
        target.minDauerKonkubinat = this.minDauerKonkubinat;
        target.unterhaltsvereinbarung = this.unterhaltsvereinbarung;
        target.unterhaltsvereinbarungBemerkung =
            this.unterhaltsvereinbarungBemerkung;
        target.geteilteObhut = this.geteilteObhut;
        target.partnerIdentischMitVorgesuch = this.partnerIdentischMitVorgesuch;
        target.gemeinsamerHaushaltMitObhutsberechtigterPerson =
            this.gemeinsamerHaushaltMitObhutsberechtigterPerson;
        target.gemeinsamerHaushaltMitPartner =
            this.gemeinsamerHaushaltMitPartner;
        target.auszahlungAusserhalbVonKibon = this.auszahlungAusserhalbVonKibon;
        return target;
    }
}
