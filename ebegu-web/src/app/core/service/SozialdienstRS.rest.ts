/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class SozialdienstRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];

    public serviceURL: string;

    public constructor(
        public readonly $http: IHttpService,
        REST_API: string,
        public readonly ebeguRestUtil: EbeguRestUtil,
        public readonly $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}sozialdienst`;
    }

    public getServiceName(): string {
        return 'SozialdienstRS';
    }

    public createSozialdienst(sozialdienst: TSSozialdienst, email: string): IPromise<TSSozialdienst> {

        const restSozialdienst = this.ebeguRestUtil.sozialdienstToRestObject({}, sozialdienst);

        return this.$http.post(this.serviceURL, restSozialdienst,
            {
                params: {
                    adminMail: email,
                },
            })
            .then(response => {
                this.$log.debug('PARSING sozialdienst REST object ', response.data);
                return this.ebeguRestUtil.parseSozialdienst(new TSSozialdienst(), response.data);
            });
    }

    public getSozialdienstList(): IPromise<TSSozialdienst[]> {
        return this.$http.get<any[]>(this.serviceURL).then(response => {
            this.$log.debug('PARSING Sozialdienst REST array object', response.data);
            return this.ebeguRestUtil.parseSozialdienstList(response.data);
        });
    }
}
