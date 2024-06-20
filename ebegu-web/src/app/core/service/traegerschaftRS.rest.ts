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
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class TraegerschaftRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService
    ) {
        this.serviceURL = `${REST_API}traegerschaften`;
    }

    public findTraegerschaft(
        traegerschaftID: string
    ): IPromise<TSTraegerschaft> {
        return this.http
            .get(`${this.serviceURL}/id/${encodeURIComponent(traegerschaftID)}`)
            .then((response: any) => {
                this.log.debug(
                    'PARSING traegerschaft REST object ',
                    response.data
                );
                return this.ebeguRestUtil.parseTraegerschaft(
                    new TSTraegerschaft(),
                    response.data
                );
            });
    }

    /**
     * It sends all required parameters (new Traegerschaft and User) to the server so the server can create
     * all required objects within a single transaction.
     */
    public createTraegerschaft(
        traegerschaft: TSTraegerschaft,
        email: string
    ): IPromise<TSTraegerschaft> {
        let restTraegerschaft = {};
        restTraegerschaft = this.ebeguRestUtil.traegerschaftToRestObject(
            restTraegerschaft,
            traegerschaft
        );
        return this.http
            .post(this.serviceURL, restTraegerschaft, {
                params: {
                    adminMail: email
                }
            })
            .then(response =>
                this.ebeguRestUtil.parseTraegerschaft(
                    new TSTraegerschaft(),
                    response.data
                )
            );
    }

    public saveTraegerschaft(
        traegerschaft: TSTraegerschaft
    ): IPromise<TSTraegerschaft> {
        let restTraegerschaft = {};
        restTraegerschaft = this.ebeguRestUtil.traegerschaftToRestObject(
            restTraegerschaft,
            traegerschaft
        );
        return this.http
            .put(this.serviceURL, restTraegerschaft)
            .then(response => {
                this.log.debug(
                    'PARSING traegerschaft REST object ',
                    response.data
                );
                return this.ebeguRestUtil.parseTraegerschaft(
                    new TSTraegerschaft(),
                    response.data
                );
            });
    }

    public removeTraegerschaft(
        traegerschaftID: string
    ): IHttpPromise<TSTraegerschaft> {
        return this.http.delete(
            `${this.serviceURL}/${encodeURIComponent(traegerschaftID)}`
        );
    }

    public getAllTraegerschaften(): IPromise<TSTraegerschaft[]> {
        return this.http.get<any[]>(this.serviceURL).then(response => {
            this.log.debug(
                'PARSING traegerschaften REST array object',
                response.data
            );
            return this.ebeguRestUtil.parseTraegerschaften(response.data);
        });
    }

    public getAllActiveTraegerschaften(): IPromise<TSTraegerschaft[]> {
        return this.http
            .get<any[]>(`${this.serviceURL}/active`)
            .then(response => {
                this.log.debug(
                    'PARSING traegerschaften REST array object',
                    response.data
                );
                return this.ebeguRestUtil.parseTraegerschaften(response.data);
            });
    }

    public getServiceName(): string {
        return 'TraegerschaftRS';
    }
}
