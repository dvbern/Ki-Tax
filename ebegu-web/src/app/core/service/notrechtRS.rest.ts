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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IHttpPromise, IHttpService, ILogService, IPromise} from 'angular';
import {TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungDokument} from '../../../models/TSRueckforderungDokument';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class NotrechtRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}notrecht`;

    }

    public initializeRueckforderungFormulare(): IPromise<TSRueckforderungFormular[]> {
        return this.$http.post(`${this.serviceURL}/initialize`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungFormularList(response.data);
            });
    }

    public getRueckforderungFormulareForCurrentBenutzer(): IPromise<TSRueckforderungFormular[]> {
        return this.$http.get(`${this.serviceURL}/currentuser`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungFormularList(response.data);
            });
    }

    public currentUserHasFormular(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/currentuser/hasformular`, {}).then(response => {
            return response.data as boolean;
        });
    }

    public getServiceName(): string {
        return 'NotrechtRS';
    }

    public findRueckforderungFormular(rueckforderungFormularID: string): IPromise<TSRueckforderungFormular> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(rueckforderungFormularID)}`)
            .then((response: any) => {
                this.$log.debug('PARSING RueckforderungFormular REST object ', response.data);
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            });
    }

    public saveRueckforderungFormular(
        rueckforderungFormular: TSRueckforderungFormular,
        changeStatusIfNecessary: boolean
    ): IPromise<TSRueckforderungFormular> {
        let restRueckforderungFormular = {};
        restRueckforderungFormular =
            this.ebeguRestUtil.rueckforderungFormularToRestObject(restRueckforderungFormular, rueckforderungFormular);
        const url = changeStatusIfNecessary ? `${this.serviceURL}/updateWithStatusChange` : `${this.serviceURL}/update`;
        return this.$http.put(url, restRueckforderungFormular).then((response: any) => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            },
        );
    }

    public sendMitteilung(
        mitteilung: TSRueckforderungMitteilung,
        statusToSendMitteilung: TSRueckforderungStatus[]
    ): IPromise<void> {
        let restRueckforderungMitteilung = {};
        restRueckforderungMitteilung =
            this.ebeguRestUtil.rueckforderungMitteilungToRestObject(restRueckforderungMitteilung, mitteilung);
        const data = {mitteilung: restRueckforderungMitteilung, statusList: statusToSendMitteilung};
        return this.$http.post(`${this.serviceURL}/mitteilung`, data)
            .then(() => {
                    return;
                },
            );
    }

    public sendEinladung(
        mitteilung: TSRueckforderungMitteilung
    ): IPromise<void> {
        let restRueckforderungMitteilung = {};
        restRueckforderungMitteilung =
            this.ebeguRestUtil.rueckforderungMitteilungToRestObject(restRueckforderungMitteilung, mitteilung);

        return this.$http.post(`${this.serviceURL}/einladung`, restRueckforderungMitteilung)
            .then(() => {
                    return;
                },
            );
    }

    public initializePhase2(): IHttpPromise<any> {
        return this.$http.post(`${this.serviceURL}/initializePhase2`, {});
    }

    public resetStatus(
        rueckforderungFormular: TSRueckforderungFormular
    ): IPromise<TSRueckforderungFormular> {
        const data = `${encodeURIComponent(rueckforderungFormular.id)}`;
        return this.$http.post(`${this.serviceURL}/resetStatus`, data).then((response: any) => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            },
        );
    }

    public formularZurueckholen(
        rueckforderungFormular: TSRueckforderungFormular
    ): IPromise<TSRueckforderungFormular> {
        const data = `${encodeURIComponent(rueckforderungFormular.id)}`;
        return this.$http.post(`${this.serviceURL}/zurueckholen`, data).then((response: any) => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            },
        );
    }

    public getRueckforderungDokumente(rueckforderungFormularID: string): IPromise<TSRueckforderungDokument[]> {
        return this.$http.get(`${this.serviceURL}/dokumente/${encodeURIComponent(rueckforderungFormularID)}`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungDokumente(response.data);
            });
    }

    public deleteRueckforderungDokument(rueckforderungDokumentId: string): IHttpPromise<any> {
        const url = `${this.serviceURL}/${encodeURIComponent(rueckforderungDokumentId)}`;
        return this.$http.delete(url).then((response: any) => {
            return response.data;
        });
    }

    public saveRueckforderungFormularEinreicheFrist(
        rueckforderungFormular: TSRueckforderungFormular
    ): IPromise<TSRueckforderungFormular> {
        return this.$http.get(`${this.serviceURL}/einreicheFrist`,
            {
                params: {
                    rueckforderungFormularId: rueckforderungFormular.id,
                    extendedEinreichefrist: DateUtil.momentToLocalDate(rueckforderungFormular.extendedEinreichefrist),
                },
            }).then((response: any) => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            },
        );
    }

    public verfuegtProvisorischRueckforderungFormular(
        rueckforderungFormular: TSRueckforderungFormular
    ): IPromise<TSRueckforderungFormular> {
        let restRueckforderungFormular = {};
        restRueckforderungFormular =
            this.ebeguRestUtil.rueckforderungFormularToRestObject(restRueckforderungFormular, rueckforderungFormular);
        const url = `${this.serviceURL}/provisorischVerfuegen`;
        return this.$http.put(url, restRueckforderungFormular).then((response: any) => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            },
        );
    }

    public setVerantwortlicher(formularId: string, username: string): IPromise<TSRueckforderungFormular> {
        return this.$http.put(`${this.serviceURL}/verantwortlicher/${encodeURIComponent(formularId)}/${encodeURIComponent(username)}`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            });
    }

    public setDokumenteGeprueft(formularId: string): IPromise<TSRueckforderungFormular> {
        return this.$http.put(`${this.serviceURL}/dokumentegeprueft/${encodeURIComponent(formularId)}`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            });
    }
}
