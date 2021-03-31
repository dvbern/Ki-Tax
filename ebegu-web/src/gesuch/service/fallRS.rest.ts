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

import {IHttpPromise, IHttpService, ILogService, IPromise} from 'angular';
import {TSSprache} from '../../models/enums/TSSprache';
import {TSFall} from '../../models/TSFall';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

export class FallRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
    ) {
        this.serviceURL = REST_API + 'falle';
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

        return this.$http.put(this.serviceURL, fallObject, {
            headers: {
                'Content-Type': 'application/json',
            },
        }).then((response: any) => {
            this.$log.debug('PARSING fall REST object ', response.data);
            this.$log.debug('PARSed fall REST object ', this.ebeguRestUtil.parseFall(new TSFall(), response.data));
            return this.ebeguRestUtil.parseFall(new TSFall(), response.data);
        });
    }

    public findFall(fallID: string): IPromise<any> {
        return this.$http.get(`${this.serviceURL}/id/${encodeURIComponent(fallID)}`)
            .then((response: any) => {
                this.$log.debug('PARSING fall REST object ', response.data);
                return this.ebeguRestUtil.parseFall(new TSFall(), response.data);
            });
    }

    public removeVollmachtDokument(fallId: string): IHttpPromise<TSFall> {
        return this.$http.delete(`${this.serviceURL}/vollmachtDokument/${encodeURIComponent(fallId)}`);
    }

    public downloadVollmachtDokument(fallId: string): IPromise<BlobPart> {
        return this.$http.get(`${this.serviceURL}/downloadVollmachtDokument/${encodeURIComponent(fallId)}`,
            {responseType: 'blob'})
            .then((response: any) => {
                return response.data;
            });
    }

    public existVollmachtDokument(fallId: string): IPromise<boolean> {
        return this.$http.get(
            `${this.serviceURL}/existVollmachtDokument/${encodeURIComponent(fallId)}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getServiceName(): string {
        return 'FallRS';
    }

    public getVollmachtDokumentAccessTokenGeneratedDokument(
        fallId: string,
        sprache: TSSprache,
    ): angular.IPromise<BlobPart> {
        return this.$http.get(`${this.serviceURL}/generateVollmachtDokument/${encodeURIComponent(fallId)}/${sprache}`,
            {responseType: 'blob'})
            .then((response: any) => {
                return response.data;
            });
    }
}
