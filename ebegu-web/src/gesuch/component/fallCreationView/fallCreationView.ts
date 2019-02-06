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

import {IComponentOptions, IPromise, IQService, IScope} from 'angular';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuch from '../../../models/TSGesuch';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import DateUtil from '../../../utils/DateUtil';
import {INewFallStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class FallCreationViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./fallCreationView.html');
    public controller = FallCreationViewController;
    public controllerAs = 'vm';
}

export class FallCreationViewController extends AbstractGesuchViewController<any> {

    public static $inject = [
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        '$stateParams',
        'WizardStepManager',
        '$translate',
        '$q',
        '$scope',
        'AuthServiceRS',
        'GesuchsperiodeRS',
        '$timeout',
    ];
    private gesuchsperiodeId: string;

    // showError ist ein Hack damit, die Fehlermeldung fuer die Checkboxes nicht direkt beim Laden der Seite angezeigt
    // wird sondern erst nachdem man auf ein checkbox oder auf speichern geklickt hat
    public showError: boolean = false;
    private gesuchsperiodenListe: Array<TSGesuchsperiode>;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly $stateParams: INewFallStateParams,
        wizardStepManager: WizardStepManager,
        private readonly $translate: ITranslateService,
        private readonly $q: IQService,
        $scope: IScope,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.GESUCH_ERSTELLEN,
            $timeout);
    }

    public $onInit(): void {
        this.readStateParams();
        this.initViewModel();
    }

    private readStateParams(): void {
        if (this.$stateParams.gesuchsperiodeId && this.$stateParams.gesuchsperiodeId !== '') {
            this.gesuchsperiodeId = this.$stateParams.gesuchsperiodeId;
        }
    }

    public setShowError(showError: boolean): void {
        this.showError = showError;
    }

    private initViewModel(): void {
        // gesuch should already have been initialized in resolve function
        if ((this.gesuchsperiodeId === null || this.gesuchsperiodeId === undefined || this.gesuchsperiodeId === '')
            && this.gesuchModelManager.getGesuchsperiode()) {
            this.gesuchsperiodeId = this.gesuchModelManager.getGesuchsperiode().id;
        }

        this.gesuchsperiodeRS.getAllPeriodenForGemeinde(this.gesuchModelManager.getDossier().gemeinde.id, this.gesuchModelManager.getDossier().id).then(
            (response: TSGesuchsperiode[]) => {
                this.gesuchsperiodenListe = angular.copy(response);
            });
    }

    // tslint:disable-next-line:cognitive-complexity
    public save(): IPromise<TSGesuch> {
        this.showError = true;
        if (this.isGesuchValid()) {
            if (!this.form.$dirty && !this.gesuchModelManager.getGesuch().isNew()) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.gesuchModelManager.getGesuch());
            }
            this.errorService.clearAll();
            if (this.gesuchModelManager.getGesuch().isNew()) {
                if (this.gesuchModelManager.getGesuch().typ === TSAntragTyp.MUTATION) {
                    this.berechnungsManager.clear();
                    return this.gesuchModelManager.saveMutation();
                }
                if (this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH) {
                    this.berechnungsManager.clear();
                    return this.gesuchModelManager.saveErneuerungsgesuch();
                }
            }
            return this.gesuchModelManager.saveGesuchAndFall();
        }
        return undefined;
    }

    public getGesuchModel(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getAllActiveGesuchsperioden(): Array<TSGesuchsperiode> {
        return this.gesuchsperiodenListe;
    }

    public setSelectedGesuchsperiode(): void {
        const gesuchsperiodeList = this.getAllActiveGesuchsperioden();
        const found = gesuchsperiodeList.find(gp => gp.id === this.gesuchsperiodeId);
        if (found) {
            this.getGesuchModel().gesuchsperiode = found;
        }
    }

    public isGesuchsperiodeActive(): boolean {
        if (this.gesuchModelManager.getGesuchsperiode()) {
            return TSGesuchsperiodeStatus.AKTIV === this.gesuchModelManager.getGesuchsperiode().status
                || TSGesuchsperiodeStatus.INAKTIV === this.gesuchModelManager.getGesuchsperiode().status;
        }
        return true;
    }

    public getTitle(): string {
        if (!this.gesuchModelManager.getGesuch() || !this.gesuchModelManager.isGesuch()) {
            return this.$translate.instant('ART_DER_MUTATION');
        }
        if (this.gesuchModelManager.isGesuchSaved() && this.gesuchModelManager.getGesuchsperiode()) {
            const k = this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH ?
                'KITAX_ERNEUERUNGSGESUCH_PERIODE' :
                'KITAX_ERSTGESUCH_PERIODE';
            return this.$translate.instant(k, {
                periode: this.gesuchModelManager.getGesuchsperiode().gesuchsperiodeString,
            });
        }
        const key = this.gesuchModelManager.getGesuch().typ === TSAntragTyp.ERNEUERUNGSGESUCH ?
            'KITAX_ERNEUERUNGSGESUCH' :
            'KITAX_ERSTGESUCH';
        return this.$translate.instant(key);

    }

    public getNextButtonText(): string {
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getGesuch().isNew()) {
                return this.$translate.instant('ERSTELLEN');
            }
            if (this.gesuchModelManager.isGesuchReadonly()
                || this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getGesuchstellerOnlyRoles())) {
                return this.$translate.instant('WEITER_ONLY_UPPER');
            }
        }
        return this.$translate.instant('WEITER_UPPER');
    }

    public isSelectedGesuchsperiodeInaktiv(): boolean {
        return this.getGesuchModel() && this.getGesuchModel().gesuchsperiode
            && this.getGesuchModel().gesuchsperiode.status === TSGesuchsperiodeStatus.INAKTIV
            && this.getGesuchModel().isNew();
    }

    public canChangeGesuchsperiode(): boolean {
        return this.gesuchModelManager.getGesuch()
            && this.gesuchModelManager.isGesuch()
            && this.isGesuchsperiodeActive() && this.gesuchModelManager.getGesuch().isNew();
    }

    public getGemeinde(): TSGemeinde {
        return this.gesuchModelManager.getDossier().gemeinde;
    }

    public getPeriodString(): string {
        return DateUtil.calculatePeriodenStartdatumString(this.getGemeinde().betreuungsgutscheineStartdatum);
    }
}
