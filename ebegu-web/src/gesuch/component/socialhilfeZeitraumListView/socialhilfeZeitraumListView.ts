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
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {SocialhilfeZeitraumRS} from '../../../app/core/service/socialhilfeZeitraumRS.rest';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSSocialhilfeZeitraumContainer} from '../../../models/TSSocialhilfeZeitraumContainer';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class SocialhilfeZeitraumListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./socialhilfeZeitraumListView.html');
    public controller = SocialhilfeZeitraumListViewController;
    public controllerAs = 'vm';
}

export class SocialhilfeZeitraumListViewController extends AbstractGesuchViewController<TSSocialhilfeZeitraumContainer> implements IDVFocusableController {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        'DvDialog',
        '$scope',
        '$timeout',
        'SocialhilfeZeitraumRS',
    ];

    public gesuchModelManager: GesuchModelManager;
    public socialhilfeZeitraeume: TSSocialhilfeZeitraumContainer[];

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        $scope: IScope,
        $timeout: ITimeoutService,
        private readonly socialhilfeZeitraumRS: SocialhilfeZeitraumRS,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.SOCIALHILFEZEITRAEUME,
            $timeout);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.initSocialhilfeZeitraumList();
    }

    public initSocialhilfeZeitraumList(): void {
        if (this.socialhilfeZeitraeume !== undefined) {
            return;
        }
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().familiensituationContainer &&
            this.gesuchModelManager.getGesuch().familiensituationContainer.socialhilfeZeitraumContainers) {
            const familiensituationContainer = this.gesuchModelManager.getGesuch().familiensituationContainer;
            this.socialhilfeZeitraeume = familiensituationContainer.socialhilfeZeitraumContainers;
        } else {
            this.socialhilfeZeitraeume = [];
        }
    }

    public createSocialhilfeZeitraum(): void {
        this.openSocialhilfeZeitraumView(undefined);
    }

    public removeSocialhilfeZeitraum(
        socialhilfeZeitraum: TSSocialhilfeZeitraumContainer,
        elementId: string,
        index: any,
    ): void {
        this.errorService.clearAll();
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: '',
            title: 'SOCIALHILFEZEITRAUM_LOESCHEN',
            parentController: this,
            elementID: elementId + String(index),
        })
            .then(() => {   // User confirmed removal
                this.socialhilfeZeitraumRS.removeSocialhilfeZeitraum(socialhilfeZeitraum.id).then(() => {
                    this.socialhilfeZeitraeume.splice(index, 1);
                });
            });
    }

    public editSocialhilfeZeitraum(socialhilfeZeitraum: any): void {
        const index = this.findIndexOfSocialhilfeZeitraum(socialhilfeZeitraum);
        this.openSocialhilfeZeitraumView(index);
    }

    private findIndexOfSocialhilfeZeitraum(socialhilfeZeitraum: any): number {
        return this.gesuchModelManager.getGesuch().familiensituationContainer.socialhilfeZeitraumContainers.indexOf(socialhilfeZeitraum);
    }

    private openSocialhilfeZeitraumView(socialhilfeZeitraumNum: number): void {
        this.$state.go('gesuch.SocialhilfeZeitraum', {
            socialhilfeZeitraumNum,
            gesuchId: this.getGesuchId(),
        });
    }

    public setFocusBack(elementID: string): void {
        angular.element('#' + elementID).first().focus();
    }

    public isSaveDisabled(): boolean {
        return false;
    }

    public isRemoveAllowed(_socialhilfeZeitraumToEdit: TSSocialhilfeZeitraumContainer): boolean {
        // Loeschen erlaubt, solange das Gesuch noch nicht readonly ist. Dies ist notwendig, weil sonst in die Zukunft
        // erfasste Sozialhilfe-Zeiträume bei doch nicht Eintreten der Sozialhilfe nicht gelöscht werden können
        return !this.isGesuchReadonly() && _socialhilfeZeitraumToEdit.isGSContainerEmpty();
    }
}
