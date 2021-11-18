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

import {IHttpService, ILogService, IPromise} from 'angular';
import {TSFachstelle} from '../../../models/TSFachstelle';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class FachstelleRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
    ) {
        this.serviceURL = `${REST_API}fachstellen`;
    }

    public getAnspruchFachstellen(gesuchsperiode: TSGesuchsperiode): IPromise<TSFachstelle[]> {
        return this.http.get(`${this.serviceURL}/anspruch?gesuchsperiodeId=${gesuchsperiode.id}`).then(
            (response: any) => this.ebeguRestUtil.parseFachstellen(response.data),
        );
    }

    public getErweiterteBetreuungFachstellen(gesuchsperiode: TSGesuchsperiode): IPromise<TSFachstelle[]> {
        return this.http.get(`${this.serviceURL}/erweiterteBetreuung?gesuchsperiodeId=${gesuchsperiode.id}`).then(
            (response: any) => this.ebeguRestUtil.parseFachstellen(response.data),
        );
    }

    public getServiceName(): string {
        return 'FachstelleRS';
    }
}
