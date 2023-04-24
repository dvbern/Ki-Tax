/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {EbeguUtil} from '../utils/EbeguUtil';
import {TSFinanzielleSituationTyp} from './enums/TSFinanzielleSituationTyp';
import {TSEinkommensverschlechterung} from './TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from './TSEinkommensverschlechterungContainer';
import {TSEinkommensverschlechterungInfoContainer} from './TSEinkommensverschlechterungInfoContainer';
import {TSFamiliensituation} from './TSFamiliensituation';
import {TSFamiliensituationContainer} from './TSFamiliensituationContainer';
import {TSFinanzielleSituation} from './TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from './TSFinanzielleSituationContainer';
import {TSGesuch} from './TSGesuch';
import {TSZahlungsinformationen} from './TSZahlungsinformationen';

export class TSFinanzModel {
    private _finanzielleSituationContainerGS1: TSFinanzielleSituationContainer;
    private _finanzielleSituationContainerGS2: TSFinanzielleSituationContainer;
    private _finanzielleSituationVorMutationGS1: TSFinanzielleSituation;
    private _finanzielleSituationVorMutationGS2: TSFinanzielleSituation;
    private _einkommensverschlechterungContainerGS1: TSEinkommensverschlechterungContainer;
    private _einkommensverschlechterungContainerGS2: TSEinkommensverschlechterungContainer;
    private _einkommensverschlechterungInfoContainer: TSEinkommensverschlechterungInfoContainer;

    private _zahlungsinformationenGS: TSZahlungsinformationen;
    private _zahlungsinformationen: TSZahlungsinformationen;

    private _finanzielleSituationTyp: TSFinanzielleSituationTyp;

    private _familienSituation: TSFamiliensituation;

    private readonly basisjahr: number;
    private readonly basisjahrPlus: number;
    private readonly gesuchsteller2Required: boolean;
    private readonly gesuchstellerNumber: number;

    public constructor(
        basisjahr: number,
        gesuchsteller2Required: boolean,
        gesuchstellerNumber: number,
        basisjahrPlus?: number
    ) {
        this.basisjahr = basisjahr;
        this.basisjahrPlus = basisjahrPlus;
        this.gesuchsteller2Required = gesuchsteller2Required;
        this.gesuchstellerNumber = gesuchstellerNumber;
    }

    public get finanzielleSituationContainerGS1(): TSFinanzielleSituationContainer {
        return this._finanzielleSituationContainerGS1;
    }

    public set finanzielleSituationContainerGS1(value: TSFinanzielleSituationContainer) {
        this._finanzielleSituationContainerGS1 = value;
    }

    public get finanzielleSituationContainerGS2(): TSFinanzielleSituationContainer {
        return this._finanzielleSituationContainerGS2;
    }

    public set finanzielleSituationContainerGS2(value: TSFinanzielleSituationContainer) {
        this._finanzielleSituationContainerGS2 = value;
    }

    public get zahlungsinformationen(): TSZahlungsinformationen {
        return this._zahlungsinformationen;
    }

    public set zahlungsinformationen(value: TSZahlungsinformationen) {
        this._zahlungsinformationen = value;
    }

    public get finanzielleSituationTyp(): TSFinanzielleSituationTyp {
        return this._finanzielleSituationTyp;
    }

    public set finanzielleSituationTyp(value: TSFinanzielleSituationTyp) {
        this._finanzielleSituationTyp = value;
    }

    public get familienSituation(): TSFamiliensituation {
        return this._familienSituation;
    }

    public set familienSituation(value: TSFamiliensituation) {
        this._familienSituation = value;
    }

