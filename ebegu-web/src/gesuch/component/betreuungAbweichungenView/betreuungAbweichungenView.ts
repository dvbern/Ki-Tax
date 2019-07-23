/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import {IComponentOptions} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {MULTIPLIER_KITA, MULTIPLIER_TAGESFAMILIEN} from '../../../app/core/constants/CONSTANTS';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import MitteilungRS from '../../../app/core/service/mitteilungRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungspensumAbweichungStatus} from '../../../models/enums/TSBetreuungspensumAbweichungStatus';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSBetreuung from '../../../models/TSBetreuung';
import TSBetreuungspensumAbweichung from '../../../models/TSBetreuungspensumAbweichung';
import TSKindContainer from '../../../models/TSKindContainer';
import EbeguUtil from '../../../utils/EbeguUtil';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import GlobalCacheService from '../../service/globalCacheService';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class BetreuungAbweichungenViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./betreuungAbweichungenView.html');
    public controller = BetreuungAbweichungenViewController;
    public controllerAs = 'vm';
}

export class BetreuungAbweichungenViewController extends AbstractGesuchViewController<TSBetreuung> {

    public static $inject = [
        '$state',
        'GesuchModelManager',
        'EbeguUtil',
        'CONSTANTS',
        '$scope',
        'BerechnungsManager',
        'ErrorService',
        'AuthServiceRS',
        'WizardStepManager',
        '$stateParams',
        'MitteilungRS',
        '$log',
        'EinstellungRS',
        'GlobalCacheService',
        '$timeout',
        '$translate',
    ];

    public $translate: ITranslateService;

    public kindModel: TSKindContainer;
    public isNewestGesuch: boolean;
    public isSavingData: boolean; // Semaphore

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        private readonly ebeguUtil: EbeguUtil,
        private readonly CONSTANTS: any,
        $scope: IScope,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $stateParams: IBetreuungStateParams,
        private readonly mitteilungRS: MitteilungRS,
        private readonly $log: ILogService,
        private readonly einstellungRS: EinstellungRS,
        private readonly globalCacheService: GlobalCacheService,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
        this.$translate = $translate;
    }

    // tslint:disable-next-line:cognitive-complexity
    public $onInit(): void {
        super.$onInit();

        const kindNumber = parseInt(this.$stateParams.kindNumber, 10);
        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kindNumber);
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            if (this.$stateParams.betreuungNumber && this.$stateParams.betreuungNumber.length > 0) {
                const betreuungNumber = parseInt(this.$stateParams.betreuungNumber, 10);
                const betreuungIndex = this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(betreuungNumber);
                this.model = angular.copy(this.gesuchModelManager.getKindToWorkWith().betreuungen[betreuungIndex]);

                this.gesuchModelManager.setBetreuungIndex(betreuungIndex);
            }

            // just to read!
            this.kindModel = this.gesuchModelManager.getKindToWorkWith();
        } else {
            this.$log.error('There is no kind available with kind-number:' + this.$stateParams.kindNumber);
        }
        this.isNewestGesuch = this.gesuchModelManager.isNeuestesGesuch();

        this.model = angular.copy(this.gesuchModelManager.getBetreuungToWorkWith());
        this.model.betreuungspensumAbweichungen.forEach(element => {
            this.percentageToEffective(element);
        });
    }

    public getKindModel(): TSKindContainer {
        return this.kindModel;
    }

    public getAbweichung(index: number): TSBetreuungspensumAbweichung {
        if (this.model.betreuungspensumAbweichungen && index >= 0
            && index < this.model.betreuungspensumAbweichungen.length) {
            return this.model.betreuungspensumAbweichungen[index];
        }

        return undefined;
    }

    public getFormattedDate(abweichung: TSBetreuungspensumAbweichung) {
        return `${abweichung.gueltigkeit.gueltigAb.month() + 1}.${abweichung.gueltigkeit.gueltigAb.year()}`;
    }

    private percentageToEffective(abweichung: TSBetreuungspensumAbweichung) {
        const multiplier = abweichung.unitForDisplay === TSPensumUnits.DAYS
            ? MULTIPLIER_KITA
            : MULTIPLIER_TAGESFAMILIEN;
        if (abweichung.pensum) {
            abweichung.pensum = Number((abweichung.pensum * multiplier).toFixed(2));
        }
        if (abweichung.originalPensumMerged) {
            abweichung.originalPensumMerged =  Number((abweichung.originalPensumMerged * multiplier).toFixed(2));
        }
    }

    private effectiveToPercentage(abweichung: TSBetreuungspensumAbweichung) {
        const multiplier = abweichung.unitForDisplay === TSPensumUnits.DAYS
            ? MULTIPLIER_KITA
            : MULTIPLIER_TAGESFAMILIEN;
        if (abweichung.pensum) {
            abweichung.pensum = Number((abweichung.pensum / multiplier));
        }
        if (abweichung.originalPensumMerged) {
            abweichung.originalPensumMerged =  Number((abweichung.originalPensumMerged / multiplier));
        }
    }

    public updateStatus (index: number): void {
        const abweichung = this.getAbweichung(index);
        abweichung.status = TSBetreuungspensumAbweichungStatus.NONE;

        if (abweichung.pensum && abweichung.monatlicheBetreuungskosten) {
            abweichung.status = TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN;
        }
    }

    public getIcon (index: number): string {
        const abweichung = this.getAbweichung(index);

        switch (abweichung.status) {
            case TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN:
                return 'fa-pencil black';
            case TSBetreuungspensumAbweichungStatus.VERRECHNET:
                return 'fa-hourglass orange';
            case TSBetreuungspensumAbweichungStatus.VERFUEGT:
                return 'fa-check green';
            default:
                return '';
        }
    }

    public save(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        // TODO KIBON-621: Umrechnung sollte auf Server stattfinden um Datenkonistenz zu gewährleisten
        this.model.betreuungspensumAbweichungen.forEach(element => {
            this.effectiveToPercentage(element);
        });

        this.gesuchModelManager.saveAbweichungen(this.model).then((result) => {
            this.gesuchModelManager.setBetreuungToWorkWith(result); // setze model
            this.model = result;
            this.model.betreuungspensumAbweichungen.forEach(element => {
                this.percentageToEffective(element);
            });
        });
    }
}
