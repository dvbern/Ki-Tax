/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import {TSSocialhilfeZeitraumContainer} from '../../../models/TSSocialhilfeZeitraumContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class SocialhilfeZeitraumRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];

    public serviceURL: string;

    public constructor(
        public readonly http: IHttpService,
        REST_API: string,
        public readonly ebeguRestUtil: EbeguRestUtil,
        public readonly log: ILogService,
    ) {
        this.serviceURL = `${REST_API}socialhilfeZeitraeume`;
    }

    public getServiceName(): string {
        return 'SocialhilfeZeitraumRS';
    }

    public saveSocialhilfeZeitraum(socialhilfeZeitraumContainer: TSSocialhilfeZeitraumContainer, famSitID: string,
    ): IPromise<TSSocialhilfeZeitraumContainer> {
        let restSocialhilfeZaitraum = {};
        restSocialhilfeZaitraum =
            this.ebeguRestUtil.socialhilfeZeitraumContainerToRestObject(restSocialhilfeZaitraum, socialhilfeZeitraumContainer);
        const url = `${this.serviceURL}/${encodeURIComponent(famSitID)}`;
        return this.http.put(url, restSocialhilfeZaitraum).then((response: any) => {
            this.log.debug('PARSING SocialhilfeZeitraumContainer REST object ', response.data);
            return this.ebeguRestUtil.parseSocialhilfeZeitraumContainer(new TSSocialhilfeZeitraumContainer(), response.data);
        });
    }

    public removeSocialhilfeZeitraum(socialhilfeZeitraumContainerID: string): IPromise<any> {
        const url = `${this.serviceURL}/socialhilfeZeitraumId/${encodeURIComponent(socialhilfeZeitraumContainerID)}`;
        return this.http.delete(url)
            .then(response => {
                return response;
            });
    }
}
