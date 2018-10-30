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
import {TSFachstelle} from '../../../models/TSFachstelle';
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

export class KindViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./kindView.html');
    public controller = KindViewController;
    public controllerAs = 'vm';
}

export class KindViewController extends AbstractGesuchViewController<TSKindContainer> {

    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        '$scope',
        'ErrorService',
        'WizardStepManager',
        '$q',
        '$translate',
        '$timeout',
    ];
    public geschlechter: Array<string>;
    public kinderabzugValues: Array<TSKinderabzug>;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    public showFachstelle: boolean;
    public showFachstelleGS: boolean;
    // der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    public fachstelleId: string;
    public allowedRoles: Array<TSRole>;

    public constructor(
        $stateParams: IKindStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        $scope: IScope,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.KINDER, $timeout);
        if ($stateParams.kindNumber) {
            const kindNumber = parseInt($stateParams.kindNumber, 10);
            const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kindNumber);
            if (kindIndex >= 0) {
                this.model = angular.copy(this.gesuchModelManager.getGesuch().kindContainers[kindIndex]);
                this.gesuchModelManager.setKindIndex(kindIndex);
            }
        } else {
            // wenn kind nummer nicht definiert ist heisst dass, das wir ein neues erstellen sollten
            this.model = this.initEmptyKind(undefined);
            const kindIndex = this.gesuchModelManager.getGesuch().kindContainers ?
                this.gesuchModelManager.getGesuch().kindContainers.length :
                0;
            this.gesuchModelManager.setKindIndex(kindIndex);
        }
        this.initViewModel();
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    private initViewModel(): void {
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.kinderabzugValues = getTSKinderabzugValues();
        this.einschulungTypValues = getTSEinschulungTypValues();

        this.showFachstelle = !!(this.model.kindJA.pensumFachstelle);
        this.showFachstelleGS = !!(this.model.kindGS && this.model.kindGS.pensumFachstelle);
        if (this.getPensumFachstelle() && this.getPensumFachstelle().fachstelle) {
            this.fachstelleId = this.getPensumFachstelle().fachstelle.id;
        }
        if (!this.gesuchModelManager.getFachstellenAnspruchList()
            || this.gesuchModelManager.getFachstellenAnspruchList().length <= 0
        ) {
            this.gesuchModelManager.updateFachstellenAnspruchList();
        }
    }

    public save(): IPromise<TSKindContainer> {
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

    public cancel(): void {
        this.reset();
        this.form.$setPristine();
    }

    public reset(): void {
        this.removeKindFromList();
    }

    private removeKindFromList(): void {
        if (!this.model.timestampErstellt) {
            // wenn das Kind noch nicht erstellt wurde, löschen wir das Kind vom Array
            this.gesuchModelManager.removeKindFromList();
        }
    }

    public setSelectedFachsstelle(): void {
        const fachstellenList = this.getFachstellenList();
        const found = fachstellenList.find(f => f.id === this.fachstelleId);
        if (found) {
            this.getModel().pensumFachstelle.fachstelle = found;
        }
    }

    public showFachstelleClicked(): void {
        if (this.showFachstelle) {
            this.getModel().pensumFachstelle = new TSPensumFachstelle();
        } else {
            this.resetFachstelleFields();
        }
    }

    public familienErgaenzendeBetreuungClicked(): void {
        if (!this.getModel().familienErgaenzendeBetreuung) {
            this.showFachstelle = false;
            this.resetFachstelleFields();
        }
    }

    private resetFachstelleFields(): void {
        this.fachstelleId = undefined;
        this.getModel().pensumFachstelle = undefined;
    }

    public getFachstellenList(): Array<TSFachstelle> {
        return this.gesuchModelManager.getFachstellenAnspruchList();
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
            const fachstelle = this.getContainer().kindGS.pensumFachstelle;
            const vonText = DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigAb, 'DD.MM.YYYY');
            const bisText = fachstelle.gueltigkeit.gueltigBis ?
                DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigBis, 'DD.MM.YYYY') :
                '31.12.9999';
            return this.$translate.instant('JA_KORREKTUR_FACHSTELLE', {
                name: fachstelle.fachstelle.name,
                pensum: fachstelle.pensum,
                von: vonText,
                bis: bisText,
            });
        }

        return this.$translate.instant('LABEL_KEINE_ANGABE');
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
