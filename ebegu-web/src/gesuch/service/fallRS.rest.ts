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
import {TSSprache} from '../../models/enums/TSSprache';
import {TSSozialdienstFallDokument} from '../../models/sozialdienst/TSSozialdienstFallDokument';
import {TSFall} from '../../models/TSFall';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

export class FallRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService
    ) {
        this.serviceURL = `${REST_API}falle`;
    }

    public createFall(fall: TSFall): IPromise<any> {
        return this.saveFall(fall);
    }

    public updateFall(fall: TSFall): IPromise<any> {
        return this.saveFall(fall);
    }

    private saveFall(fall: TSFall): IPromise<TSFall> {
        let fallObject = {};
        fallObject = this.ebeguRestUtil.fallToRestObject(fallObject, fall);

        return this.$http
            .put(this.serviceURL, fallObject, {
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then((response: any) => {
                this.$log.debug('PARSING fall REST object ', response.data);
                this.$log.debug(
                    'PARSed fall REST object ',
                    this.ebeguRestUtil.parseFall(new TSFall(), response.data)
                );
                return this.ebeguRestUtil.parseFall(
                    new TSFall(),
                    response.data
                );
            });
    }

    public findFall(fallID: string): IPromise<any> {
        return this.$http
            .get(`${this.serviceURL}/id/${encodeURIComponent(fallID)}`)
            .then((response: any) => {
                this.$log.debug('PARSING fall REST object ', response.data);
                return this.ebeguRestUtil.parseFall(
                    new TSFall(),
                    response.data
                );
            });
    }

    public removeVollmachtDokument(
        sozialdienstFallDokumentId: string
    ): IHttpPromise<TSFall> {
        return this.$http.delete(
            `${this.serviceURL}/vollmachtDokument/${encodeURIComponent(sozialdienstFallDokumentId)}`
        );
    }

    public getAllVollmachtDokumente(
        sozialdienstFallId: string
    ): IPromise<TSSozialdienstFallDokument[]> {
        return this.$http
            .get(
                `${this.serviceURL}/vollmachtDokumente/${encodeURIComponent(sozialdienstFallId)}`
            )
            .then(restDokumente =>
                this.ebeguRestUtil.parseSozialdienstFallDokumente(
                    restDokumente.data
                )
            );
    }

    public getServiceName(): string {
        return 'FallRS';
    }

    public getVollmachtDokumentAccessTokenGeneratedDokument(
        fallId: string,
        sprache: TSSprache
    ): angular.IPromise<BlobPart> {
        return this.$http
            .get(
                `${this.serviceURL}/generateVollmachtDokument/${encodeURIComponent(fallId)}/${sprache}`,
                {responseType: 'blob'}
            )
            .then((response: any) => response.data);
    }

    public sozialdienstFallEntziehen(fallId: string): IPromise<TSFall> {
        return this.$http
            .put(
                `${this.serviceURL}/sozialdienstFallEntziehen/${encodeURIComponent(fallId)}`,
                {}
            )
            .then((response: IHttpResponse<TSFall>) => {
                this.$log.debug('PARSING gesuch REST object ', response.data);
                return this.ebeguRestUtil.parseFall(
                    new TSFall(),
                    response.data
                );
            });
    }
}
