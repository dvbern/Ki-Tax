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

import {MatRadioChange} from '@angular/material/radio';
import {TranslateService} from '@ngx-translate/core';
import {IPromise} from 'angular';
import {CONSTANTS} from '../../../../app/core/constants/CONSTANTS';
import {ApplicationPropertyRS} from '../../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzielleSituationSelbstdeklaration} from '../../../../models/TSFinanzielleSituationSelbstdeklaration';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../models/TSGesuch';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';
import {FinanzielleSituationLuzernService} from './finanzielle-situation-luzern.service';

export abstract class AbstractFinSitLuzernView extends AbstractGesuchViewX<TSFinanzModel> {

    private isInfomazahlungenAktiv: boolean = false;

    protected constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected gesuchstellerNumber: number,
        protected finSitLuService: FinanzielleSituationLuzernService,
        protected authServiceRS: AuthServiceRS,
        protected readonly translate: TranslateService,
        protected readonly applicationPropertyRS: ApplicationPropertyRS
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_LUZERN);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            gesuchstellerNumber);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.setupForm();
        this.gesuchModelManager.setGesuchstellerNumber(gesuchstellerNumber);
        this.applicationPropertyRS.getPublicPropertiesCached().then(res => {
            this.isInfomazahlungenAktiv = res.infomaZahlungen;
        });
    }

    public showSelbstdeklaration(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.quellenbesteuert)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.veranlagtVorjahr);
    }

    public showVeranlagung(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.veranlagt)
            || EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.veranlagtVorjahr);
    }

    public showResultat(): boolean {
        return !this.gesuchModelManager.isGesuchsteller2Required();
    }

    public gemeinsameStekVisible(): boolean {
        return this.isGemeinsam() && EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.quellenbesteuert);
    }

    public alleinigeStekVisible(): boolean {
        return !this.isGemeinsam() && EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.quellenbesteuert);
    }

    public veranlagtVisible(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)
            || EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr);
    }

    public veranlagtVorjahrVisible(): boolean {
        return EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.veranlagt);
    }

    public sozialhilfeBezuegerChange(): void {
        this.getModel().finanzielleSituationJA.quellenbesteuert = undefined;
        this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.alleinigeStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.veranlagt = undefined;
        this.getModel().finanzielleSituationJA.veranlagtVorjahr = undefined;
        this.initOrResetDekarationen();
    }

    public quellenBesteuertChange(newQuellenBesteuert: MatRadioChange): void {
        if (newQuellenBesteuert.value === true) {
            this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr = undefined;
            this.getModel().finanzielleSituationJA.alleinigeStekVorjahr = undefined;
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
            this.getModel().finanzielleSituationJA.veranlagtVorjahr = undefined;
        }
        this.initOrResetDekarationen();
    }

    public gemeinsameStekChange(newGemeinsameStek: MatRadioChange): void {
        if (newGemeinsameStek.value === false &&
            EbeguUtil.isNullOrFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
            this.getModel().finanzielleSituationJA.veranlagtVorjahr = undefined;
        }
        this.initOrResetDekarationen();
    }

    public alleinigeStekVorjahrChange(newAlleinigeStekVorjahr: MatRadioChange): void {
        if (newAlleinigeStekVorjahr.value === false &&
            EbeguUtil.isNullOrFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
            this.getModel().finanzielleSituationJA.veranlagtVorjahr = undefined;
        }
        this.initOrResetDekarationen();
    }

    public veranlagtChange(): void {
        this.getModel().finanzielleSituationJA.veranlagtVorjahr = undefined;
        this.initOrResetDekarationen();
    }

    public veranlagtVorjahrChange(): void {
        this.initOrResetDekarationen();
    }

    public getYearForVeranlagung(): number | string {
        if (EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.veranlagt)) {
            return this.getBasisjahr();
        }
        if (EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.veranlagtVorjahr)) {
            return this.getBasisjahrMinus1();
        }
        return '';
    }

    public getYearForSelbstdeklaration(): string {
        if (this.getModel().finanzielleSituationJA.quellenbesteuert) {
            return this.getBasisjahr().toString();
        }
        if (EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)) {
            return this.getBasisjahrPlus1().toString();
        }
        if (EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.veranlagtVorjahr)) {
            return this.getBasisjahr().toString();
        }
        return '';
    }

    public getYearForDeklaration(): number | string {
        if (this.showSelbstdeklaration()) {
            return this.getYearForSelbstdeklaration();
        }
        if (this.showVeranlagung()) {
            return this.getYearForVeranlagung();
        }
        return '';
    }

    public abstract isGemeinsam(): boolean;

    public abstract getAntragstellerNummer(): number;

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public isFinSitReadonly(): boolean {
        return this.isGesuchReadonly()
            || (this.getGesuch().isMutation() && this.authServiceRS.isRole(TSRole.GESUCHSTELLER));
    }

    public showZahlungsinformationen(): boolean {
        return this.getAntragstellerNummer() === 1;
    }

    public showInfomaFields(): boolean {
        return this.gesuchModelManager.getGemeinde().infomaZahlungen && this.isInfomazahlungenAktiv === true;
    }

    public hasPrevious(): boolean {
        return true;
    }

    public hasNext(): boolean {
        return true;
    }

    public abstract getSubStepIndex(): number;

    public abstract getSubStepName(): string;

    public abstract prepareSave(onResult: (arg: any) => any): IPromise<TSFinanzielleSituationContainer>;

    public getAntragstellerNameForCurrentStep(): string {
        if (this.isGemeinsam()) {
            return '';
        }
        if (this.getAntragstellerNummer() === 1) {
            return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
        }
        if (this.getAntragstellerNummer() === 2) {
            try {
                return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
            } catch (error) {
                // Gesuchsteller has not yet filled in Form for Antragsteller 2
                return '';
            }
        }
        return '';
    }

    public isGS1(): boolean {
        return this.getAntragstellerNummer() === 1;
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public getAntragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    public isRoleGemeindeOrSuperAdmin(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole());
    }

    public showFinSitDatumGueltigAbText(): boolean {
        return EbeguUtil.isNullOrUndefined(this.getGesuch().finSitAenderungGueltigAbDatum);
    }

    public showFinSitDatumGueltigAb(): boolean {
        if (!isAtLeastFreigegeben(this.getGesuch().status)) {
            return false;
        }

        return this.isMutation() && this.isGS1();
    }

    public getFinSitDatumGueltigAbText(): string {
        const eingangsdatum = EbeguUtil.isNotNullOrUndefined(this.getGesuch().regelnGueltigAb) ?
            this.getGesuch().regelnGueltigAb :
            this.getGesuch().eingangsdatum;

        const formatedDate = DateUtil.momentToLocalDateFormat(eingangsdatum, CONSTANTS.DATE_FORMAT);
        return this.translate.instant('FINANZIELLE_SITUATION_GUELTIG_AB_NULL_INFO', {datum: formatedDate});
    }

    public isSozialhilfeBezueger(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.model.familienSituation.sozialhilfeBezueger);
    }

    protected abstract save(onResult: (arg: any) => any): IPromise<TSFinanzielleSituationContainer>;

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    protected updateWizardStepStatus(): IPromise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_LUZERN,
                TSWizardStepStatus.OK);
    }

    private setupForm(): void {
        if (!this.getModel().finanzielleSituationJA.isNew()) {
            return;
        }
        this.getModel().finanzielleSituationJA.quellenbesteuert = undefined;
        this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.alleinigeStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.veranlagt = undefined;
    }

    private initOrResetDekarationen(): void {
        if (this.showVeranlagung()) {
            this.getModel().finanzielleSituationJA.selbstdeklaration = undefined;
        }
        if (this.showSelbstdeklaration()) {
            this.resetVeranlagungValues();
            this.getModel().finanzielleSituationJA.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
    }

    private resetVeranlagungValues(): void {
        this.getModel().finanzielleSituationJA.steuerbaresEinkommen = undefined;
        this.getModel().finanzielleSituationJA.steuerbaresVermoegen = undefined;
        this.getModel().finanzielleSituationJA.abzuegeLiegenschaft = undefined;
        this.getModel().finanzielleSituationJA.geschaeftsverlust = undefined;
        this.getModel().finanzielleSituationJA.einkaeufeVorsorge = undefined;
        this.finSitLuService.calculateMassgebendesEinkommen(this.model);
    }
}
