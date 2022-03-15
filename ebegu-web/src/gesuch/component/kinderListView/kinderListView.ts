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

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSKindDublette} from '../../../models/TSKindDublette';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');

export class KinderListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        kinderDubletten: '<',
    };
    public template = require('./kinderListView.html');
    public controller = KinderListViewController;
    public controllerAs = 'vm';
}

export class KinderListViewController extends AbstractGesuchViewController<any> implements IDVFocusableController {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        'BerechnungsManager',
        '$translate',
        'DvDialog',
        'WizardStepManager',
        '$scope',
        '$timeout',
        'EinstellungRS'
    ];

    public kinderDubletten: TSKindDublette[] = [];
    private allFamilienPauschalAbzuegeZero: boolean = true;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly $translate: ITranslateService,
        private readonly dvDialog: DvDialog,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        $timeout: ITimeoutService,
        private readonly einstellungRS: EinstellungRS
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.KINDER, $timeout);
        this.initViewModel();
        this.initEinstellungen();
    }

    private initViewModel(): void {
        this.gesuchModelManager.initKinder();

        if (!this.gesuchModelManager.isThereAnyNotGeprueftesKind()
            && this.gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()) {
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.KINDER,
                TSWizardStepStatus.OK);
        } else {
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.KINDER,
                TSWizardStepStatus.IN_BEARBEITUNG);
        }
    }

    private initEinstellungen(): void {
        const gemeinde = this.gesuchModelManager.getGemeinde();
        const gs = this.gesuchModelManager.getGesuchsperiode();
        Promise.all([
            this.einstellungRS.findEinstellung(TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
                gemeinde.id,
                gs.id),
            this.einstellungRS.findEinstellung(TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
                gemeinde.id,
                gs.id),
            this.einstellungRS.findEinstellung(TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
                gemeinde.id,
                gs.id),
            this.einstellungRS.findEinstellung(TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
                gemeinde.id,
                gs.id),
        ]).then(einstellungen => {
            this.allFamilienPauschalAbzuegeZero = einstellungen.reduce((
                isZero: boolean,
                einstellung: TSEinstellung,
            ) => isZero && parseInt(einstellung.value, 10) === 0, true);
        });
    }

    public getKinderList(): Array<TSKindContainer> {
        return this.gesuchModelManager.getKinderList();
    }

    public isThereAnyNotGeprueftesKind(): boolean {
        return this.gesuchModelManager.isThereAnyNotGeprueftesKind();
    }

    public createKind(): void {
        this.openKindView(undefined); // neues kind hat noch keinen index
    }

    public editKind(kind: any): void {
        if (kind) {         // check entfernt
            kind.isSelected = false; // damit die row in der Tabelle nicht mehr als "selected" markiert ist
            this.openKindView(kind.kindNummer);
        }
    }

    public getDubletten(kindContainer: TSKindContainer): TSKindDublette[] {
        if (this.kinderDubletten) {
            return this.kinderDubletten.filter(kd => kd.kindNummerOriginal === kindContainer.kindNummer);
        }
        return undefined;
    }

    public gotoKindDublette(dublette: TSKindDublette): void {
        const url = this.$state.href('gesuch.kind',
            {kindNumber: dublette.kindNummerDublette, gesuchId: dublette.gesuchId});
        window.open(url, '_blank');
    }

    private openKindView(kindNumber: number): void {
        this.$state.go('gesuch.kind', {kindNumber, gesuchId: this.getGesuchId()});
    }

    public getFallNummer(dublette: TSKindDublette): string {
        return EbeguUtil.addZerosToFallNummer(dublette.fallNummer);
    }

    public removeKind(kind: any, index: any): void {
        const remTitleText = this.$translate.instant('KIND_LOESCHEN', {kindname: kind.kindJA.getFullName()});
        this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: remTitleText,
            deleteText: 'KIND_LOESCHEN_BESCHREIBUNG',
            parentController: this,
            elementID: `removeKindButton_${index}`,
        })
            .then(() => {   // User confirmed removal
                const kindIndex = this.gesuchModelManager.findKind(kind);
                if (kindIndex >= 0) {
                    this.gesuchModelManager.setKindIndex(kindIndex);
                    this.gesuchModelManager.removeKind();
                }
            });
    }

    /**
     * Ein Kind darf geloescht werden wenn: Das Gesuch noch nicht verfuegt/verfuegen ist und das vorgaengerId null
     * ist (es ist ein neues kind) oder in einer mutation wenn es (obwohl ein altes Kind) keine Betreuungen hat
     */
    public canRemoveKind(kind: TSKindContainer): boolean {
        return !this.isGesuchReadonly()
            && (
                (
                    this.gesuchModelManager.getGesuch().isMutation()
                    && (!kind.betreuungen || kind.betreuungen.length <= 0)
                )
                || !kind.kindJA.vorgaengerId
            );
    }

    public getColsNumber(): number {
        return this.kinderDubletten ? 6 : 5;
    }

    public setFocusBack(elementID: string): void {
        angular.element('#' + elementID).first().focus();
    }
}
