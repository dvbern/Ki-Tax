/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {IHttpService, ILogService, IPromise} from 'angular';
import {TSWizardStepX} from '../../../models/TSWizardStepX';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class WizardStepXRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}wizardstepX`;
    }

    public getServiceName(): string {
        return 'WizardStepXRS';
    }

    public getAllSteps(wizardStepTyp: string): IPromise<TSWizardStepX[]> {
        return this.http.get(`${this.serviceURL}/getAllSteps/${encodeURIComponent(wizardStepTyp)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseWizardStepXList(response.data);
            });
    }

    public initFirstStep(wizardStepTyp: string): IPromise<TSWizardStepX> {
        return this.http.get(`${this.serviceURL}/initFirstStep/${encodeURIComponent(wizardStepTyp)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseWizardStepX(response.data);
            });
    }

    public getNextStep(wizardStepTyp: string, wizardStep: string): IPromise<TSWizardStepX> {
        return this.http.get(`${this.serviceURL}/getNextStep/${encodeURIComponent(wizardStepTyp)}/${encodeURIComponent(
            wizardStep)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseWizardStepX(response.data);
            });
    }

    public getPreviousStep(wizardStepTyp: string, wizardStep: string): IPromise<TSWizardStepX> {
        return this.http.get(`${this.serviceURL}/getPreviousStep/${encodeURIComponent(wizardStepTyp)}/${encodeURIComponent(
            wizardStep)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseWizardStepX(response.data);
            });
    }
}
