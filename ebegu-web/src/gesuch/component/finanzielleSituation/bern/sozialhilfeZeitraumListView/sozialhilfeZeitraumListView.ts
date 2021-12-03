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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/core';
import {IComponentOptions, IScope, ITimeoutService} from 'angular';
import {IDVFocusableController} from '../../../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {SozialhilfeZeitraumRS} from '../../../../../app/core/service/sozialhilfeZeitraumRS.rest';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSSozialhilfeZeitraumContainer} from '../../../../../models/TSSozialhilfeZeitraumContainer';
import {RemoveDialogController} from '../../../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../../abstractGesuchView';

const removeDialogTemplate = require('../../../../dialog/removeDialogTemplate.html');

export class SozialhilfeZeitraumListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./sozialhilfeZeitraumListView.html');
    public controller = SozialhilfeZeitraumListViewController;
    public controllerAs = 'vm';
}

export class SozialhilfeZeitraumListViewController extends AbstractGesuchViewController<TSSozialhilfeZeitraumContainer>
    implements IDVFocusableController {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        'DvDialog',
        '$scope',
        '$timeout',
        'SozialhilfeZeitraumRS',
    ];

    public gesuchModelManager: GesuchModelManager;
    public sozialhilfeZeitraeume: TSSozialhilfeZeitraumContainer[];

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        $scope: IScope,
        $timeout: ITimeoutService,
        private readonly sozialhilfeZeitraumRS: SozialhilfeZeitraumRS,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.initSozialhilfeZeitraumList();
        if (this.isSaveDisabled()) {
            this.wizardStepManager.updateWizardStepStatus(
                TSWizardStepName.FINANZIELLE_SITUATION,
                TSWizardStepStatus.NOK);
        } else {
            this.wizardStepManager.updateWizardStepStatus(
                TSWizardStepName.FINANZIELLE_SITUATION,
                TSWizardStepStatus.OK);
        }
    }

    public initSozialhilfeZeitraumList(): void {
        if (this.sozialhilfeZeitraeume !== undefined) {
            return;
        }
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().familiensituationContainer &&
            this.gesuchModelManager.getGesuch().familiensituationContainer.sozialhilfeZeitraumContainers) {
            const familiensituationContainer = this.gesuchModelManager.getGesuch().familiensituationContainer;
            this.sozialhilfeZeitraeume = familiensituationContainer.sozialhilfeZeitraumContainers;
        } else {
            this.sozialhilfeZeitraeume = [];
        }
    }

    public createSozialhilfeZeitraum(): void {
        this.openSozialhilfeZeitraumView(undefined);
    }

    public removeSozialhilfeZeitraum(
        sozialhilfeZeitraum: TSSozialhilfeZeitraumContainer,
        elementId: string,
        index: any,
    ): void {
        this.errorService.clearAll();
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: '',
            title: 'SOZIALHILFEZEITRAUM_LOESCHEN',
            parentController: this,
            elementID: elementId + String(index),
        })
            .then(() => {   // User confirmed removal
                this.sozialhilfeZeitraumRS.removeSozialhilfeZeitraum(sozialhilfeZeitraum.id).then(() => {
                    this.sozialhilfeZeitraeume.splice(index, 1);
                });
            });
    }

    public editSozialhilfeZeitraum(sozialhilfeZeitraum: any): void {
        const index = this.findIndexOfSozialhilfeZeitraum(sozialhilfeZeitraum);
        this.openSozialhilfeZeitraumView(index);
    }

    private findIndexOfSozialhilfeZeitraum(sozialhilfeZeitraum: any): number {
        return this.gesuchModelManager.getGesuch()
            .familiensituationContainer
            .sozialhilfeZeitraumContainers
            .indexOf(sozialhilfeZeitraum);
    }

    private openSozialhilfeZeitraumView(sozialhilfeZeitraumNum: number): void {
        this.$state.go('gesuch.SozialhilfeZeitraum', {
            sozialhilfeZeitraumNum,
            gesuchId: this.getGesuchId(),
        });
    }

    public setFocusBack(elementID: string): void {
        angular.element('#' + elementID).first().focus();
    }

    public isSaveDisabled(): boolean {
        return this.sozialhilfeZeitraeume && this.sozialhilfeZeitraeume.length <= 0;
    }

    public isRemoveAllowed(_sozialhilfeZeitraumToEdit: TSSozialhilfeZeitraumContainer): boolean {
        // Loeschen erlaubt, solange das Gesuch noch nicht readonly ist. Dies ist notwendig, weil sonst in die Zukunft
        // erfasste Sozialhilfe-Zeiträume bei doch nicht Eintreten der Sozialhilfe nicht gelöscht werden können
        return !this.isGesuchReadonly() && _sozialhilfeZeitraumToEdit.isGSContainerEmpty();
    }
}
