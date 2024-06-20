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

import {
    IHttpPromise,
    IHttpResponse,
    IHttpService,
    ILogService,
    IPromise
} from 'angular';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import {TSGesuch} from '../../models/TSGesuch';
import {TSMahnung} from '../../models/TSMahnung';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

export class MahnungRS implements IEntityRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];

    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService
    ) {
        this.serviceURL = `${REST_API}mahnung`;
    }

    public saveMahnung(mahnung: TSMahnung): IPromise<TSMahnung> {
        let sentMahnung = {};
        sentMahnung = this.ebeguRestUtil.mahnungToRestObject(
            sentMahnung,
            mahnung
        );

        return this.$http
            .post(this.serviceURL, sentMahnung)
            .then((response: any) => {
                this.$log.debug('PARSING gesuch REST object ', response.data);

                return this.ebeguRestUtil.parseMahnung(
                    new TSMahnung(),
                    response.data
                );
            });
    }

    public findMahnungen(gesuchId: string): IPromise<TSMahnung[]> {
        return this.$http
            .get(`${this.serviceURL}/${encodeURIComponent(gesuchId)}`)
            .then((response: any) =>
                this.ebeguRestUtil.parseMahnungen(response.data)
            );
    }

    public mahnlaufBeenden(gesuch: TSGesuch): IPromise<TSGesuch> {
        return this.$http
            .put(`${this.serviceURL}/${encodeURIComponent(gesuch.id)}`, {})
            .then((response: IHttpResponse<TSGesuch>) => {
                this.$log.debug('PARSING gesuch REST object ', response.data);
                return this.ebeguRestUtil.parseGesuch(
                    new TSGesuch(),
                    response.data
                );
            });
    }

    public getInitialeBemerkungen(gesuch: TSGesuch): IHttpPromise<string> {
        return this.$http.get(
            `${this.serviceURL}/bemerkungen/${encodeURIComponent(gesuch.id)}`,
            {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }
        );
    }
}
