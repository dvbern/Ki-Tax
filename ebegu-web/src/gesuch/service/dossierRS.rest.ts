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

import {IHttpPromise, IHttpService, IPromise} from 'angular';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import {TSDossier} from '../../models/TSDossier';
import {TSInstitution} from '../../models/TSInstitution';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

export class DossierRS implements IEntityRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(public $http: IHttpService, REST_API: string, public ebeguRestUtil: EbeguRestUtil) {
        this.serviceURL = `${REST_API}dossier`;
    }

    public createDossier(dossier: TSDossier): IPromise<TSDossier> {
        let sentDossier = {};
        sentDossier = this.ebeguRestUtil.dossierToRestObject(sentDossier, dossier);
        return this.$http.post(this.serviceURL, sentDossier).then((response: any) => {
            return this.ebeguRestUtil.parseDossier(new TSDossier(), response.data);
        });
    }

    public findDossier(dossierId: string): IPromise<TSDossier> {
        return this.$http.get(`${this.serviceURL}/id/${encodeURIComponent(dossierId)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDossier(new TSDossier(), response.data);
            });
    }

    public findDossiersByFall(fallId: string): IPromise<TSDossier[]> {
        return this.$http.get(`${this.serviceURL}/fall/${encodeURIComponent(fallId)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDossierList(response.data);
            });
    }

    public findNewestDossierByCurrentBenutzerAsBesitzer(): IPromise<TSDossier> {
        return this.$http.get(`${this.serviceURL}/newestCurrentBesitzer/`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseDossier(new TSDossier(), response.data);
            });
    }

    public getOrCreateDossierAndFallForCurrentUserAsBesitzer(gemeindeId: string): IPromise<TSDossier> {
        return this.$http.put(`${this.serviceURL}/createforcurrentbenutzer/${encodeURIComponent(gemeindeId)}`, {})
            .then((response: any) => {
                return this.ebeguRestUtil.parseDossier(new TSDossier(), response.data);
            });
    }

    public setVerantwortlicherBG(dossierId: string, username: string): IHttpPromise<TSDossier> {
        return this.$http.put(`${this.serviceURL}/verantwortlicherBG/${encodeURIComponent(dossierId)}`, username);
    }

    public setVerantwortlicherTS(dossierId: string, username: string): IHttpPromise<TSDossier> {
        return this.$http.put(`${this.serviceURL}/verantwortlicherTS/${encodeURIComponent(dossierId)}`, username);
    }

    public findAllInstitutionen(dossierId: string): IPromise<Array<TSInstitution>> {
        return this.$http.get(`${this.serviceURL}/findAllInstitutionen/${encodeURIComponent(dossierId)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseInstitutionen(response.data);
            });
    }
}
