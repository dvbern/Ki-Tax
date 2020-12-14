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

import {IHttpPromise, IHttpService, ILogService, IPromise, IQService} from 'angular';
import {DossierRS} from '../../../gesuch/service/dossierRS.rest';
import {GlobalCacheService} from '../../../gesuch/service/globalCacheService';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSSprache} from '../../../models/enums/TSSprache';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class GesuchsperiodeRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', '$q', 'GlobalCacheService', 'DossierRS'];
    public serviceURL: string;

    private activeGesuchsperiodenList: Array<TSGesuchsperiode>;
    private nichtAbgeschlosseneGesuchsperiodenList: Array<TSGesuchsperiode>;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public log: ILogService,
        private readonly $q: IQService,
        private readonly globalCacheService: GlobalCacheService,
        private readonly dossierRS: DossierRS
    ) {
        this.serviceURL = `${REST_API}gesuchsperioden`;
    }

    public getServiceName(): string {
        return 'GesuchsperiodeRS';
    }

    public findGesuchsperiode(gesuchsperiodeID: string): IPromise<TSGesuchsperiode> {
        return this.http.get(`${this.serviceURL}/gesuchsperiode/${encodeURIComponent(gesuchsperiodeID)}`)
            .then(response => this.ebeguRestUtil.parseGesuchsperiode(new TSGesuchsperiode(), response.data));
    }

    public saveGesuchsperiode(gesuchsperiode: TSGesuchsperiode): IPromise<TSGesuchsperiode> {
        let restGesuchsperiode = {};
        restGesuchsperiode = this.ebeguRestUtil.gesuchsperiodeToRestObject(restGesuchsperiode, gesuchsperiode);
        return this.http.put(this.serviceURL, restGesuchsperiode).then((response: any) => {
            this.log.debug('PARSING Gesuchsperiode REST object ', response.data);
            return this.ebeguRestUtil.parseGesuchsperiode(new TSGesuchsperiode(), response.data);
        });
    }

    public removeGesuchsperiode(gesuchsperiodeId: string): IHttpPromise<TSGesuchsperiode> {
        return this.http.delete(`${this.serviceURL}/${encodeURIComponent(gesuchsperiodeId)}`);
    }

    public updateActiveGesuchsperiodenList(): IPromise<TSGesuchsperiode[]> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GESUCHSPERIODEN_ACTIVE);
        return this.http.get(`${this.serviceURL}/active`, {cache}).then(response => {
            const gesuchsperioden = this.ebeguRestUtil.parseGesuchsperioden(response.data);
            this.activeGesuchsperiodenList = angular.copy(gesuchsperioden);
            return this.activeGesuchsperiodenList;
        });
    }

    public getAllActiveGesuchsperioden(): IPromise<TSGesuchsperiode[]> {
        if (!this.activeGesuchsperiodenList || this.activeGesuchsperiodenList.length <= 0) { // if the list is empty, reload it
            return this.updateActiveGesuchsperiodenList().then(() => {
                return this.activeGesuchsperiodenList;
            });
        }
        return this.$q.when(this.activeGesuchsperiodenList); // we need to return a promise
    }

    public getActiveGesuchsperiodenForDossier(dossierId: string): IPromise<TSGesuchsperiode[]> {
        return this.dossierRS.findDossier(dossierId)
            .then(dossier => {
                return this.getAllPeriodenForGemeinde(dossier.gemeinde.id);
            });
    }

    public getAktivePeriodenForGemeinde(gemeindeId: string, dossierId?: string): IPromise<TSGesuchsperiode[]> {
        return this.http
            .get(`${this.serviceURL}/aktive/gemeinde/${gemeindeId}`, {
                params: {
                    dossierId,
                },
            })
            .then(response => {
                return this.ebeguRestUtil.parseGesuchsperioden(response.data);
            });
    }

    public getAllPeriodenForGemeinde(gemeindeId: string, dossierId?: string): IPromise<TSGesuchsperiode[]> {
        return this.http
            .get(`${this.serviceURL}/gemeinde/${gemeindeId}`, {
                params: {
                    dossierId,
                },
            })
            .then(response => {
                return this.ebeguRestUtil.parseGesuchsperioden(response.data);
            });
    }

    public getAllGesuchsperioden(): IPromise<TSGesuchsperiode[]> {
        return this.http.get(`${this.serviceURL}/`).then((response: any) => {
            return this.ebeguRestUtil.parseGesuchsperioden(response.data);
        });
    }

    public updateNichtAbgeschlosseneGesuchsperiodenList(): IPromise<TSGesuchsperiode[]> {
        return this.http.get(`${this.serviceURL}/unclosed`).then((response: any) => {
            const gesuchsperioden = this.ebeguRestUtil.parseGesuchsperioden(response.data);
            this.nichtAbgeschlosseneGesuchsperiodenList = angular.copy(gesuchsperioden);
            return this.nichtAbgeschlosseneGesuchsperiodenList;
        });
    }

    public getAllAktivUndInaktivGesuchsperioden(): IPromise<TSGesuchsperiode[]> {
        if (!this.nichtAbgeschlosseneGesuchsperiodenList || this.nichtAbgeschlosseneGesuchsperiodenList.length <= 0) { // if the list is empty, reload it
            return this.updateNichtAbgeschlosseneGesuchsperiodenList().then(() => {
                return this.nichtAbgeschlosseneGesuchsperiodenList;
            });
        }
        return this.$q.when(this.nichtAbgeschlosseneGesuchsperiodenList); // we need to return a promise
    }

    public getNewestGesuchsperiode(): IPromise<TSGesuchsperiode> {
        return this.http.get(`${this.serviceURL}/newestGesuchsperiode/`)
            .then((response: any) => {
                this.log.debug('PARSING Gesuchsperiode REST object ', response.data);
                return this.ebeguRestUtil.parseGesuchsperiode(new TSGesuchsperiode(), response.data);
            });
    }

    public removeGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache,
                                        dokumentTyp: TSDokumentTyp): IHttpPromise<TSGesuchsperiode> {
        // tslint:disable-next-line:max-line-length
        return this.http.delete(`${this.serviceURL}/gesuchsperiodeDokument/${encodeURIComponent(gesuchsperiodeId)}/${sprache}/${dokumentTyp}`);
    }

    public existDokument(gesuchsperiodeId: string, sprache: TSSprache, dokumentTyp: TSDokumentTyp): IPromise<boolean> {
        return this.http.get(
            `${this.serviceURL}/existDokument/${encodeURIComponent(gesuchsperiodeId)}/${sprache}/${dokumentTyp}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public downloadGesuchsperiodeDokument(gesuchsperiodeId: string, sprache: TSSprache,
                                          dokumentTyp: TSDokumentTyp): IPromise<BlobPart> {
        // tslint:disable-next-line:max-line-length
        return this.http.get(`${this.serviceURL}/downloadGesuchsperiodeDokument/${encodeURIComponent(gesuchsperiodeId)}/${sprache}/${dokumentTyp}`,
            {responseType: 'blob'})
            .then((response: any) => {
                return response.data;
            });
    }
}
