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

import {IHttpResponse, IHttpService} from 'angular';
import {TSFinanzielleSituationResultateDTO} from '../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSEinkommensverschlechterungContainer} from '../../models/TSEinkommensverschlechterungContainer';
import {TSFinanzModel} from '../../models/TSFinanzModel';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {WizardStepManager} from './wizardStepManager';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;

export class EinkommensverschlechterungContainerRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public $log: ILogService,
        private readonly wizardStepManager: WizardStepManager,
    ) {
        this.serviceURL = `${REST_API}einkommensverschlechterung`;
    }

    public saveEinkommensverschlechterungContainer(
        einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer,
        gesuchstellerId: string,
        gesuchId: string,
    ): IPromise<TSEinkommensverschlechterungContainer> {
        let returnedEinkommensverschlechterungContainer = {};
        returnedEinkommensverschlechterungContainer =
            this.ebeguRestUtil.einkommensverschlechterungContainerToRestObject(
                returnedEinkommensverschlechterungContainer,
                einkommensverschlechterungContainer);
        const url = `${this.serviceURL}/${gesuchstellerId}/${encodeURIComponent(gesuchId)}`;

        return this.$http.put(url, returnedEinkommensverschlechterungContainer).then(r => {
            return this.wizardStepManager.findStepsFromGesuch(gesuchId)
                .then(() => this.toEinkommensverschlechterung(r));
        });
    }

    public calculateEinkommensverschlechterung(
        gesuch: TSGesuch,
        basisJahrPlus: number,
    ): IPromise<TSFinanzielleSituationResultateDTO> {
        let gesuchToSend = {};
        gesuchToSend = this.ebeguRestUtil.gesuchToRestObject(gesuchToSend, gesuch);
        return this.$http.post(`${this.serviceURL}/calculate/${basisJahrPlus}`, gesuchToSend)
            .then(response => this.toFinanzielleSituationResult(response));
    }

    public calculateEinkommensverschlechterungTemp(
        finanzModel: TSFinanzModel,
        basisJahrPlus: number,
    ): IPromise<TSFinanzielleSituationResultateDTO> {
        let finanzenToSend = {};
        finanzenToSend = this.ebeguRestUtil.finanzModelToRestObject(finanzenToSend, finanzModel);
        return this.$http.post(`${this.serviceURL}/calculateTemp/${basisJahrPlus}`, finanzenToSend)
            .then(response => this.toFinanzielleSituationResult(response));
    }

    public findEinkommensverschlechterungContainer(einkommensverschlechterungID: string,
    ): IPromise<TSEinkommensverschlechterungContainer> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(einkommensverschlechterungID)}`)
            .then(r => this.toEinkommensverschlechterung(r));
    }

    public findEKVContainerForGesuchsteller(gesuchstellerID: string): IPromise<TSEinkommensverschlechterungContainer> {
        return this.$http.get(`${this.serviceURL}/forGesuchsteller/${encodeURIComponent(gesuchstellerID)}`)
            .then(r => this.toEinkommensverschlechterung(r));
    }

    public calculateProzentualeDifferenz(betragJahr: number, betragJahrPlus1: number): IPromise<string> {
        return this.$http.post(`${this.serviceURL}/calculateDifferenz/${betragJahr}/${betragJahrPlus1}`, null)
            .then(httpresponse =>  httpresponse.data as string);
    }

    private toEinkommensverschlechterung(response: IHttpResponse<any>): TSEinkommensverschlechterungContainer {
        this.$log.debug('PARSING EinkommensverschlechterungContainer REST object ', response.data);
        const container = new TSEinkommensverschlechterungContainer();

        return this.ebeguRestUtil.parseEinkommensverschlechterungContainer(container, response.data);
    }

    private toFinanzielleSituationResult(httpresponse: IHttpResponse<any>): TSFinanzielleSituationResultateDTO {
        this.$log.debug('PARSING Einkommensverschlechterung Result  REST object ', httpresponse.data);
        const result = new TSFinanzielleSituationResultateDTO();

        return this.ebeguRestUtil.parseFinanzielleSituationResultate(result, httpresponse.data);
    }

    public getMinimalesMassgebendesEinkommenForGesuch(gesuch: TSGesuch): IPromise<number> {
        return this.$http.get<any>(`${this.serviceURL}/minimalesMassgebendesEinkommen/${gesuch.id}`, null)
            .then(httpresponse => parseFloat(httpresponse.data.minEinkommen));
    }
}
