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

import {IHttpPromise, IHttpService} from 'angular';
import * as moment from 'moment';
import {TSGemeindeAntragTyp} from '../../models/enums/TSGemeindeAntragTyp';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {DateUtil} from '../../utils/DateUtil';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import IPromise = angular.IPromise;

export class TestFaelleRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public readonly ebeguRestUtil: EbeguRestUtil,
    ) {
        this.serviceURL = `${REST_API}testfaelle`;
    }

    public getServiceName(): string {
        return 'TestFaelleRS';
    }

    public createTestFallGS(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
        username: string,
    ): IHttpPromise<string> {
        // TODO that is a strange API path. Configuration does not belong in a hierarchy. Use POST and move the
        // parameter to the method body
        // tslint:disable-next-line:max-line-length
        const url = `${this.serviceURL}/testfallgs/${encodeURIComponent(testFall)}/${gesuchsperiodeId}/${gemeindeId}/${bestaetigt}/${verfuegen}/${encodeURIComponent(
            username)}`;
        return this.http.get(url);
    }

    public removeFaelleOfGS(username: string): IHttpPromise<string> {
        return this.http.delete(`${this.serviceURL}/testfallgs/${encodeURIComponent(username)}`);
    }

    public createTestFall(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
    ): IHttpPromise<string> {
        // tslint:disable-next-line:max-line-length
        const url = `${this.serviceURL}/testfall/${encodeURIComponent(testFall)}/${gesuchsperiodeId}/${gemeindeId}/${bestaetigt}/${verfuegen}`;

        return this.http.get(url);
    }

    public mutiereFallHeirat(
        dossierid: string,
        gesuchsperiodeid: string,
        mutationsdatum: moment.Moment,
        aenderungper: moment.Moment,
    ): IHttpPromise<string> {
        return this.http.get(`${this.serviceURL}/mutationHeirat/${dossierid}/${encodeURIComponent(gesuchsperiodeid)}`, {
            params: {
                mutationsdatum: DateUtil.momentToLocalDate(mutationsdatum),
                aenderungper: DateUtil.momentToLocalDate(aenderungper),
            },
        });
    }

    public testAllMails(mailadresse: string): IHttpPromise<void> {
        return this.http.get(`${this.serviceURL}/mailtest/${mailadresse}`);
    }

    public mutiereFallScheidung(
        dossierid: string,
        gesuchsperiodeid: string,
        mutationsdatum: moment.Moment,
        aenderungper: moment.Moment,
    ): IHttpPromise<string> {
        const url = `${this.serviceURL}/mutationScheidung/${dossierid}/${encodeURIComponent(gesuchsperiodeid)}`;
        return this.http.get(url,
            {
                params: {
                    mutationsdatum: DateUtil.momentToLocalDate(mutationsdatum),
                    aenderungper: DateUtil.momentToLocalDate(aenderungper),
                },
            });
    }

    public resetSchulungsdaten(): IHttpPromise<string> {
        return this.http.get(`${this.serviceURL}/schulung/reset`);
    }

    public createSchulungsdaten(): IHttpPromise<string> {
        return this.http.get(`${this.serviceURL}/schulung/create`);
    }

    public deleteSchulungsdaten(): IHttpPromise<string> {
        return this.http.delete(`${this.serviceURL}/schulung/delete`);
    }

    public createTutorialdaten(): IHttpPromise<string> {
        return this.http.get(`${this.serviceURL}/schulung/tutorial/create`);
    }

    public getSchulungBenutzer(): IPromise<string[]> {
        return this.http.get(`${this.serviceURL}/schulung/public/user`).then((response: any) => {
            return response.data;
        });
    }

    public processScript(scriptNr: string): IHttpPromise<any> {
        return this.http.get(`${this.serviceURL}/processscript/${scriptNr}`);
    }

    public createGemeindeAntragTestDaten(
        antragTyp: TSGemeindeAntragTyp,
        gesuchsperiode: TSGesuchsperiode,
        gemeinde: TSGemeinde,
        status: string,
    ): IPromise<string> {
        return this.http.post<string>(`${this.serviceURL}/gemeinde-antraege/${antragTyp}`,
            {
                gesuchsperiode: this.ebeguRestUtil.gesuchsperiodeToRestObject({}, gesuchsperiode),
                gemeinde: this.ebeguRestUtil.gemeindeToRestObject({}, gemeinde),
                status,
            })
            .then(response => response.data);
    }
}
