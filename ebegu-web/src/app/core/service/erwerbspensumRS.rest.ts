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

import {IHttpService, ILogService, IPromise} from 'angular';
import {WizardStepManager} from '../../../gesuch/service/wizardStepManager';
import {TSErwerbspensumContainer} from '../../../models/TSErwerbspensumContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class ErwerbspensumRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];

    public serviceURL: string;

    public constructor(
        public readonly http: IHttpService,
        REST_API: string,
        public readonly ebeguRestUtil: EbeguRestUtil,
        public readonly log: ILogService,
        private readonly wizardStepManager: WizardStepManager,
    ) {
        this.serviceURL = `${REST_API}erwerbspensen`;
    }

    public getServiceName(): string {
        return 'ErwerbspensumRS';
    }

    public findErwerbspensum(erwerbspensenContainerID: string): IPromise<TSErwerbspensumContainer> {
        return this.http.get(`${this.serviceURL}/${encodeURIComponent(erwerbspensenContainerID)}`)
            .then((response: any) => {
                this.log.debug('PARSING erwerbspensenContainer REST object ', response.data);
                return this.ebeguRestUtil.parseErwerbspensumContainer(new TSErwerbspensumContainer(), response.data);
            });
    }

    public saveErwerbspensum(erwerbspensenContainer: TSErwerbspensumContainer, gesuchstellerID: string,
                             gesuchId: string,
    ): IPromise<TSErwerbspensumContainer> {
        let restErwerbspensum = {};
        restErwerbspensum =
            this.ebeguRestUtil.erwerbspensumContainerToRestObject(restErwerbspensum, erwerbspensenContainer);
        const url = `${this.serviceURL}/${encodeURIComponent(gesuchstellerID)}/${gesuchId}`;
        return this.http.put(url, restErwerbspensum).then((response: any) => {
            return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                this.log.debug('PARSING ErwerbspensumContainer REST object ', response.data);
                return this.ebeguRestUtil.parseErwerbspensumContainer(new TSErwerbspensumContainer(), response.data);
            });
        });
    }

    public removeErwerbspensum(erwerbspensumContID: string, gesuchId: string): IPromise<any> {
        const gesuchIdEnc = encodeURIComponent(gesuchId);
        const url = `${this.serviceURL}/gesuchId/${gesuchIdEnc}/erwPenId/${encodeURIComponent(erwerbspensumContID)}`;
        return this.http.delete(url)
            .then(response => {
                this.wizardStepManager.findStepsFromGesuch(gesuchId);
                return response;
            });
    }

    public isErwerbspensumRequired(gesuchId: string): IPromise<boolean> {
        return this.http.get(`${this.serviceURL}/required/${encodeURIComponent(gesuchId)}`)
            .then((response: any) => {
                return JSON.parse(response.data);
            });
    }
}
