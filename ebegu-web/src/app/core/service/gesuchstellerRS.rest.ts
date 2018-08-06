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

import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {IHttpService, ILogService, IPromise} from 'angular';
import WizardStepManager from '../../../gesuch/service/wizardStepManager';
import TSGesuchstellerContainer from '../../../models/TSGesuchstellerContainer';

export default class GesuchstellerRS {

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    serviceURL: string;

    constructor(public http: IHttpService, REST_API: string, public ebeguRestUtil: EbeguRestUtil, public log: ILogService,
                private readonly wizardStepManager: WizardStepManager) {
        this.serviceURL = REST_API + 'gesuchsteller';

    }

    public saveGesuchsteller(gesuchsteller: TSGesuchstellerContainer, gesuchId: string, gsNumber: number, umzug: boolean): IPromise<TSGesuchstellerContainer> {
        const gessteller = this.ebeguRestUtil.gesuchstellerContainerToRestObject({}, gesuchsteller);
        return this.http.put(this.serviceURL + '/' + encodeURIComponent(gesuchId) + '/gsNumber/' + gsNumber + '/' + umzug, gessteller, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response: any) => {
            return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                this.log.debug('PARSING gesuchsteller REST object ', response.data);
                return this.ebeguRestUtil.parseGesuchstellerContainer(new TSGesuchstellerContainer(), response.data);
            });
        });
    }

    public findGesuchsteller(gesuchstellerID: string): IPromise<TSGesuchstellerContainer> {
        return this.http.get(this.serviceURL + '/id/' + encodeURIComponent(gesuchstellerID))
            .then((response: any) => {
                this.log.debug('PARSING gesuchsteller REST object ', response.data);
                return this.ebeguRestUtil.parseGesuchstellerContainer(new TSGesuchstellerContainer(), response.data);
            });
    }

    public getServiceName(): string {
        return 'GesuchstellerRS';
    }

}

