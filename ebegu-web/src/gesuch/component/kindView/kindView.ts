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
import * as moment from 'moment';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {getTSKinderabzugValues, TSKinderabzug} from '../../../models/enums/TSKinderabzug';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSKind from '../../../models/TSKind';
import TSKindContainer from '../../../models/TSKindContainer';
import {TSPensumFachstelle} from '../../../models/TSPensumFachstelle';
import DateUtil from '../../../utils/DateUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {IKindStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const template = require('./kindView.html');
require('./kindView.less');

export class KindViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = KindViewController;
    controllerAs = 'vm';
}

export class KindViewController extends AbstractGesuchViewController<TSKindContainer> {

    static $inject: string[] = ['$stateParams', 'GesuchModelManager', 'BerechnungsManager', 'CONSTANTS', '$scope',
        'ErrorService', 'WizardStepManager', '$q', '$translate', '$timeout'];
    geschlechter: Array<string>;
    kinderabzugValues: Array<TSKinderabzug>;
    einschulungTypValues: Array<TSEinschulungTyp>;
    showFachstelle: boolean;
    showFachstelleGS: boolean;
    fachstelleId: string; //der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    allowedRoles: Array<TSRole>;

    /* @ngInject */
    constructor($stateParams: IKindStateParams, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private readonly CONSTANTS: any, $scope: IScope, private readonly errorService: ErrorService,
                wizardStepManager: WizardStepManager, private readonly $q: IQService, private readonly $translate: ITranslateService, $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.KINDER, $timeout);
        if ($stateParams.kindNumber) {
            const kindIndex: number = this.gesuchModelManager.convertKindNumberToKindIndex(parseInt($stateParams.kindNumber));
            if (kindIndex >= 0) {
                this.model = angular.copy(this.gesuchModelManager.getGesuch().kindContainers[kindIndex]);
                this.gesuchModelManager.setKindIndex(kindIndex);
            }
        } else {
            //wenn kind nummer nicht definiert ist heisst dass, das wir ein neues erstellen sollten
            this.model = this.initEmptyKind(undefined);
            const kindIndex: number = this.gesuchModelManager.getGesuch().kindContainers ? this.gesuchModelManager.getGesuch().kindContainers.length : 0;
            this.gesuchModelManager.setKindIndex(kindIndex);
        }
        this.initViewModel();
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    private initViewModel(): void {
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.kinderabzugValues = getTSKinderabzugValues();
        this.einschulungTypValues = getTSEinschulungTypValues();

        this.showFachstelle = (this.model.kindJA.pensumFachstelle) ? true : false;
        this.showFachstelleGS = (this.model.kindGS && this.model.kindGS.pensumFachstelle) ? true : false;
        if (this.getPensumFachstelle() && this.getPensumFachstelle().fachstelle) {
            this.fachstelleId = this.getPensumFachstelle().fachstelle.id;
        }
        if (!this.gesuchModelManager.getFachstellenList() || this.gesuchModelManager.getFachstellenList().length <= 0) {
            this.gesuchModelManager.updateFachstellenList();
        }
    }

    save(): IPromise<TSKindContainer> {
        if (this.isGesuchValid()) {
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.model);
            }

            this.errorService.clearAll();
            return this.gesuchModelManager.saveKind(this.model);
        }
        return undefined;
    }

    cancel() {
        this.reset();
        this.form.$setPristine();
    }

    reset() {
        this.removeKindFromList();
    }

    private removeKindFromList() {
        if (!this.model.timestampErstellt) {
            //wenn das Kind noch nicht erstellt wurde, löschen wir das Kind vom Array
            this.gesuchModelManager.removeKindFromList();
        }
    }

    public setSelectedFachsstelle() {
        const fachstellenList = this.getFachstellenList();
        const found = fachstellenList.find(f => f.id === this.fachstelleId);
        if (found) {
            this.getModel().pensumFachstelle.fachstelle = found;
        }
    }

    public showFachstelleClicked() {
        if (!this.showFachstelle) {
            this.resetFachstelleFields();
        } else {
            this.getModel().pensumFachstelle = new TSPensumFachstelle();
        }
    }

    public familienErgaenzendeBetreuungClicked() {
        if (!this.getModel().familienErgaenzendeBetreuung) {
            this.showFachstelle = false;
            this.getModel().wohnhaftImGleichenHaushalt = undefined;
            this.resetFachstelleFields();
        }
    }

    private resetFachstelleFields() {
        this.fachstelleId = undefined;
        this.getModel().pensumFachstelle = undefined;
    }

    public getFachstellenList() {
        return this.gesuchModelManager.getFachstellenList();
    }

    public getModel(): TSKind {
        if (this.model) {
            return this.model.kindJA;
        }
        return undefined;
    }

    public getContainer(): TSKindContainer {
        if (this.model) {
            return this.model;
        }
        return undefined;
    }

    public getPensumFachstelle(): TSPensumFachstelle {
        if (this.getModel()) {
            return this.getModel().pensumFachstelle;
        }
        return undefined;
    }

    public isFachstelleRequired(): boolean {
        return this.getModel() && this.getModel().familienErgaenzendeBetreuung && this.showFachstelle;
    }

    public getDatumEinschulung(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiodeBegin();
    }

    public getTextFachstelleKorrekturJA(): string {
        if (this.getContainer().kindGS && this.getContainer().kindGS.pensumFachstelle) {
            const fachstelle: TSPensumFachstelle = this.getContainer().kindGS.pensumFachstelle;
            const vonText = DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigAb, 'DD.MM.YYYY');
            const bisText = fachstelle.gueltigkeit.gueltigBis ? DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigBis, 'DD.MM.YYYY') : '31.12.9999';
            return this.$translate.instant('JA_KORREKTUR_FACHSTELLE', {
                name: fachstelle.fachstelle.name,
                pensum: fachstelle.pensum,
                von: vonText,
                bis: bisText
            });
        } else {
            return this.$translate.instant('LABEL_KEINE_ANGABE');
        }
    }

    private initEmptyKind(kindNumber: number): TSKindContainer {
        const tsKindContainer = new TSKindContainer(undefined, new TSKind());
        tsKindContainer.kindNummer = kindNumber;
        return tsKindContainer;
    }

    /**
     * Returns true if the Kind has a Betreuung
     */
    public hasKindBetreuungen(): boolean {
        return this.model.betreuungen && this.model.betreuungen.length > 0;
    }
}

