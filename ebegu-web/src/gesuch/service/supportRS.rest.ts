/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import {TSSupportAnfrage} from '../../models/TSSupportAnfrage';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

export class SupportRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService
    ) {
        this.serviceURL = `${REST_API  }support`;
    }

    public sendSupportAnfrage(supportAnfrage: TSSupportAnfrage): IPromise<any> {
        let anfrageRestObject = {};
        anfrageRestObject = this.ebeguRestUtil.supportAnfrageToRestObject(anfrageRestObject, supportAnfrage);
        // Damit wir Sentryangaben zu diesem Fall mappen koennen
        this.$log.warn(`Supportanfrage erstellt mit ID ${  supportAnfrage.id}`);
        return this.$http.put(this.serviceURL, anfrageRestObject, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }

    public getServiceName(): string {
        return 'SupportRS';
    }
}
