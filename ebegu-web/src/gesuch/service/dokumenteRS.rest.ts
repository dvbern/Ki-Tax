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

import {IHttpService} from 'angular';
import {TSDokumenteDTO} from '../../models/dto/TSDokumenteDTO';
import {TSDokumentGrundTyp} from '../../models/enums/TSDokumentGrundTyp';
import {TSDokument} from '../../models/TSDokument';
import {TSDokumentGrund} from '../../models/TSDokumentGrund';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import ICacheObject = angular.ICacheObject;
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;

export class DokumenteRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService
    ) {
        this.serviceURL = `${REST_API}dokumente`;
    }

    public getDokumente(gesuch: TSGesuch): IPromise<TSDokumenteDTO> {
        return this.http
            .get(`${this.serviceURL}/${encodeURIComponent(gesuch.id)}`)
            .then((response: any) => {
                this.log.debug(
                    'PARSING dokumentDTO REST object ',
                    response.data
                );
                return this.ebeguRestUtil.parseDokumenteDTO(
                    new TSDokumenteDTO(),
                    response.data
                );
            });
    }

    public getDokumenteByTypeCached(
        gesuch: TSGesuch,
        dokumentGrundTyp: TSDokumentGrundTyp,
        cache: ICacheObject
    ): IPromise<TSDokumenteDTO> {
        const grund = encodeURIComponent(TSDokumentGrundTyp[dokumentGrundTyp]);
        const url = `${this.serviceURL}/byTyp/${encodeURIComponent(gesuch.id)}/${grund}`;

        return this.http.get(url, {cache}).then((response: any) => {
            this.log.debug(
                'PARSING cached dokumentDTO REST object ',
                response.data
            );
            return this.ebeguRestUtil.parseDokumenteDTO(
                new TSDokumenteDTO(),
                response.data
            );
        });
    }

    public removeDokument(dokument: TSDokument): IPromise<TSDokumentGrund> {
        const url = `${this.serviceURL}/${encodeURIComponent(dokument.id)}`;
        return this.http.delete(url).then((response: any) => {
            this.log.debug('PARSING dokumentGrund REST object ', response.data);
            return this.ebeguRestUtil.parseDokumentGrund(
                new TSDokumentGrund(),
                response.data
            );
        });
    }
}
