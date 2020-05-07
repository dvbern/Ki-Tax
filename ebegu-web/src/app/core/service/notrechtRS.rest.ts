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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IHttpService, ILogService, IPromise} from 'angular';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class NotrechtRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}notrecht`;

    }

    public initializeRueckforderungFormulare(): IPromise<void> {
        return this.$http.post(`${this.serviceURL}/initialize`, {})
            .then(response => {
                const rueckforderungFormulare = this.ebeguRestUtil.parseRueckforderungFormularList(response.data);
                console.log(rueckforderungFormulare);
            }, error => {
                this.$log.error(error);
            });
    }

    public getServiceName(): string {
        return 'NotrechtRS';
    }

    public findRueckforderungFormular(rueckforderungFormularID: string): IPromise<TSRueckforderungFormular> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(rueckforderungFormularID)}`)
            .then((response: any) => {
                this.$log.debug('PARSING RueckforderungFormular REST object ', response.data);
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            });
    }
}
