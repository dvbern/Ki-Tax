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
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSBerechtigungHistory} from '../../../models/TSBerechtigungHistory';
import {TSUserSearchresultDTO} from '../../../models/TSUserSearchresultDTO';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {IEntityRS} from './iEntityRS.rest';

export class BenutzerRS implements IEntityRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}benutzer`;
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerBgOrGemeindeForGemeinde(gemeindeId: string): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/BgOrGemeinde/${encodeURIComponent(gemeindeId)}`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerTsOrGemeindeForGemeinde(gemeindeId: string): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/TsOrGemeinde/${encodeURIComponent(gemeindeId)}`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerTsBgOrGemeindeForGemeinde(gemeindeId: string): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/TsBgOrGemeinde/${encodeURIComponent(gemeindeId)}`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerBgOrGemeinde(): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/BgOrGemeinde/all`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerTsOrGemeinde(): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/TsOrGemeinde/all`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG, Admin_BG, Sachbearbeiter_TS, Admin_TS
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerBgTsOrGemeinde(): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/BgTsOrGemeinde/all`);
    }

    public getAllGesuchsteller(): IPromise<TSBenutzerNoDetails[]> {
        return this.getBenutzer(`${this.serviceURL}/gesuchsteller`);
    }

    private getBenutzer(url: string): IPromise<TSBenutzerNoDetails[]> {
        return this.$http.get(url).then((response: any) => {
            return this.ebeguRestUtil.parseUserNoDetailsList(response.data);
        });
    }

    private getSingleBenutzer(url: string): IPromise<TSBenutzer> {
        return this.$http.get(url)
            .then((response: any) => {
                this.$log.debug('PARSING benutzer REST object ', response.data);
                return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
            });
    }

    public searchUsers(userSearch: any): IPromise<TSUserSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/search/`, userSearch).then((response: any) => {
            this.$log.debug('PARSING benutzer REST array object', response.data);
            const tsBenutzers = this.ebeguRestUtil.parseUserList(response.data.benutzerDTOs);

            return new TSUserSearchresultDTO(tsBenutzers, response.data.paginationDTO.totalItemCount);
        });
    }

    public findBenutzer(username: string): IPromise<TSBenutzer> {
        return this.getSingleBenutzer(`${this.serviceURL}/username/${encodeURIComponent(username)}`);
    }

    public inactivateBenutzer(user: TSBenutzer): IPromise<TSBenutzer> {
        const userRest = this.ebeguRestUtil.userToRestObject({}, user);
        return this.$http.put(`${this.serviceURL}/inactivate/`, userRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public reactivateBenutzer(benutzer: TSBenutzer): IPromise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.put(`${this.serviceURL}/reactivate/`, benutzerRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public einladen(benutzer: TSBenutzer): IPromise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.post(`${this.serviceURL}/einladen/`, benutzerRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public erneutEinladen(benutzer: TSBenutzer): IHttpPromise<any> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.post(`${this.serviceURL}/erneutEinladen/`, benutzerRest);
    }

    public saveBenutzerBerechtigungen(benutzer: TSBenutzer): IPromise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.put(`${this.serviceURL}/saveBenutzerBerechtigungen/`, benutzerRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public getBerechtigungHistoriesForBenutzer(username: string): IPromise<TSBerechtigungHistory[]> {
        return this.$http.get(`${this.serviceURL}/berechtigunghistory/${encodeURIComponent(username)}`)
            .then((response: any) => {
                this.$log.debug('PARSING benutzer REST object ', response.data);
                return this.ebeguRestUtil.parseBerechtigungHistoryList(response.data);
            });
    }

    public isBenutzerDefaultBenutzerOfAnyGemeinde(username: string): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/isdefaultuser/${encodeURIComponent(username)}`)
            .then((response: any) => {
                return JSON.parse(response.data);
            });
    }

    public removeBenutzer(username: string): IPromise<boolean> {
        return this.$http.delete(`${this.serviceURL}/delete/${encodeURIComponent(username)}`)
            .then((response: any) => {
                return response.data;
            });
    }
}
