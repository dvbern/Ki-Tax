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

import {IHttpService, IPromise} from 'angular';
import {TSEbeguVorlage} from '../../models/TSEbeguVorlage';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import IHttpPromise = angular.IHttpPromise;
import IQService = angular.IQService;

export class EbeguVorlageRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', 'Upload', '$q', 'base64'];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly upload: any,
        private readonly $q: IQService,
        private readonly base64: any
    ) {
        this.serviceURL = `${REST_API}ebeguVorlage`;
    }

    public uploadVorlage(
        file: any,
        ebeguVorlage: TSEbeguVorlage,
    ): IPromise<TSEbeguVorlage> {

        let restEbeguVorlage = {};
        restEbeguVorlage = this.ebeguRestUtil.ebeguVorlageToRestObject(restEbeguVorlage, ebeguVorlage);
        this.upload.json(restEbeguVorlage);
        const encodedFilename = this.base64.encode(file.name);
        return this.upload.upload({
            url: this.serviceURL,
            method: 'POST',
            headers: {
                'x-filename': encodedFilename,
                'x-vorlagekey': ebeguVorlage.name,
            },
            data: {
                file,
            },
        }).then((response: any) => {
            return this.ebeguRestUtil.parseEbeguVorlage(new TSEbeguVorlage(), response.data);
        }, (response: any) => {
            console.log('Upload File: NOT SUCCESS');
            return this.$q.reject(response);
        }, (evt: any) => {
            const loaded: number = evt.loaded;
            const total: number = evt.total;
            const progressPercentage = 100 * loaded / total;
            console.log(`progress: ${progressPercentage}% `);

            this.$q.defer().notify();
        });
    }

    public deleteEbeguVorlage(ebeguVorlageID: string): IHttpPromise<any> {
        return this.http.delete(`${this.serviceURL}/${encodeURIComponent(ebeguVorlageID)}`);
    }

    public getEbeguVorlagenWithoutGesuchsperiode(): IPromise<TSEbeguVorlage[]> {
        return this.http.get(`${this.serviceURL}/nogesuchsperiode/`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseEbeguVorlages(response.data);
            });
    }
}
