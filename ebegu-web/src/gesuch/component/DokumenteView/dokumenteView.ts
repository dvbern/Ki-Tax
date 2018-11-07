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

import {IComponentOptions, ILogService} from 'angular';
import TSDokumenteDTO from '../../../models/dto/TSDokumenteDTO';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSDokument from '../../../models/TSDokument';
import TSDokumentGrund from '../../../models/TSDokumentGrund';
import EbeguUtil from '../../../utils/EbeguUtil';
import {IStammdatenStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import DokumenteRS from '../../service/dokumenteRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import GlobalCacheService from '../../service/globalCacheService';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export class DokumenteViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./dokumenteView.html');
    public controller = DokumenteViewController;
    public controllerAs = 'vm';
}

/**
 * Controller fuer den Dokumenten Upload
 */
export class DokumenteViewController extends AbstractGesuchViewController<any> {

    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        'DokumenteRS',
        '$log',
        'WizardStepManager',
        'GlobalCacheService',
        '$scope',
        '$timeout',
    ];
    public parsedNum: number;
    public dokumenteEkv: TSDokumentGrund[] = [];
    public dokumenteFinSit: TSDokumentGrund[] = [];
    public dokumenteFamSit: TSDokumentGrund[] = [];
    public dokumenteErwp: TSDokumentGrund[] = [];
    public dokumenteKinder: TSDokumentGrund[] = [];
    public dokumenteErwBetr: TSDokumentGrund[] = [];
    public dokumenteSonst: TSDokumentGrund[] = [];
    public dokumentePapiergesuch: TSDokumentGrund[] = [];
    public dokumenteFreigabequittung: TSDokumentGrund[] = [];

    public constructor(
        $stateParams: IStammdatenStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly dokumenteRS: DokumenteRS,
        private readonly $log: ILogService,
        wizardStepManager: WizardStepManager,
        private readonly globalCacheService: GlobalCacheService,
        $scope: IScope,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.DOKUMENTE, $timeout);
        this.parsedNum = parseInt($stateParams.gesuchstellerNumber, 10);
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.calculate();
    }

    public calculate(): void {
        if (!this.gesuchModelManager.getGesuch()) {
            this.$log.debug('No gesuch für dokumente');

            return;
        }

        this.berechnungsManager
            .getDokumente(this.gesuchModelManager.getGesuch())
            .then((alleDokumente: TSDokumenteDTO) => {
                this.searchDokumente(alleDokumente,
                    this.dokumenteEkv,
                    TSDokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
                this.searchDokumente(alleDokumente, this.dokumenteFinSit, TSDokumentGrundTyp.FINANZIELLESITUATION);
                this.searchDokumente(alleDokumente, this.dokumenteFamSit, TSDokumentGrundTyp.FAMILIENSITUATION);
                this.searchDokumente(alleDokumente, this.dokumenteErwp, TSDokumentGrundTyp.ERWERBSPENSUM);
                this.searchDokumente(alleDokumente, this.dokumenteKinder, TSDokumentGrundTyp.KINDER);
                this.searchDokumente(alleDokumente, this.dokumenteErwBetr, TSDokumentGrundTyp.ERWEITERTE_BETREUUNG);
                this.searchDokumente(alleDokumente, this.dokumenteSonst, TSDokumentGrundTyp.SONSTIGE_NACHWEISE);
                this.searchDokumente(alleDokumente, this.dokumentePapiergesuch, TSDokumentGrundTyp.PAPIERGESUCH);
                this.searchDokumente(alleDokumente,
                    this.dokumenteFreigabequittung,
                    TSDokumentGrundTyp.FREIGABEQUITTUNG);
            });
    }

    private searchDokumente(
        alleDokumente: TSDokumenteDTO,
        dokumenteForType: TSDokumentGrund[],
        dokumentGrundTyp: TSDokumentGrundTyp,
    ): void {

        alleDokumente.dokumentGruende
            .filter(tsDokument => tsDokument.dokumentGrundTyp === dokumentGrundTyp)
            .forEach(tsDokument => dokumenteForType.push(tsDokument));

        dokumenteForType.sort((n1: TSDokumentGrund, n2: TSDokumentGrund) => {
            let result = 0;

            if (n1 && n2) {
                if (n1.tag && n2.tag) {
                    result = n1.tag.localeCompare(n2.tag);
                }
                if (result === 0 && n1.dokumentTyp && n2.dokumentTyp) {
                    result = n1.dokumentTyp.toString().localeCompare(n2.dokumentTyp.toString());
                }
            }
            return result;
        });
    }

    public addUploadedDokuments(dokumentGrund: any, dokumente: TSDokumentGrund[]): void {
        this.$log.debug('addUploadedDokuments called');
        const index = EbeguUtil.getIndexOfElementwithID(dokumentGrund, dokumente);

        if (index > -1) {
            this.$log.debug('add dokument to dokumentList');
            dokumente[index] = dokumentGrund;

            // Clear cached Papiergesuch on add...
            if (dokumentGrund.dokumentGrundTyp === TSDokumentGrundTyp.PAPIERGESUCH) {
                this.globalCacheService.getCache(TSCacheTyp.EBEGU_DOCUMENT).removeAll();
            }
        }
        EbeguUtil.handleSmarttablesUpdateBug(dokumente);
    }

    public removeDokument(dokumentGrund: TSDokumentGrund, dokument: TSDokument, dokumente: TSDokumentGrund[]): void {

        const index = EbeguUtil.getIndexOfElementwithID(dokument, dokumentGrund.dokumente);

        if (index > -1) {
            this.$log.debug('add dokument to dokumentList');
            dokumentGrund.dokumente.splice(index, 1);
        }

        this.dokumenteRS.updateDokumentGrund(dokumentGrund).then(response => {

            const returnedDG = angular.copy(response);

            if (returnedDG) {
                // replace existing object in table with returned if returned not null
                const idx = EbeguUtil.getIndexOfElementwithID(returnedDG, dokumente);
                if (idx > -1) {
                    this.$log.debug('update dokumentGrund in dokumentList');
                    dokumente[idx] = dokumentGrund;

                    // Clear cached Papiergesuch on remove...
                    if (dokumentGrund.dokumentGrundTyp === TSDokumentGrundTyp.PAPIERGESUCH) {
                        this.globalCacheService.getCache(TSCacheTyp.EBEGU_DOCUMENT).removeAll();
                    }
                }
            } else {
                // delete object in table with sended if returned is null
                const idx = EbeguUtil.getIndexOfElementwithID(dokumentGrund, dokumente);
                if (idx > -1) {
                    this.$log.debug('remove dokumentGrund in dokumentList');
                    dokumente.splice(idx, 1);
                }
            }
            this.wizardStepManager.findStepsFromGesuch(this.gesuchModelManager.getGesuch().id);
        });

        EbeguUtil.handleSmarttablesUpdateBug(dokumente);
    }

    public showDokumenteGeprueftButton(): boolean {
        return this.gesuchModelManager.getGesuch().dokumenteHochgeladen;
    }

    public setDokumenteGeprueft(): void {
        this.gesuchModelManager.getGesuch().dokumenteHochgeladen = false;
        this.gesuchModelManager.updateGesuch();
    }
}
