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

import {IHttpResponse, IHttpService, IPromise} from 'angular';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import {TSAntragDTO} from '../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../models/TSAntragSearchresultDTO';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

export class SearchRS implements IEntityRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
    ) {
        this.serviceURL = `${REST_API}search`;
    }

    public getServiceName(): string {
        return 'SearchRS';
    }

    public searchAntraege(antragSearch: any): IPromise<TSAntragSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/search/`, antragSearch)
            .then(response => this.toAntragSearchresult(response));
    }

    public countAntraege(antragSearch: any): IPromise<number> {
        return this.$http.post(`${this.serviceURL}/search/count`, antragSearch)
            .then((response: any) => {
                 return response.data;
            });
    }

    public getPendenzenList(antragSearch: any): IPromise<TSAntragSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/jugendamt/`, antragSearch)
            .then(response => this.toAntragSearchresult(response));
    }

    public countPendenzenList(antragSearch: any): IPromise<number> {
        return this.$http.post(`${this.serviceURL}/jugendamt/count`, antragSearch)
            .then((response: any) => {
                return response.data;
            });
    }

    private toAntragSearchresult(response: IHttpResponse<any>): TSAntragSearchresultDTO {
        const tsAntragDTOS = this.ebeguRestUtil.parseAntragDTOs(response.data.antragDTOs);

        return new TSAntragSearchresultDTO(tsAntragDTOS);
    }

    public getAntraegeOfDossier(dossierId: string): IPromise<Array<TSAntragDTO>> {
        return this.$http.get(`${this.serviceURL}/gesuchsteller/${encodeURIComponent(dossierId)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseAntragDTOs(response.data);
            });
    }
}
