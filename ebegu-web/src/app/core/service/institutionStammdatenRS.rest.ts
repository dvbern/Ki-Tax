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

import {IHttpService, ILogService, IPromise} from 'angular';
import {GlobalCacheService} from '../../../gesuch/service/globalCacheService';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class InstitutionStammdatenRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'GlobalCacheService'];

    public serviceURL: string;

    public constructor(public $http: IHttpService,
                       REST_API: string,
                       public ebeguRestUtil: EbeguRestUtil,
                       public $log: ILogService, private readonly globalCacheService: GlobalCacheService,
    ) {
        this.serviceURL = `${REST_API}institutionstammdaten`;
    }

    public findInstitutionStammdaten(institutionStammdatenID: string): IPromise<TSInstitutionStammdaten> {
        return this.$http.get(`${this.serviceURL}/id/${encodeURIComponent(institutionStammdatenID)}`)
            .then((response: any) => {
                this.$log.debug('PARSING InstitutionStammdaten REST object ', response.data);
                return this.ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(), response.data);
            });
    }

    public createInstitutionStammdaten(institutionStammdaten: TSInstitutionStammdaten): IPromise<TSInstitutionStammdaten> {
        return this.saveInstitutionStammdaten(institutionStammdaten);
    }

    public updateInstitutionStammdaten(institutionStammdaten: TSInstitutionStammdaten): IPromise<TSInstitutionStammdaten> {
        return this.saveInstitutionStammdaten(institutionStammdaten);
    }

    public saveInstitutionStammdaten(institutionStammdaten: TSInstitutionStammdaten): IPromise<TSInstitutionStammdaten> {
        let restInstitutionStammdaten = {};
        restInstitutionStammdaten =
            this.ebeguRestUtil.institutionStammdatenToRestObject(restInstitutionStammdaten, institutionStammdaten);

        return this.$http.put(this.serviceURL, restInstitutionStammdaten).then((response: any) => {
                return this.ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(), response.data);
            },
        );
    }

    public getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(gesuchsperiodeId: string,
                                                                        gemeindeId: string): IPromise<TSInstitutionStammdaten[]> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_INSTITUTIONSSTAMMDATEN_GEMEINDE);
        return this.$http.get(`${this.serviceURL}/gesuchsperiode/gemeinde/active`, {
            params: {
                gesuchsperiodeId,
                gemeindeId,
            },
            cache
        })
            .then((response: any) => {
                return this.ebeguRestUtil.parseInstitutionStammdatenArray(response.data);
            });
    }

    public fetchInstitutionStammdatenByInstitution(institutionID: string): IPromise<TSInstitutionStammdaten> {
        return this.$http.get(`${this.serviceURL}/institutionornull/${encodeURIComponent(institutionID)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(), response.data);
            });
    }

    public getBetreuungsangeboteForInstitutionenOfCurrentBenutzer(): IPromise<TSBetreuungsangebotTyp[]> {
        return this.$http.get(`${this.serviceURL}/currentuser`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getServiceName(): string {
        return 'InstitutionStammdatenRS';
    }

    public getAllTagesschulenForCurrentBenutzer(): IPromise<TSInstitutionStammdaten[]> {
        return this.$http.get(`${this.serviceURL}/tagesschulen/currentuser`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseInstitutionStammdatenArray(response.data);
            });
    }
}
