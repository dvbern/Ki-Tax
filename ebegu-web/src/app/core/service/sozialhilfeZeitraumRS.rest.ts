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
import {TSSozialhilfeZeitraumContainer} from '../../../models/TSSozialhilfeZeitraumContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class SozialhilfeZeitraumRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];

    public serviceURL: string;

    public constructor(
        public readonly http: IHttpService,
        REST_API: string,
        public readonly ebeguRestUtil: EbeguRestUtil,
        public readonly log: ILogService
    ) {
        this.serviceURL = `${REST_API}sozialhilfeZeitraeume`;
    }

    public getServiceName(): string {
        return 'SozialhilfeZeitraumRS';
    }

    public saveSozialhilfeZeitraum(
        sozialhilfeZeitraumContainer: TSSozialhilfeZeitraumContainer,
        famSitID: string
    ): IPromise<TSSozialhilfeZeitraumContainer> {
        let restSozialhilfeZaitraum = {};
        restSozialhilfeZaitraum =
            this.ebeguRestUtil.sozialhilfeZeitraumContainerToRestObject(
                restSozialhilfeZaitraum,
                sozialhilfeZeitraumContainer
            );
        const url = `${this.serviceURL}/${encodeURIComponent(famSitID)}`;
        return this.http
            .put(url, restSozialhilfeZaitraum)
            .then((response: any) => {
                this.log.debug(
                    'PARSING SozialhilfeZeitraumContainer REST object ',
                    response.data
                );
                return this.ebeguRestUtil.parseSozialhilfeZeitraumContainer(
                    new TSSozialhilfeZeitraumContainer(),
                    response.data
                );
            });
    }

    public removeSozialhilfeZeitraum(
        sozialhilfeZeitraumContainerID: string
    ): IPromise<any> {
        const url = `${this.serviceURL}/sozialhilfeZeitraumId/${encodeURIComponent(sozialhilfeZeitraumContainerID)}`;
        return this.http.delete(url).then(response => response);
    }
}
