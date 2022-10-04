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
import {TSEinkommensverschlechterungInfoContainer} from '../../models/TSEinkommensverschlechterungInfoContainer';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {WizardStepManager} from './wizardStepManager';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;

export class EinkommensverschlechterungInfoRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public $log: ILogService,
        private readonly wizardStepManager: WizardStepManager
    ) {
        this.serviceURL = `${REST_API}einkommensverschlechterungInfo`;
    }

    public saveEinkommensverschlechterungInfo(
        einkommensverschlechterungInfoContainer: TSEinkommensverschlechterungInfoContainer,
        gesuchId: string
    ): IPromise<TSEinkommensverschlechterungInfoContainer> {

        let returnedEinkommensverschlechterungInfo = {};
        returnedEinkommensverschlechterungInfo =
            this.ebeguRestUtil.einkommensverschlechterungInfoContainerToRestObject(
                returnedEinkommensverschlechterungInfo,
                einkommensverschlechterungInfoContainer);
        const url = `${this.serviceURL}/${encodeURIComponent(gesuchId)}`;

        return this.$http.put(url, returnedEinkommensverschlechterungInfo).then((httpresponse: any) => this.wizardStepManager.findStepsFromGesuch(gesuchId).then(() => {
                this.$log.debug('PARSING EinkommensverschlechterungInfo REST object ', httpresponse.data);
                const container = new TSEinkommensverschlechterungInfoContainer();

                return this.ebeguRestUtil.parseEinkommensverschlechterungInfoContainer(container, httpresponse.data);
            }));
    }

}
