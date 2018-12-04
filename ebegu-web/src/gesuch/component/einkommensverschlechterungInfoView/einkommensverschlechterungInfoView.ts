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

import {IComponentOptions, IPromise} from 'angular';
import * as moment from 'moment';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {isAtLeastFreigegeben} from '../../../models/enums/TSAntragStatus';
import {getTSMonthValues, getTSMonthWithVorjahrValues, TSMonth} from '../../../models/enums/TSMonth';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSEinkommensverschlechterungContainer from '../../../models/TSEinkommensverschlechterungContainer';
import TSEinkommensverschlechterungInfo from '../../../models/TSEinkommensverschlechterungInfo';
import TSEinkommensverschlechterungInfoContainer from '../../../models/TSEinkommensverschlechterungInfoContainer';
import TSGesuchstellerContainer from '../../../models/TSGesuchstellerContainer';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import EinkommensverschlechterungContainerRS from '../../service/einkommensverschlechterungContainerRS.rest';
import EinkommensverschlechterungInfoRS from '../../service/einkommensverschlechterungInfoRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class EinkommensverschlechterungInfoViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./einkommensverschlechterungInfoView.html');
    public controller = EinkommensverschlechterungInfoViewController;
    public controllerAs = 'vm';
}

export class EinkommensverschlechterungInfoViewController
    extends AbstractGesuchViewController<TSEinkommensverschlechterungInfoContainer> {

    public static $inject: string[] = [
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'EbeguUtil',
        'WizardStepManager',
        'DvDialog',
        '$q',
        'EinkommensverschlechterungInfoRS',
        '$scope',
        'AuthServiceRS',
        'EinkommensverschlechterungContainerRS',
        '$timeout',
    ];

    public monthsStichtage: Array<TSMonth>;
    public monthsStichtageWithVorjahr: Array<TSMonth>;
    public selectedStichtagBjP1: TSMonth = undefined;
    public selectedStichtagBjP2: TSMonth = undefined;
    // tslint:disable-next-line:variable-name naming-convention
    public selectedStichtagBjP1_GS: TSMonth = undefined;
    // tslint:disable-next-line:variable-name naming-convention
    public selectedStichtagBjP2_GS: TSMonth = undefined;
    public initialEinkVersInfo: TSEinkommensverschlechterungInfoContainer;
    public allowedRoles: Array<TSRole>;
    public basisJahrUndPeriode = {
        jahr1periode: this.gesuchModelManager.getBasisjahrPlus(1),
        jahr2periode: this.gesuchModelManager.getBasisjahrPlus(2),
        basisjahr: this.gesuchModelManager.getBasisjahr(),
    };

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly ebeguUtil: EbeguUtil,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        private readonly $q: IQService,
        private readonly einkommensverschlechterungInfoRS: EinkommensverschlechterungInfoRS,
        $scope: IScope,
        private readonly authServiceRS: AuthServiceRS,
        private readonly ekvContainerRS: EinkommensverschlechterungContainerRS,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            $timeout);
        this.initialEinkVersInfo =
            angular.copy(this.gesuchModelManager.getGesuch().einkommensverschlechterungInfoContainer);
        this.model = angular.copy(this.initialEinkVersInfo);
        this.initViewModel();
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.monthsStichtage = getTSMonthValues();
        this.monthsStichtageWithVorjahr = getTSMonthWithVorjahrValues();
        this.selectedStichtagBjP1 =
            this.getMonatFromStichtag(this.getEinkommensverschlechterungsInfo().stichtagFuerBasisJahrPlus1, 1);
        this.selectedStichtagBjP2 =
            this.getMonatFromStichtag(this.getEinkommensverschlechterungsInfo().stichtagFuerBasisJahrPlus2, 2);
        if (this.getEinkommensverschlechterungsInfoGS()) {
            this.selectedStichtagBjP1_GS =
                this.getMonatFromStichtag(this.getEinkommensverschlechterungsInfoGS().stichtagFuerBasisJahrPlus1, 1);
            this.selectedStichtagBjP2_GS =
                this.getMonatFromStichtag(this.getEinkommensverschlechterungsInfoGS().stichtagFuerBasisJahrPlus2, 2);
        }
        this.initializeEKVContainers();
    }

    public initEinkommensverschlechterungInfo(): void {
        if (!this.model) {
            this.model = new TSEinkommensverschlechterungInfoContainer();
            this.model.init();
        }
    }

    public getEinkommensverschlechterungsInfoContainer(): TSEinkommensverschlechterungInfoContainer {
        if (!this.model) {
            this.initEinkommensverschlechterungInfo();
        }
        return this.model;
    }

    public getEinkommensverschlechterungsInfo(): TSEinkommensverschlechterungInfo {
        return this.getEinkommensverschlechterungsInfoContainer().einkommensverschlechterungInfoJA;
    }

    public getEinkommensverschlechterungsInfoGS(): TSEinkommensverschlechterungInfo {
        return this.getEinkommensverschlechterungsInfoContainer().einkommensverschlechterungInfoGS;
    }

    public showEkvi(): boolean {
        return this.getEinkommensverschlechterungsInfo().einkommensverschlechterung;
    }

    public showJahrPlus1(): boolean {
        return this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus1;
    }

    public showJahrPlus2(): boolean {
        return this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus2;
    }

    public getBasisJahrPlusAsString(jahr: number): string {
        return this.ebeguUtil.getBasisJahrPlusAsString(this.gesuchModelManager.getGesuch().gesuchsperiode, jahr);
    }

    /**
     * Gibt den Tag (Moment) anhand des Jahres und Monat enum zurück
     */
    private getStichtagFromMonat(monat: TSMonth, basisJahrPlus: number): moment.Moment {
        if (!monat) {
            return null;
        }

        const jahr = this.gesuchModelManager.getBasisjahr() + basisJahrPlus;
        if (monat === TSMonth.VORJAHR) {
            return moment([jahr - 1, 11]); // 1. Dezember des Vorjahres
        }
        return moment([jahr, this.monthsStichtage.indexOf(monat)]);
    }

    /**
     * Gibt den Monat enum anhand des Stichtages zurück
     */
    private getMonatFromStichtag(stichtag: moment.Moment, basisJahrPlus: number): TSMonth {
        if (!stichtag) {
            return null;
        }

        if ((this.gesuchModelManager.getBasisjahr() + basisJahrPlus) !== stichtag.year()) {
            return TSMonth.VORJAHR;
        }
        return this.monthsStichtage[stichtag.month()];
    }

    public confirmAndSave(): IPromise<TSEinkommensverschlechterungInfoContainer> {
        if (this.isGesuchValid()) {
            if (!this.form.$dirty && !this.isThereSomethingNew()) {
                // If the model is new (it hasn't been saved yet) we need to save it
                // If there are no changes in form we don't need anything to update on Server and we could
                // return the promise immediately
                return this.$q.when(this.model);
            }
            if (this.isConfirmationRequired()) {
                return this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                    title: 'EINKVERS_WARNING',
                    deleteText: 'EINKVERS_WARNING_BESCHREIBUNG',
                }).then(() => {   // User confirmed changes
                    return this.save();
                });
            }
            return this.save();
        }
        return undefined;
    }

    /**
     * Sometimes there is something new to save though the form hasn't changed. This is the case when the model i.e.
     * the Einkommensverschlechterung is new (it hasn't been saved yet) or when due to a change in the
     * Familiensituation the GS2 is new and doesn't have an EKVContainer yet.
     */
    private isThereSomethingNew(): boolean {
        return (this.model && this.model.isNew())
            || this.isThereAnyEinkommenverschlechterung() && (
                this.gesuchModelManager.isGesuchsteller2Required()
                && this.gesuchModelManager.getGesuch().gesuchsteller2
                && (
                    !this.gesuchModelManager.getGesuch().gesuchsteller2.einkommensverschlechterungContainer
                    || this.gesuchModelManager.getGesuch().gesuchsteller2.einkommensverschlechterungContainer.isNew()
                )
            );
    }

    private isThereAnyEinkommenverschlechterung(): boolean {
        const infoContainer = this.gesuchModelManager.getGesuch().einkommensverschlechterungInfoContainer;

        return infoContainer
            && infoContainer.einkommensverschlechterungInfoJA
            && infoContainer.einkommensverschlechterungInfoJA.einkommensverschlechterung;
    }

    private save(): IPromise<TSEinkommensverschlechterungInfoContainer> {
        this.errorService.clearAll();
        if (!this.isFinanzielleSituationRequired()) {
            // just return the existing one
            return this.$q.when(this.gesuchModelManager.getGesuch().einkommensverschlechterungInfoContainer);
        }

        if (this.getEinkommensverschlechterungsInfo().einkommensverschlechterung) {
            if (this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus1 === undefined) {
                this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus1 = false;
            }
            if (this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus2 === undefined) {
                this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus2 = false;
            }
            this.getEinkommensverschlechterungsInfo().stichtagFuerBasisJahrPlus1 =
                this.getStichtagFromMonat(this.selectedStichtagBjP1, 1);
            this.getEinkommensverschlechterungsInfo().stichtagFuerBasisJahrPlus2 =
                this.getStichtagFromMonat(this.selectedStichtagBjP2, 2);

            this.initializeEKVContainers();
        } else {
            // wenn keine EV eingetragen wird, setzen wir alles auf undefined, da keine Daten gespeichert werden
            // sollen
            this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus1 = false;
            this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus2 = false;
            this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP1 = undefined;
            this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP2 = undefined;
            this.getEinkommensverschlechterungsInfo().grundFuerBasisJahrPlus1 = undefined;
            this.getEinkommensverschlechterungsInfo().grundFuerBasisJahrPlus2 = undefined;
            this.getEinkommensverschlechterungsInfo().stichtagFuerBasisJahrPlus1 = undefined;
            this.getEinkommensverschlechterungsInfo().stichtagFuerBasisJahrPlus2 = undefined;
        }

        return this.einkommensverschlechterungInfoRS.saveEinkommensverschlechterungInfo(
            this.getEinkommensverschlechterungsInfoContainer(), this.gesuchModelManager.getGesuch().id)
            .then((ekvInfoRespo: TSEinkommensverschlechterungInfoContainer) => {
                this.gesuchModelManager.getGesuch().einkommensverschlechterungInfoContainer = ekvInfoRespo;
                return this.loadEKVContainersFromServer().then(() => {
                    return ekvInfoRespo;
                });
            });

    }

    private initializeEKVContainers(): void {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch.gesuchsteller1 && !gesuch.gesuchsteller1.einkommensverschlechterungContainer) {
            gesuch.gesuchsteller1.einkommensverschlechterungContainer = new TSEinkommensverschlechterungContainer();
        }
        if (this.gesuchModelManager.isGesuchsteller2Required()
            && !gesuch.gesuchsteller2.einkommensverschlechterungContainer) {
            gesuch.gesuchsteller2.einkommensverschlechterungContainer = new TSEinkommensverschlechterungContainer();
        }
    }

    private loadEKVContainersFromServer(): IPromise<TSEinkommensverschlechterungContainer> {
        if (!this.gesuchModelManager.getGesuch().gesuchsteller1) {
            return undefined;
        }

        const id = this.gesuchModelManager.getGesuch().gesuchsteller1.id;

        return this.ekvContainerRS.findEKVContainerForGesuchsteller(id)
            .then((responseGS1: TSEinkommensverschlechterungContainer) => {
                const gesuch = this.gesuchModelManager.getGesuch();
                gesuch.gesuchsteller1.einkommensverschlechterungContainer = responseGS1;

                if (this.gesuchModelManager.isGesuchsteller2Required() && gesuch.gesuchsteller2) {
                    return this.ekvContainerRS.findEKVContainerForGesuchsteller(gesuch.gesuchsteller2.id)
                        .then(responseGS2 => {
                            this.gesuchModelManager.getGesuch().gesuchsteller2.einkommensverschlechterungContainer =
                                responseGS2;

                            return responseGS2;
                        });
                }
                return gesuch.gesuchsteller1.einkommensverschlechterungContainer;
            });
    }

    public removeEkvBasisJahrPlus1(gesuchsteller: TSGesuchstellerContainer): void {
        if (gesuchsteller && gesuchsteller.einkommensverschlechterungContainer) {
            gesuchsteller.einkommensverschlechterungContainer.ekvJABasisJahrPlus1 = undefined;
        }
    }

    public removeEkvBasisJahrPlus2(gesuchsteller: TSGesuchstellerContainer): void {
        if (gesuchsteller && gesuchsteller.einkommensverschlechterungContainer) {
            gesuchsteller.einkommensverschlechterungContainer.ekvJABasisJahrPlus2 = undefined;
        }
    }

    public isRequired(basisJahrPlus: number): boolean {
        const info = this.getEinkommensverschlechterungsInfo();

        return basisJahrPlus === 2 ?
            info && !info.ekvFuerBasisJahrPlus1 :
            info && !info.ekvFuerBasisJahrPlus2;
    }

    /**
     * Confirmation is required when the user already introduced data for the EV and is about to remove it
     */
    private isConfirmationRequired(): boolean {
        const info = this.getEinkommensverschlechterungsInfo();

        return this.initialEinkVersInfo && this.initialEinkVersInfo.einkommensverschlechterungInfoJA
            && info
            && !info.einkommensverschlechterung
            && this.hasGS1Ekv();
    }

    /**
     * Checks whether the GS1 exists and has an Einkommensverschlechterung
     */
    private hasGS1Ekv(): boolean {
        return this.gesuchModelManager.getGesuch().gesuchsteller1
            && this.gesuchModelManager.getGesuch().gesuchsteller1.einkommensverschlechterungContainer !== null
            && this.gesuchModelManager.getGesuch().gesuchsteller1.einkommensverschlechterungContainer !== undefined
            && !this.gesuchModelManager.getGesuch().gesuchsteller1.einkommensverschlechterungContainer.isEmpty();
    }

    public isAmt(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole());
    }

    public isGesuchFreigegeben(): boolean {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().status) {
            return isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status);
        }
        return false;
    }

    public showAblehnungBasisJahrPlus1(): boolean {
        return (!this.isAmt() && this.showEkvi() && this.showJahrPlus1()
            && this.getEinkommensverschlechterungsInfo().ekvBasisJahrPlus1Annulliert && this.isGesuchFreigegeben())
            || (this.isAmt() && this.showEkvi() && this.showJahrPlus1());
    }

    public showAblehnungBasisJahrPlus2(): boolean {
        return (!this.isAmt() && this.showEkvi() && this.showJahrPlus2()
            && this.getEinkommensverschlechterungsInfo().ekvBasisJahrPlus2Annulliert && this.isGesuchFreigegeben())
            || (this.isAmt() && this.showEkvi() && this.showJahrPlus2());
    }

    public isFinanzielleSituationRequired(): boolean {
        return this.gesuchModelManager.isFinanzielleSituationEnabled() && this.gesuchModelManager.isFinanzielleSituationRequired();
    }
}
