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

import {Component} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {getTSFamilienstatusValues, TSFamilienstatus} from '../../../models/enums/TSFamilienstatus';
import {
    getTSGesuchstellerKardinalitaetValues,
    TSGesuchstellerKardinalitaet
} from '../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSRole} from '../../../models/enums/TSRole';
import {
    getTSUnterhaltsvereinbarungAnswerValues,
    TSUnterhaltsvereinbarungAnswer
} from '../../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSFamiliensituation} from '../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../models/TSFamiliensituationContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {FamiliensituationRS} from '../../service/familiensituationRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';


const LOG = LogFactory.createLog('FamiliensitutionViewComponent');


@Component({
    selector: 'dv-familiensituation-view-x',
    templateUrl: './familiensituation-view-x.component.html',
    styleUrls: ['./familiensituation-view-x.component.less']
})
export class FamiliensituationViewXComponent extends AbstractGesuchViewX<TSFamiliensituationContainer> {

    private familienstatusValues: Array<TSFamilienstatus>;
    public allowedRoles: ReadonlyArray<TSRole>;
    public initialFamiliensituation: TSFamiliensituation;
    public savedClicked: boolean = false;
    public situationFKJV = false;
    public gesuchstellerKardinalitaetValues: Array<TSGesuchstellerKardinalitaet>;
    public unterhaltsvereinbarungAnswerValues: Array<TSUnterhaltsvereinbarungAnswer>;

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        private readonly berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        private readonly dialog: MatDialog,
        private readonly $translate: TranslateService,
        private readonly familiensituationRS: FamiliensituationRS,
        private readonly einstellungRS: EinstellungRS,
    ) {

        super(gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.FAMILIENSITUATION);
        this.gesuchModelManager.initFamiliensituation();
        //TODO: ????
        this.model = Object.assign({}, this.getGesuch().familiensituationContainer);
        this.initialFamiliensituation = Object.assign({}, this.gesuchModelManager.getFamiliensituation());
        this.gesuchstellerKardinalitaetValues = getTSGesuchstellerKardinalitaetValues();
        this.unterhaltsvereinbarungAnswerValues = getTSUnterhaltsvereinbarungAnswerValues();
        this.initViewModel();

    }

    public $onInit(): void {
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id
        ).subscribe((response: TSEinstellung[]) => {
            response.filter(r => r.key === TSEinstellungKey.FKJV_FAMILIENSITUATION_NEU)
                .forEach(value => {
                    this.situationFKJV = value.getValueAsBoolean();
                    this.familienstatusValues = getTSFamilienstatusValues();
                    this.getFamiliensituation().fkjvFamSit = this.situationFKJV;
                });
            response.filter(r => r.key === TSEinstellungKey.MINIMALDAUER_KONKUBINAT)
                .forEach(value => {
                    this.getFamiliensituation().minDauerKonkubinat = Number(value.value);
                });
        }, error => LOG.error(error));
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FAMILIENSITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.allowedRoles = TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    public confirmAndSave(): Promise<TSFamiliensituationContainer> {
        this.savedClicked = true;
        if (this.isGesuchValid() && !this.hasEmptyAenderungPer() && !this.hasError()) {
            if (!this.form.dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                // Update wizardStepStatus also if the form is empty and not dirty
                this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
                return Promise.resolve(this.getGesuch().familiensituationContainer);
            }

            if (this.isConfirmationRequired()) {
                const descriptionText: any = this.$translate.instant('FAMILIENSITUATION_WARNING_BESCHREIBUNG', {
                    gsfullname: this.getGesuch().gesuchsteller2
                        ? this.getGesuch().gesuchsteller2.extractFullName() : ''
                });
                return this.dialog.open(DvNgRemoveDialogComponent, {
                    data: {
                        title: 'FAMILIENSITUATION_WARNING',
                        deleteText: descriptionText,
                    },
                }).afterClosed().toPromise().then(() =>    // User confirmed changes
                    this.save(),
                );
            }

            return this.save();

        }
        return undefined;
    }

    private save(): Promise<TSFamiliensituationContainer> {
        this.errorService.clearAll();
        return this.familiensituationRS.saveFamiliensituation(
            this.model,
            this.getGesuch().id
        ).then((familienContainerResponse: any) => {
            this.model = familienContainerResponse;
            this.getGesuch().familiensituationContainer = familienContainerResponse;
            // Gesuchsteller may changed...
            return this.gesuchModelManager.reloadGesuch().then(() => this.model);
        }) as Promise<TSFamiliensituationContainer>;
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.model.familiensituationJA;
    }

    public getFamiliensituationGS(): TSFamiliensituation {
        return this.model.familiensituationGS;
    }

    public isStartKonkubinatVisible(): boolean {
        return this.getFamiliensituation()?.familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND;
    }

    /**
     * Removes startKonkubinat when the familienstatus doesn't require it.
     * If we are in a mutation and we change to KONKUBINAT_KEIN_KIND we need to copy aenderungPer into startKonkubinat
     */
    public familienstatusChanged(): void {
        if (this.getFamiliensituation().familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            this.getFamiliensituation().startKonkubinat = undefined;

        } else if (this.isMutation() && this.getFamiliensituation().aenderungPer && this.isStartKonkubinatVisible()) {
            this.getFamiliensituation().startKonkubinat = this.getFamiliensituation().aenderungPer;
        }
        // eslint-disable-next-line
        if (!this.isFamilienstatusAlleinerziehendOrShortKonkubinat()) {
            this.getFamiliensituation().gesuchstellerKardinalitaet = undefined;
            this.getFamiliensituation().unterhaltsvereinbarung = undefined;
            this.getFamiliensituation().unterhaltsvereinbarungBemerkung = undefined;
            this.getFamiliensituation().geteilteObhut = undefined;
        }
    }

    /**
     * This should happen only in a Mutation, where we can change the field aenderungPer but not startKonkubinat.
     * Any change in aenderungPer will copy the value into startKonkubinat if the last is visible
     */
    public aenderungPerChanged(): void {
        if (this.isStartKonkubinatVisible()) {
            this.getFamiliensituation().startKonkubinat = this.getFamiliensituation().aenderungPer;
        }
    }

    public getFamiliensituationErstgesuch(): TSFamiliensituation {
        return this.model.familiensituationErstgesuch;
    }

    /**
     * Confirmation is required when the GS2 already exists and the familiensituation changes from 2GS to 1GS. Or when
     * in a Mutation the GS2 is new and will be removed
     */
    private isConfirmationRequired(): boolean {
        return (!this.isKorrekturModusJugendamt()
            || (this.isKorrekturModusJugendamt() && this.getGesuch().gesuchsteller2))
            && ((!this.isMutation() && this.checkChanged2To1GS())
                || (this.isMutation() && (this.isChanged1To2Reverted() || this.checkChanged2To1GSMutation())));
    }

    private checkChanged2To1GS(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.getGesuch().gesuchsteller2
            && this.getGesuch().gesuchsteller2.id
            && this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && this.isScheidung();
    }

    private isChanged1To2Reverted(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.getGesuch().gesuchsteller2
            && this.getGesuch().gesuchsteller2.id
            && this.isScheidung()
            && this.model.familiensituationErstgesuch
            && !this.model.familiensituationErstgesuch.hasSecondGesuchsteller(bis);
    }

    private checkChanged2To1GSMutation(): boolean {
        const ab = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigAb;
        return (this.getFamiliensituation()?.aenderungPer.isBefore(ab)
            && this.getGesuch().getRegelStartDatum().isBefore(ab));
    }

    private isScheidung(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && !this.getFamiliensituation()?.hasSecondGesuchsteller(bis);
    }

    public isMutationAndDateSet(): boolean {
        if (!this.isMutation()) {
            return true;
        }

        return EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation()) && EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation().aenderungPer);
    }

    public isFamiliensituationEnabled(): boolean {
        return this.isMutationAndDateSet() && !this.isGesuchReadonly();
    }

    public isStartKonkubinatDisabled(): boolean {
        return this.isMutation() || (this.isGesuchReadonly() && !this.isKorrekturModusJugendamt());
    }

    public hasEmptyAenderungPer(): boolean {
        return this.isMutation()
            && !this.getFamiliensituation()?.aenderungPer
            && !this.getFamiliensituationErstgesuch()?.isSameFamiliensituation(this.getFamiliensituation());
    }

    public resetFamsit(): void {
        this.getFamiliensituation().revertFamiliensituation(this.getFamiliensituationErstgesuch());
    }

    public hasError(): boolean {
        return this.isMutation()
            && this.getFamiliensituation()?.aenderungPer
            && this.getFamiliensituationErstgesuch()?.isSameFamiliensituation(this.getFamiliensituation());
    }

    public showError(): boolean {
        return this.hasError() && this.savedClicked;
    }

    public onDatumBlur(): void {
        if (this.hasEmptyAenderungPer()) {
            this.resetFamsit();
        }
    }

    public getFamiliensituationValues(): Array<TSFamilienstatus> {
        return this.familienstatusValues;
    }

    public getUnterhaltvereinbarungValues(): Array<TSUnterhaltsvereinbarungAnswer> {
        return this.unterhaltsvereinbarungAnswerValues;
    }

    public showGesuchstellerKardinalitaet(): boolean {
        if (this.getFamiliensituation() && this.situationFKJV && this.isFamilienstatusAlleinerziehendOrShortKonkubinat()) {
            return this.getFamiliensituation().geteilteObhut;
        }
        return false;
    }

    public showFrageUnterhaltsvereinbarung(): boolean {
        if (this.getFamiliensituation() && this.situationFKJV && this.isFamilienstatusAlleinerziehendOrShortKonkubinat()) {
            return EbeguUtil.isNotNullAndFalse(this.getFamiliensituation().geteilteObhut);
        }
        return false;
    }

    public showBemerkungUnterhaltsvereinbarung(): boolean {
        return this.getFamiliensituation()?.unterhaltsvereinbarung === TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;
    }

    public showFrageGeteilteObhut(): boolean {
        if (this.getFamiliensituation() && this.situationFKJV) {
            return this.isFamilienstatusAlleinerziehendOrShortKonkubinat()
                || this.isFamilienstatusKonkubinatKeinKindAndSmallerThanXYears();
        }
        return false;
    }

    private isFamilienstatusAlleinerziehendOrShortKonkubinat(): boolean {
        if (!this.getFamiliensituation()) {
            return false;
        }
        if (this.getFamiliensituation().familienstatus === TSFamilienstatus.ALLEINERZIEHEND) {
            return true;
        }
        return this.getFamiliensituation().familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND
            && this.getFamiliensituation()
                .konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(this.getGesuch().gesuchsperiode);
    }

    private isFamilienstatusKonkubinatKeinKindAndSmallerThanXYears(): boolean {
        if (!this.getFamiliensituation()) {
            return false;
        }
        return this.getFamiliensituation().familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND
            && this.getFamiliensituation()
                .konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(this.getGesuch().gesuchsperiode);
    }

    public frageGeteiltObhutClicked(): void {
        this.getFamiliensituation().gesuchstellerKardinalitaet = undefined;
        this.getFamiliensituation().unterhaltsvereinbarung = undefined;
        this.getFamiliensituation().unterhaltsvereinbarungBemerkung = undefined;
    }

    public frageUnterhaltsvereinbarungClicked(): void {
        this.getFamiliensituation().unterhaltsvereinbarungBemerkung = undefined;
    }

    public getTextForFamSitFrage2Tooltip(): string {
        return this.$translate.instant('FAMILIENSITUATION_HELP',
            {jahr: this.getFamiliensituation().minDauerKonkubinat});
    }

    public getTextForFamSitGeteilteObhut(): string {
        return this.$translate.instant('FAMILIENSITUATION_FRAGE_GEMEINSAME_OBHUT_INFO');
    }

    public getTextForFamSitUnterhaltsvereinbarung(): string {
        return this.$translate.instant('FAMILIENSITUATION_FRAGE_UNTERHALTSVEREINBARUNG_INFO');
    }

    public getTextForFamSitUnterhaltsvereinbarungGrund(): string {
        return this.$translate.instant('UNTERHALTSVEREINBARUNG_GRUND_INFO');
    }

    public getAllRolesButTraegerschaftInstitutionSteueramt(): ReadonlyArray<TSRole>  {
        return TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt();
    }

    public getTraegerschaftInstitutionSteueramtOnlyRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getTraegerschaftInstitutionSteueramtOnlyRoles();
    }
}