    public copyFinSitDataFromGesuch(gesuch: TSGesuch): void {
        if (!gesuch) {
            return;
        }
        this.familienSituation = angular.copy(gesuch.extractFamiliensituation());
        this.familienSituation.gemeinsameSteuererklaerung =
            this.getCopiedValueOrFalse(gesuch.extractFamiliensituation().gemeinsameSteuererklaerung);
        this.familienSituation.sozialhilfeBezueger =
            this.getCopiedValueOrUndefined(gesuch.extractFamiliensituation().sozialhilfeBezueger);
        this.familienSituation.zustaendigeAmtsstelle = gesuch.extractFamiliensituation().zustaendigeAmtsstelle;

        this.familienSituation.nameBetreuer = gesuch.extractFamiliensituation().nameBetreuer;
        this.familienSituation.verguenstigungGewuenscht =
            this.getCopiedValueOrUndefined(gesuch.extractFamiliensituation().verguenstigungGewuenscht);
        this.finanzielleSituationContainerGS1 = angular.copy(gesuch.gesuchsteller1.finanzielleSituationContainer);
        if (gesuch.gesuchsteller2) {
            this.finanzielleSituationContainerGS2 = angular.copy(gesuch.gesuchsteller2.finanzielleSituationContainer);
        }

        this.zahlungsinformationen = new TSZahlungsinformationen();
        this.zahlungsinformationen.kontoinhaber = gesuch.extractFamiliensituation().kontoinhaber;
        this.zahlungsinformationen.iban = gesuch.extractFamiliensituation().iban;
        this.zahlungsinformationen.abweichendeZahlungsadresse =
            gesuch.extractFamiliensituation().abweichendeZahlungsadresse;
        this.zahlungsinformationen.zahlungsadresse = gesuch.extractFamiliensituation().zahlungsadresse;
        this.zahlungsinformationen.keineMahlzeitenverguenstigungBeantragt =
            gesuch.extractFamiliensituation().keineMahlzeitenverguenstigungBeantragt;
        this.zahlungsinformationen.infomaBankcode =  gesuch.extractFamiliensituation().infomaBankcode;
        this.zahlungsinformationen.infomaKreditorennummer =  gesuch.extractFamiliensituation().infomaKreditorennummer;

        if (gesuch.extractFamiliensituationGS()) {
            this.zahlungsinformationenGS = new TSZahlungsinformationen();
            this.zahlungsinformationenGS.kontoinhaber = gesuch.extractFamiliensituationGS().kontoinhaber;
            this.zahlungsinformationenGS.iban = gesuch.extractFamiliensituationGS().iban;
            this.zahlungsinformationenGS.abweichendeZahlungsadresse =
                gesuch.extractFamiliensituationGS().abweichendeZahlungsadresse;
            this.zahlungsinformationenGS.zahlungsadresse = gesuch.extractFamiliensituationGS().zahlungsadresse;
            this.zahlungsinformationenGS.keineMahlzeitenverguenstigungBeantragt =
                gesuch.extractFamiliensituationGS().keineMahlzeitenverguenstigungBeantragt;
            this.zahlungsinformationenGS.infomaBankcode = gesuch.extractFamiliensituationGS().infomaBankcode;
            this.zahlungsinformationenGS.infomaKreditorennummer =
                gesuch.extractFamiliensituationGS().infomaKreditorennummer;
        }

        this.finanzielleSituationTyp = gesuch.finSitTyp;

        this.initFinSit();
    }

    private getCopiedValueOrFalse(value: boolean): boolean {
        return value ? angular.copy(value) : false;
    }

    private getCopiedValueOrUndefined(value: boolean): boolean {
        return value || !value ? angular.copy(value) : undefined;
    }

    public copyEkvDataFromGesuch(gesuch: TSGesuch): void {
        this.einkommensverschlechterungInfoContainer = gesuch.einkommensverschlechterungInfoContainer ?
            angular.copy(gesuch.einkommensverschlechterungInfoContainer) :
            new TSEinkommensverschlechterungInfoContainer();
        if (gesuch.gesuchsteller1) {
            this.einkommensverschlechterungContainerGS1 =
                angular.copy(gesuch.gesuchsteller1.einkommensverschlechterungContainer);
        }
        if (gesuch.gesuchsteller2) {
            this.einkommensverschlechterungContainerGS2 =
                angular.copy(gesuch.gesuchsteller2.einkommensverschlechterungContainer);
        }
    }

    public initFinSit(): void {
        if (!this.finanzielleSituationContainerGS1) {
            this.finanzielleSituationContainerGS1 = new TSFinanzielleSituationContainer();
            this.finanzielleSituationContainerGS1.jahr = this.basisjahr;
            this.finanzielleSituationContainerGS1.finanzielleSituationJA = new TSFinanzielleSituation();
        }

        if (!this.gesuchsteller2Required || this.finanzielleSituationContainerGS2) {
            return;
        }

        this.finanzielleSituationContainerGS2 = new TSFinanzielleSituationContainer();
        this.finanzielleSituationContainerGS2.jahr = this.basisjahr;
        this.finanzielleSituationContainerGS2.finanzielleSituationJA = new TSFinanzielleSituation();
    }

