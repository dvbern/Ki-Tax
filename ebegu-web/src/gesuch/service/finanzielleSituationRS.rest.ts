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

import {HttpHeaders} from '@angular/common/http';
import {IHttpService} from 'angular';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {EinstellungRS} from '../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {TSFinanzielleSituationAufteilungDTO} from '../../models/dto/TSFinanzielleSituationAufteilungDTO';
import {TSFinanzielleSituationResultateDTO} from '../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSEinstellungKey} from '../../models/enums/TSEinstellungKey';
import {TSFinanzielleSituationTyp} from '../../models/enums/TSFinanzielleSituationTyp';
import {TSEinstellung} from '../../models/TSEinstellung';
import {TSFinanzielleSituationContainer} from '../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../models/TSFinanzModel';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSGesuch} from '../../models/TSGesuch';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {TSGesuchsteller} from '../../models/TSGesuchsteller';
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
        gesuchsteller: TSGesuchstellerContainer
    ): IPromise<TSFinanzielleSituationContainer> {
        const url = `${this.serviceURL}/finanzielleSituation/${encodeURIComponent(gesuchId)}/${encodeURIComponent(
            gesuchsteller.id)}`;
        const finSitContainerToSend = this.ebeguRestUtil.finanzielleSituationContainerToRestObject({},
            gesuchsteller.finanzielleSituationContainer);
        return this.$http.put(url, finSitContainerToSend).then(response => this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(),
            response.data)));
    }

    public saveFinanzielleSituationStart(
        gesuch: TSGesuch
    ): IPromise<TSGesuch> {
        const sentGesuch = this.ebeguRestUtil.gesuchToRestObject({}, gesuch);
        const url = `${this.serviceURL}/finanzielleSituationStart`;

        if (!gesuch.gesuchsteller1.finanzielleSituationContainer) {
            this.$log.error(
                'This should never happen. If it happens check in sentry the breadcrums to try to reproduce it.'
                + ' Service will be called anyway and it will throw a NPE');
        }

        return this.$http.put(url, sentGesuch).then(response => this.wizardStepManager.findStepsFromGesuch(gesuch.id).then(() => this.ebeguRestUtil.parseGesuch(new TSGesuch(), response.data)));
    }

    public calculateFinanzielleSituation(gesuch: TSGesuch): IPromise<TSFinanzielleSituationResultateDTO> {
        let gesuchToSend = {};
        gesuchToSend = this.ebeguRestUtil.gesuchToRestObject(gesuchToSend, gesuch);
        return this.$http.post(`${this.serviceURL}/calculate`, gesuchToSend).then((httpresponse: any) => this.ebeguRestUtil.parseFinanzielleSituationResultate(new TSFinanzielleSituationResultateDTO(),
            httpresponse.data));
    }

    public calculateFinanzielleSituationTemp(finSitModel: TSFinanzModel): IPromise<TSFinanzielleSituationResultateDTO> {
        let finSitModelToSend = {};
        finSitModelToSend = this.ebeguRestUtil.finanzModelToRestObject(finSitModelToSend, finSitModel);
        return this.$http.post(`${this.serviceURL}/calculateTemp`, finSitModelToSend, {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
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

    public getFinanzielleSituationTyp(
        gesuchsperiode: TSGesuchsperiode,
        gemeinde: TSGemeinde
    ): Observable<TSFinanzielleSituationTyp> {
        return this.einstellungRS.findEinstellung(
            TSEinstellungKey.FINANZIELLE_SITUATION_TYP,
            gemeinde.id,
            gesuchsperiode.id
        ).pipe(
            map((einstellung: TSEinstellung) => this.ebeguRestUtil.parseFinanzielleSituationTyp(einstellung.value))
        );
    }

    public updateFinSitMitSteuerdaten(
        gesuchId: string,
        gesuchsteller: TSGesuchstellerContainer,
        isGemeinsam: boolean
    ): IPromise<TSFinanzielleSituationContainer> {
        const url = `${this.serviceURL}/kibonanfrage/${encodeURIComponent(gesuchId)}/${encodeURIComponent(
            gesuchsteller.id)}/${isGemeinsam}`;
        const finSitContainerToSend = this.ebeguRestUtil.finanzielleSituationContainerToRestObject({},
            gesuchsteller.finanzielleSituationContainer);
        return this.$http.put(url, finSitContainerToSend).then(response => this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(),
            response.data));
    }

    public updateFromAufteilung(aufteilungDTO: TSFinanzielleSituationAufteilungDTO, gesuch: TSGesuch): IPromise<any> {
        const url = `${this.serviceURL}/updateFromAufteilung/${encodeURIComponent(gesuch.id)}`;
        const restObj = this.ebeguRestUtil.aufteilungDTOToRestObject(aufteilungDTO);
        return this.$http.put(url, restObj);
    }

    public resetKiBonAnfrageFinSit(
        gesuchId: string,
        gesuchsteller: TSGesuchstellerContainer,
        isGemeinsam: boolean
    ): IPromise<TSFinanzielleSituationContainer> {
        const url = `${this.serviceURL}/kibonanfrage/reset/${encodeURIComponent(gesuchId)}/${encodeURIComponent(
            gesuchsteller.id)}/${isGemeinsam}`;
        const finSitContainerToSend = this.ebeguRestUtil.finanzielleSituationContainerToRestObject({},
            gesuchsteller.finanzielleSituationContainer);
        return this.$http.put(url, finSitContainerToSend).then(response => this.ebeguRestUtil.parseFinanzielleSituationContainer(new TSFinanzielleSituationContainer(),
            response.data));
    }

    public geburtsdatumMatchesSteuerabfrage(geburtsdatum: moment.Moment, finSitContainerId: string): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/geburtsdatum-matches-steuerabfrage/${finSitContainerId}?geburtsdatum=${geburtsdatum.format(CONSTANTS.DATE_FORMAT)}`).then(result => result.data as boolean);
    }

    public removeFinanzielleSituationFromGesuchsteller(gesuchsteller: TSGesuchstellerContainer): IPromise<TSGesuchstellerContainer> {
        const url = `${this.serviceURL}/remove/${encodeURIComponent(gesuchsteller.id)}`;
        return this.$http.delete(url)
            .then(response =>
                this.ebeguRestUtil.parseGesuchstellerContainer(new TSGesuchstellerContainer(), response.data));
    }
}
