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
import {IEntityRS} from '../../core/service/iEntityRS.rest';
import TSDossier from '../../models/TSDossier';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import WizardStepManager from './wizardStepManager';
import IRootScopeService = angular.IRootScopeService;

export default class DossierRS implements IEntityRS {
    serviceURL: string;
    http: IHttpService;
    ebeguRestUtil: EbeguRestUtil;

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager', '$rootScope'];
    /* @ngInject */
    constructor($http: IHttpService, REST_API: string, ebeguRestUtil: EbeguRestUtil, private $log: ILogService,
                private wizardStepManager: WizardStepManager, private $rootScope: IRootScopeService) {
        this.serviceURL = REST_API + 'dossier';
        this.http = $http;
        this.ebeguRestUtil = ebeguRestUtil;
    }

    public createDossier(dossier: TSDossier): IPromise<TSDossier> {
        let sentDossier = {};
        sentDossier = this.ebeguRestUtil.dossierToRestObject(sentDossier, dossier);
        return this.http.post(this.serviceURL, sentDossier, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response: any) => {
            this.$log.debug('PARSING dossier REST object ', response.data);
            return this.ebeguRestUtil.parseDossier(new TSDossier(), response.data);
        });
    }
}
