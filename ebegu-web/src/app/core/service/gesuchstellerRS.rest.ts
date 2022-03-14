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
import {TSGesuchsteller} from '../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class GesuchstellerRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly wizardStepManager: WizardStepManager,
    ) {
        this.serviceURL = `${REST_API}gesuchsteller`;

    }

    public saveGesuchsteller(
        gesuchsteller: TSGesuchstellerContainer,
        gesuchId: string,
        gsNumber: number,
        umzug: boolean,
    ): IPromise<TSGesuchstellerContainer> {
        const gessteller = this.ebeguRestUtil.gesuchstellerContainerToRestObject({}, gesuchsteller);
        const url = `${this.serviceURL}/${encodeURIComponent(gesuchId)}/gsNumber/${gsNumber}/${umzug}`;

        return this.http.put(url, gessteller)
            .then(response => this.wizardStepManager.findStepsFromGesuch(gesuchId)
                .then(() =>
                    this.ebeguRestUtil.parseGesuchstellerContainer(new TSGesuchstellerContainer(), response.data)));
    }

    public findGesuchsteller(gesuchstellerID: string): IPromise<TSGesuchstellerContainer> {
        return this.http.get(`${this.serviceURL}/id/${encodeURIComponent(gesuchstellerID)}`)
            .then(response =>
                this.ebeguRestUtil.parseGesuchstellerContainer(new TSGesuchstellerContainer(), response.data));
    }

    public getServiceName(): string {
        return 'GesuchstellerRS';
    }

    public initGS2ZPVNr(email: string, gs2: TSGesuchsteller): IPromise<any> {
        return this.http.get(`${this.serviceURL}/initZPVNr/${encodeURIComponent(gs2.id)}?email=${encodeURIComponent(email)}`);
    }
}
