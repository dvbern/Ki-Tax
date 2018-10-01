/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {IHttpResponse, IHttpService, IPromise} from 'angular';
import {TSEinstellungKey} from '../../models/enums/TSEinstellungKey';
import TSEinstellung from '../../models/TSEinstellung';
import TSGemeinde from '../../models/TSGemeinde';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import ICacheObject = angular.ICacheObject;

export class EinstellungRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(public readonly http: IHttpService,
                       REST_API: string,
                       public readonly ebeguRestUtil: EbeguRestUtil) {
        this.serviceURL = REST_API + 'einstellung';
    }

    public saveEinstellung(tsEinstellung: TSEinstellung): IPromise<TSEinstellung> {
        let restEinstellung = {};
        restEinstellung = this.ebeguRestUtil.einstellungToRestObject(restEinstellung, tsEinstellung);
        return this.http.put(this.serviceURL, restEinstellung, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((response: any) => {
                return this.ebeguRestUtil.parseEinstellung(new TSEinstellung(), response.data);
            }
        );
    }

    public findEinstellung(key: TSEinstellungKey, gemeinde: TSGemeinde, gesuchsperiode: TSGesuchsperiode): IPromise<TSEinstellung> {
        return this.http.get(this.serviceURL + '/key/' + key + '/gemeinde/' + gemeinde.id + '/gp/' + gesuchsperiode.id)
            .then((param: IHttpResponse<TSEinstellung>) => {
                return param.data;
            });
    }

    public getAllEinstellungenBySystem(gesuchsperiodeId: string): IPromise<TSEinstellung[]> {
        return this.http.get(this.serviceURL + '/gesuchsperiode/' + gesuchsperiodeId)
            .then((response: any) => {
                return this.ebeguRestUtil.parseEinstellungList(response.data);
            });
    }

    public getAllEinstellungenBySystemCached(gesuchsperiodeId: string, cache: ICacheObject): IPromise<TSEinstellung[]> {
        return this.http.get(this.serviceURL + '/gesuchsperiode/' + gesuchsperiodeId, {cache})
            .then((response: any) => {
                return this.ebeguRestUtil.parseEinstellungList(response.data);
            });
    }
}
