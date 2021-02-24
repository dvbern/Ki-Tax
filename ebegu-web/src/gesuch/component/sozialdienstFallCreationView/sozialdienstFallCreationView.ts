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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IPromise, IQService, IScope} from 'angular';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {SozialdienstRS} from '../../../app/core/service/SozialdienstRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSSozialdienstFallStatus} from '../../../models/enums/TSSozialdienstFallStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSSozialdienstFall} from '../../../models/sozialdienst/TSSozialdienstFall';
import {TSGesuch} from '../../../models/TSGesuch';
import {INewFallStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class SozialdienstFallCreationViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./sozialdienstFallCreationView.html');
    public controller = SozialdienstFallCreationViewController;
    public controllerAs = 'vm';
}

export class SozialdienstFallCreationViewController extends AbstractGesuchViewController<any> {

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
        'SozialdienstRS',
        '$timeout',
    ];

    private sozialdienstFall: TSSozialdienstFall;

    // showError ist ein Hack damit, die Fehlermeldung fuer die Checkboxes nicht direkt beim Laden der Seite angezeigt
    // wird sondern erst nachdem man auf ein checkbox oder auf speichern geklickt hat
    public showError: boolean = false;

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
        private readonly sozialdienstRS: SozialdienstRS,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
            $timeout);
    }

    public $onInit(): void {
        super.$onInit();
        this.initViewModel();
    }

    public setShowError(showError: boolean): void {
        this.showError = showError;
    }

    private initViewModel(): void {
        this.sozialdienstFall = this.gesuchModelManager.getFall().sozialdienstFall;
    }

    // tslint:disable-next-line:cognitive-complexity
    public save(): IPromise<TSGesuch> {
        this.showError = true;
        if (!this.isGesuchValid()) {
            return undefined;
        }
        if (!this.form.$dirty && !this.gesuchModelManager.getFall().sozialdienstFall.isNew()) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.gesuchModelManager.getGesuch());
        }
        this.errorService.clearAll();
        return this.gesuchModelManager.saveGesuchAndFall();
    }

    public getGesuchModel(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getNextButtonText(): string {
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getFall().sozialdienstFall.isNew()) {
                return this.$translate.instant('ERSTELLEN');
            }
            if (this.gesuchModelManager.getFall().sozialdienstFall.status === TSSozialdienstFallStatus.AKTIV
                || this.gesuchModelManager.isGesuchReadonly()) {
                return this.$translate.instant('WEITER_ONLY');
            }

            return this.$translate.instant('SPEICHERN');

        }
        return this.$translate.instant('WEITER');
    }

    /**
     * There could be Gesuchsperiode in the list so we can chose it, or the gesuch has already a
     * gesuchsperiode set
     */
    public isSozialdienstFallReadOnly(): boolean {
        if (this.gesuchModelManager.getFall().sozialdienstFall.status === TSSozialdienstFallStatus.AKTIV) {
            return true;
        }
        return false;
    }

    public isAktivierungMoeglich(): boolean {
        if (this.gesuchModelManager.getFall().sozialdienstFall.status === TSSozialdienstFallStatus.INAKTIV) {
            return true;
        }
        return false;
    }

    public fallAktivieren(): void {
        // todo
    }
}