    public copyFinSitDataToGesuch(gesuch: TSGesuch): TSGesuch {
        if (EbeguUtil.isNullOrUndefined(gesuch.familiensituationContainer)) {
            gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        }
        let familiensituation = gesuch.extractFamiliensituation();
        if (EbeguUtil.isNullOrUndefined(familiensituation)) {
            familiensituation = new TSFamiliensituation();
            gesuch.familiensituationContainer.familiensituationJA = familiensituation;
        }
        gesuch.familiensituationContainer.familiensituationJA = this.familienSituation;
        gesuch.gesuchsteller1.finanzielleSituationContainer = this.finanzielleSituationContainerGS1;
        if (gesuch.gesuchsteller2) {
            gesuch.gesuchsteller2.finanzielleSituationContainer = this.finanzielleSituationContainerGS2;
        } else if (this.finanzielleSituationContainerGS2) {
            // wenn wir keinen gs2 haben sollten wir auch gar keinen solchen container haben
            console.log('illegal state: finanzielleSituationContainerGS2 exists but no gs2 is available');
        }
        this.resetSteuerveranlagungErhaltenAndSteuererklaerungAusgefuellt(gesuch);

        familiensituation.kontoinhaber = this.zahlungsinformationen.kontoinhaber;
        familiensituation.iban = this.zahlungsinformationen.iban?.toLocaleUpperCase();
        familiensituation.abweichendeZahlungsadresse =
            this.zahlungsinformationen.abweichendeZahlungsadresse;
        familiensituation.zahlungsadresse = this.zahlungsinformationen.zahlungsadresse;
        familiensituation.keineMahlzeitenverguenstigungBeantragt =
            this.zahlungsinformationen.keineMahlzeitenverguenstigungBeantragt;
        familiensituation.infomaBankcode = this.zahlungsinformationen.infomaBankcode;
        familiensituation.infomaKreditorennummer = this.zahlungsinformationen.infomaKreditorennummer;

        return gesuch;
    }

    public initFinSitVorMutation(gesuchVorMutation: TSGesuch): void {
        this._finanzielleSituationVorMutationGS1 =
            gesuchVorMutation.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA;
        if (gesuchVorMutation.gesuchsteller2) {
            this._finanzielleSituationVorMutationGS2 =
                gesuchVorMutation.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA;
        }
    }

    /**
     * if gemeinsameSteuererklaerung has been set to true and steuerveranlagungErhalten ist set to true for the GS1
     * as well, then we need to set steuerveranlagungErhalten to true for the GS2 too, if it exists.
     * the same for steuererklaerungAusgefuellt
     */
    private resetSteuerveranlagungErhaltenAndSteuererklaerungAusgefuellt(gesuch: TSGesuch): void {
        if (!(gesuch.extractFamiliensituation().gemeinsameSteuererklaerung && gesuch.gesuchsteller1 && gesuch.gesuchsteller2)) {
            return;
        }
        const finSitGS1 = gesuch.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA;
        if (EbeguUtil.isNullOrUndefined(gesuch.gesuchsteller2.finanzielleSituationContainer)) {
            gesuch.gesuchsteller2.finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        }
        if (EbeguUtil.isNullOrUndefined(gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA)) {
            gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        }
        if (this.finanzielleSituationTyp === TSFinanzielleSituationTyp.SOLOTHURN) {
            return;
        }
        gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA.steuerveranlagungErhalten
            = finSitGS1.steuerveranlagungErhalten;
        gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA.steuererklaerungAusgefuellt
            = finSitGS1.steuererklaerungAusgefuellt;
    }

    public copyEkvSitDataToGesuch(gesuch: TSGesuch): TSGesuch {
        gesuch.einkommensverschlechterungInfoContainer = this.einkommensverschlechterungInfoContainer;
        gesuch.gesuchsteller1.einkommensverschlechterungContainer = this.einkommensverschlechterungContainerGS1;
        if (gesuch.gesuchsteller2) {
            gesuch.gesuchsteller2.einkommensverschlechterungContainer = this.einkommensverschlechterungContainerGS2;
        } else if (this.einkommensverschlechterungContainerGS2) {
            // wenn wir keinen gs2 haben sollten wir auch gar keinen solchen container haben
            console.log('illegal state: einkommensverschlechterungContainerGS2 exists but no gs2 is available');
        }
        return gesuch;
    }

    public getFiSiConToWorkWith(): TSFinanzielleSituationContainer {
        return this.gesuchstellerNumber === 2 ?
            this.finanzielleSituationContainerGS2 :
            this.finanzielleSituationContainerGS1;
    }

    public getEkvContToWorkWith(): TSEinkommensverschlechterungContainer {
        return this.gesuchstellerNumber === 2 ?
            this.einkommensverschlechterungContainerGS2 :
            this.einkommensverschlechterungContainerGS1;
    }

    public getEkvToWorkWith(): TSEinkommensverschlechterung {
        return this.gesuchstellerNumber === 2 ?
            this.getEkvOfBsj_JA(this.einkommensverschlechterungContainerGS2) :
            this.getEkvOfBsj_JA(this.einkommensverschlechterungContainerGS1);
    }

