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

import {IComponentOptions} from 'angular';
import GesuchModelManager from '../../service/gesuchModelManager';
import {StateService} from '@uirouter/core';
import TSKindContainer from '../../../models/TSKindContainer';
import AbstractGesuchViewController from '../abstractGesuchView';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import BerechnungsManager from '../../service/berechnungsManager';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import WizardStepManager from '../../service/wizardStepManager';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSKindDublette from '../../../models/TSKindDublette';
import EbeguUtil from '../../../utils/EbeguUtil';
import {IDVFocusableController} from '../../../core/component/IDVFocusableController';
import ITranslateService = angular.translate.ITranslateService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

let template = require('./kinderListView.html');
let removeDialogTempl = require('../../dialog/removeDialogTemplate.html');
require('./kinderListView.less');

export class KinderListViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {
        kinderDubletten: '<'
    };
    template = template;
    controller = KinderListViewController;
    controllerAs = 'vm';
}

export class KinderListViewController extends AbstractGesuchViewController<any> implements IDVFocusableController {

    kinderDubletten: TSKindDublette[] = [];

    static $inject: string[] = ['$state', 'GesuchModelManager', 'BerechnungsManager', '$translate', 'DvDialog',
        'WizardStepManager', '$scope', 'CONSTANTS', '$timeout'];

    /* @ngInject */
    constructor(private $state: StateService, gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                private $translate: ITranslateService, private DvDialog: DvDialog,
                wizardStepManager: WizardStepManager, $scope: IScope, private CONSTANTS: any, $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.KINDER, $timeout);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.gesuchModelManager.initKinder();

        if (this.gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()) {
            this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
        } else {
            this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        }
    }

    getKinderList(): Array<TSKindContainer> {
        return this.gesuchModelManager.getKinderList();
    }

    createKind(): void {
        this.openKindView(undefined); //neues kind hat noch keinen index
    }

    editKind(kind: any): void {
        if (kind) {         //check entfernt
            kind.isSelected = false; // damit die row in der Tabelle nicht mehr als "selected" markiert ist
            this.openKindView(kind.kindNummer);
        }
    }

    getDubletten(kindContainer: TSKindContainer): TSKindDublette[] {
        if (this.kinderDubletten) {
            let dublettenForThisKind: TSKindDublette[] = [];
            for (let i = 0; i < this.kinderDubletten.length; i++) {
                if (this.kinderDubletten[i].kindNummerOriginal === kindContainer.kindNummer) {
                    dublettenForThisKind.push(this.kinderDubletten[i]);
                }
            }
            return dublettenForThisKind;
        }
        return undefined;
    }

    public gotoKindDublette(dublette: TSKindDublette): void {
        let url = this.$state.href('gesuch.kind', {kindNumber: dublette.kindNummerDublette, gesuchId: dublette.gesuchId});
        window.open(url, '_blank');
    }

    private openKindView(kindNumber: number): void {
        this.$state.go('gesuch.kind', {kindNumber: kindNumber, gesuchId: this.getGesuchId()});
    }

    public getFallNummer(dublette: TSKindDublette): string {
        return EbeguUtil.addZerosToFallNummer(dublette.fallNummer);
    }

    removeKind(kind: any, index: any): void {
        let remTitleText = this.$translate.instant('KIND_LOESCHEN', {kindname: kind.kindJA.getFullName()});
        this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: remTitleText,
            deleteText: 'KIND_LOESCHEN_BESCHREIBUNG',
            parentController: this,
            elementID: 'removeKindButton_' + index
        })
            .then(() => {   //User confirmed removal
                let kindIndex: number = this.gesuchModelManager.findKind(kind);
                if (kindIndex >= 0) {
                    this.gesuchModelManager.setKindIndex(kindIndex);
                    this.gesuchModelManager.removeKind();
                }
            });
    }

    /**
     * Ein Kind darf geloescht werden wenn: Das Gesuch noch nicht verfuegt/verfuegen ist und das vorgaengerId null
     * ist (es ist ein neues kind) oder in einer mutation wenn es (obwohl ein altes Kind) keine Betreuungen hat
     * @param kind
     * @returns {boolean}
     */
    public canRemoveKind(kind: TSKindContainer): boolean {
        return !this.isGesuchReadonly()
            && ((this.gesuchModelManager.getGesuch().isMutation() && (!kind.betreuungen || kind.betreuungen.length <= 0))
                || !kind.kindJA.vorgaengerId);
    }

    public getColsNumber(): number {
        return this.kinderDubletten ? 6 : 5;
    }

    public setFocusBack(elementID: string): void {
        angular.element('#' + elementID).first().focus();
    }

}
