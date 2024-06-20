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
import {TSLand} from '../../../models/types/TSLand';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class ListResourceRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];

    public static laenderList: TSLand[];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil
    ) {
        this.serviceURL = `${REST_API}lists`;
        ListResourceRS.laenderList = [];
    }

    public getLaenderList(): IPromise<TSLand[]> {
        return this.http
            .get(`${this.serviceURL}/laender`, {cache: true})
            .then((response: any) => {
                if (
                    ListResourceRS.laenderList.length <= 0 &&
                    Array.isArray(response.data)
                ) {
                    response.data
                        .map((d: string) =>
                            this.ebeguRestUtil.landCodeToTSLand(d)
                        )
                        .forEach((land: TSLand) =>
                            ListResourceRS.laenderList.push(land)
                        );
                }

                // wenn die Laenderliste schon ausgefuellt wurde, nichts machen
                return ListResourceRS.laenderList;
            });
    }
}
