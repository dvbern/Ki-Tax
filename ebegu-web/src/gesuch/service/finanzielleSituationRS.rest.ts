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

import {IHttpService} from 'angular';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import TSFinanzielleSituationContainer from '../../models/TSFinanzielleSituationContainer';
import TSGesuch from '../../models/TSGesuch';
import TSFinanzielleSituationResultateDTO from '../../models/dto/TSFinanzielleSituationResultateDTO';
import WizardStepManager from './wizardStepManager';
import TSFinanzModel from '../../models/TSFinanzModel';
import IPromise = angular.IPromise;
import ILogService = angular.ILogService;

export default class FinanzielleSituationRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(public $http: IHttpService,
                       REST_API: string,
                       public ebeguRestUtil: EbeguRestUtil,
                       public $log: ILogService,
                       private readonly wizardStepManager: WizardStepManager) {
        this.serviceURL = REST_API + 'finanzielleSituation';
    }

    public saveFinanzielleSituation(finanzielleSituationContainer: TSFinanzielleSituationContainer, gesuchstellerId: string, gesuchId: string): IPromise<TSFinanzielleSituationContainer> {
        let returnedFinanzielleSituation = {};
        returnedFinanzielleSituation = this.ebeguRestUtil.finanzielleSituationContainerToRestObject(returnedFinanzielleSituation, finanzielleSituationContainer);
        return this.$http.put(this.serviceURL + '/' + encodeURIComponent(gesuchstellerId) + '/' + encodeURIComponent(gesuchId), returnedFinanzielleSituation, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((httpresponse: any) => {
            return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                this.$log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
                return this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(), httpresponse.data);
            });
        });
    }

    /**
     * Sendet zurzeit das komplette Gesuch.
     */
    public saveFinanzielleSituationStart(gesuch: TSGesuch): IPromise<TSGesuch> {
        let sentGesuch = {};
        sentGesuch = this.ebeguRestUtil.gesuchToRestObject(sentGesuch, gesuch);
        return this.$http.put(this.serviceURL + '/finsitStart/', sentGesuch, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(response => {
            return this.wizardStepManager.findStepsFromGesuch(gesuch.id).then(() => {
                this.$log.debug('PARSING gesuch REST object ', response.data);
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
        });
    }

    public calculateFinanzielleSituation(gesuch: TSGesuch): IPromise<TSFinanzielleSituationResultateDTO> {
        let gesuchToSend = {};
        gesuchToSend = this.ebeguRestUtil.gesuchToRestObject(gesuchToSend, gesuch);
        return this.$http.post(this.serviceURL + '/calculate', gesuchToSend, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((httpresponse: any) => {
            this.$log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseFinanzielleSituationResultate(new TSFinanzielleSituationResultateDTO(), httpresponse.data);
        });
    }

    public calculateFinanzielleSituationTemp(finSitModel: TSFinanzModel): IPromise<TSFinanzielleSituationResultateDTO> {
        let finSitModelToSend = {};
        finSitModelToSend = this.ebeguRestUtil.finanzModelToRestObject(finSitModelToSend, finSitModel);
        return this.$http.post(this.serviceURL + '/calculateTemp', finSitModelToSend, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((httpresponse: any) => {
            this.$log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseFinanzielleSituationResultate(new TSFinanzielleSituationResultateDTO(), httpresponse.data);
        });
    }

    public findFinanzielleSituation(finanzielleSituationID: string): IPromise<TSFinanzielleSituationContainer> {
        return this.$http.get(this.serviceURL + '/' + encodeURIComponent(finanzielleSituationID)).then((httpresponse: any) => {
            this.$log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(), httpresponse.data);
        });
    }
}
