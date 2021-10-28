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
import {EinstellungRS} from '../../admin/service/einstellungRS.rest';
import {TSFinanzielleSituationResultateDTO} from '../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSEinstellungKey} from '../../models/enums/TSEinstellungKey';
import {TSFinanzielleSituationTyp} from '../../models/enums/TSFinanzielleSituationTyp';
import {TSFinanzielleSituationContainer} from '../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../models/TSFinanzModel';
import {TSGesuch} from '../../models/TSGesuch';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {TSGesuchstellerContainer} from '../../models/TSGesuchstellerContainer';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {WizardStepManager} from './wizardStepManager';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;

export class FinanzielleSituationRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager', 'EinstellungRS'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public $log: ILogService,
        private readonly wizardStepManager: WizardStepManager,
        private readonly einstellungRS: EinstellungRS
    ) {
        this.serviceURL = `${REST_API}finanzielleSituation`;
    }

    public saveFinanzielleSituation(
        gesuchId: string,
        gesuchsteller: TSGesuchstellerContainer,
    ): IPromise<TSFinanzielleSituationContainer> {
        const url = `${this.serviceURL}/finanzielleSituation/${encodeURIComponent(gesuchId)}/${encodeURIComponent(gesuchsteller.id)}`;
        const finSitContainerToSend = this.ebeguRestUtil.finanzielleSituationContainerToRestObject({}, gesuchsteller.finanzielleSituationContainer);
        return this.$http.put(url, finSitContainerToSend).then(response => {
            return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                return this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(), response.data);
            });
        });
    }

    public saveFinanzielleSituationStart(
        gesuch: TSGesuch,
    ): IPromise<TSGesuch> {
        const sentGesuch = this.ebeguRestUtil.gesuchToRestObject({}, gesuch);
        const url = `${this.serviceURL}/finanzielleSituationStart`;

        if (!gesuch.gesuchsteller1.finanzielleSituationContainer) {
            this.$log.error('This should never happen. If it happens check in sentry the breadcrums to try to reproduce it.'
                + ' Service will be called anyway and it will throw a NPE');
        }

        return this.$http.put(url, sentGesuch).then(response => {
            return this.wizardStepManager.findStepsFromGesuch(gesuch.id).then(() => {
                return this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data);
            });
        });
    }

    public calculateFinanzielleSituation(gesuch: TSGesuch): IPromise<TSFinanzielleSituationResultateDTO> {
        let gesuchToSend = {};
        gesuchToSend = this.ebeguRestUtil.gesuchToRestObject(gesuchToSend, gesuch);
        return this.$http.post(`${this.serviceURL}/calculate`, gesuchToSend).then((httpresponse: any) => {
            return this.ebeguRestUtil.parseFinanzielleSituationResultate(new TSFinanzielleSituationResultateDTO(),
                httpresponse.data);
        });
    }

    public calculateFinanzielleSituationTemp(finSitModel: TSFinanzModel): IPromise<TSFinanzielleSituationResultateDTO> {
        let finSitModelToSend = {};
        finSitModelToSend = this.ebeguRestUtil.finanzModelToRestObject(finSitModelToSend, finSitModel);
        return this.$http.post(`${this.serviceURL}/calculateTemp`, finSitModelToSend, {
            headers: {
                'Content-Type': 'application/json',
            },
        }).then((httpresponse: any) => {
            this.$log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseFinanzielleSituationResultate(new TSFinanzielleSituationResultateDTO(),
                httpresponse.data);
        });
    }

    public findFinanzielleSituation(finanzielleSituationID: string): IPromise<TSFinanzielleSituationContainer> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(finanzielleSituationID)}`)
            .then((httpresponse: any) => {
                this.$log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
                return this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(),
                    httpresponse.data);
            });
    }

    public getFinanzielleSituationTyp(gesuchsperiode: TSGesuchsperiode): IPromise<TSFinanzielleSituationTyp> {
        return this.einstellungRS.findEinstellung(TSEinstellungKey.FINANZIELLE_SITUATION_TYP, null, gesuchsperiode.id)
            .then((httpresponse: any) => {
                return this.ebeguRestUtil.parseFinanzielleSituationTyp(httpresponse.data);
            });
    }
}