    // eslint-disable-next-line
    private getEkvOfBsj_JA(einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer,
    ): TSEinkommensverschlechterung {
        return this.basisjahrPlus === 2 ?
            einkommensverschlechterungContainer.ekvJABasisJahrPlus2 :
            einkommensverschlechterungContainer.ekvJABasisJahrPlus1;
    }

    // eslint-disable-next-line
    public getEkvToWorkWith_GS(): TSEinkommensverschlechterung {
        return this.gesuchstellerNumber === 2 ?
            this.getEkvOfBsj_GS(this.einkommensverschlechterungContainerGS2) :
            this.getEkvOfBsj_GS(this.einkommensverschlechterungContainerGS1);
    }

    // eslint-disable-next-line
    private getEkvOfBsj_GS(einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer,
    ): TSEinkommensverschlechterung {
        return this.basisjahrPlus === 2 ?
            einkommensverschlechterungContainer.ekvGSBasisJahrPlus2 :
            einkommensverschlechterungContainer.ekvGSBasisJahrPlus1;
    }

    public getFinSitVorMutationToWorkWith(): TSFinanzielleSituation {
        return this.gesuchstellerNumber === 2 ?
            this._finanzielleSituationVorMutationGS2 :
            this._finanzielleSituationVorMutationGS1;
    }

    public getGesuchstellerNumber(): number {
        return this.gesuchstellerNumber;
    }

    public isGesuchsteller2Required(): boolean {
        return this.gesuchsteller2Required;
    }

    public get einkommensverschlechterungContainerGS1(): TSEinkommensverschlechterungContainer {
        return this._einkommensverschlechterungContainerGS1;
    }

    public set einkommensverschlechterungContainerGS1(value: TSEinkommensverschlechterungContainer) {
        this._einkommensverschlechterungContainerGS1 = value;
    }

    public get einkommensverschlechterungContainerGS2(): TSEinkommensverschlechterungContainer {
        return this._einkommensverschlechterungContainerGS2;
    }

    public set einkommensverschlechterungContainerGS2(value: TSEinkommensverschlechterungContainer) {
        this._einkommensverschlechterungContainerGS2 = value;
    }

    public get einkommensverschlechterungInfoContainer(): TSEinkommensverschlechterungInfoContainer {
        return this._einkommensverschlechterungInfoContainer;
    }

    public set einkommensverschlechterungInfoContainer(value: TSEinkommensverschlechterungInfoContainer) {
        this._einkommensverschlechterungInfoContainer = value;
    }

    public get zahlungsinformationenGS(): TSZahlungsinformationen {
        return this._zahlungsinformationenGS;
    }

    public set zahlungsinformationenGS(value: TSZahlungsinformationen) {
        this._zahlungsinformationenGS = value;
    }

    // eslint-disable-next-line
    public initEinkommensverschlechterungContainer(basisjahrPlus: number, gesuchstellerNumber: number): void {
        if ((basisjahrPlus !== 2 && basisjahrPlus !== 1)
            || (gesuchstellerNumber !== 2 && gesuchstellerNumber !== 1)) {
            return;
        }
        if (gesuchstellerNumber === 1) {
            if (!this.einkommensverschlechterungContainerGS1) {
                this.einkommensverschlechterungContainerGS1 = new TSEinkommensverschlechterungContainer();
            }

            if (basisjahrPlus === 1 && !this.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus1) {
                this.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus1 =
                    new TSEinkommensverschlechterung();
            }
            if (basisjahrPlus === 2 && !this.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus2) {
                this.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus2 =
                    new TSEinkommensverschlechterung();
            }
        } else {

            if (!this.einkommensverschlechterungContainerGS2) {
                this.einkommensverschlechterungContainerGS2 = new TSEinkommensverschlechterungContainer();
            }
            if (basisjahrPlus === 1 && !this.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus1) {
                this.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus1 =
                    new TSEinkommensverschlechterung();
            }
            if (basisjahrPlus === 2 && !this.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus2) {
                this.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus2 =
                    new TSEinkommensverschlechterung();
            }
        }
    }

    public getBasisJahrPlus(): number {
        return this.basisjahrPlus;
    }

    public getBasisjahr(): number {
        return this.basisjahr;
    }

    /**
     * Indicates whether FinSit must be filled out or not. It supposes that it is enabled.
     */
    public isFinanzielleSituationRequired(): boolean {
        return EbeguUtil.isFinanzielleSituationRequired(this.familienSituation.sozialhilfeBezueger, this.familienSituation.verguenstigungGewuenscht);
    }

    public get finanzielleSituationVorMutationGS1(): TSFinanzielleSituation {
        return this._finanzielleSituationVorMutationGS1;
    }

    public get finanzielleSituationVorMutationGS2(): TSFinanzielleSituation {
        return this._finanzielleSituationVorMutationGS2;
    }
}
