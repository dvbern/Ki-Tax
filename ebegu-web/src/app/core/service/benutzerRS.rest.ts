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

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map} from 'rxjs/operators';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSBerechtigungHistory} from '../../../models/TSBerechtigungHistory';
import {TSUserSearchresultDTO} from '../../../models/TSUserSearchresultDTO';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';
import {LogFactory} from '../logging/LogFactory';

@Injectable({
    providedIn: 'root',
})
export class BenutzerRS {

    private readonly LOG = LogFactory.createLog(BenutzerRS.name);

    public readonly serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor(
        public $http: HttpClient,
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}benutzer`;
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerBgOrGemeindeForGemeinde(gemeindeId: string): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/BgOrGemeinde/${encodeURIComponent(gemeindeId)}`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerTsOrGemeindeForGemeinde(gemeindeId: string): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/TsOrGemeinde/${encodeURIComponent(gemeindeId)}`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerTsBgOrGemeindeForGemeinde(gemeindeId: string): Promise<TSBenutzer[]> {
        return this.getBenutzer(`${this.serviceURL}/TsBgOrGemeinde/${encodeURIComponent(gemeindeId)}`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerBgOrGemeinde(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/BgOrGemeinde/all`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerTsOrGemeinde(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/TsOrGemeinde/all`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG, Admin_BG, Sachbearbeiter_TS, Admin_TS
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerBgTsOrGemeinde(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/BgTsOrGemeinde/all`);
    }

    public getAllGesuchsteller(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/gesuchsteller`);
    }

    private getBenutzerNoDetail(url: string): Promise<TSBenutzerNoDetails[]> {
        return this.$http.get(url).pipe(map((response: any) => {
            return this.ebeguRestUtil.parseUserNoDetailsList(response);
        })).toPromise();
    }

    private getBenutzer(url: string): Promise<TSBenutzer[]> {
        return this.$http.get(url).pipe(map((response: any) => {
            this.LOG.debug('PARSING benutzer REST array object', response);
            return this.ebeguRestUtil.parseUserList(response);
        })).toPromise();
    }

    private getSingleBenutzer(url: string): Promise<TSBenutzer> {
        return this.$http.get(url)
            .pipe(map((response: any) => {
                this.LOG.debug('PARSING benutzer REST object ', response);
                return this.ebeguRestUtil.parseUser(new TSBenutzer(), response);
            })).toPromise();
    }

    public searchUsers(userSearch: any): Promise<TSUserSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/search/`, userSearch).pipe(map((response: any) => {
            this.LOG.debug('PARSING benutzer REST array object', response);
            const tsBenutzers = this.ebeguRestUtil.parseUserList(response.benutzerDTOs);

            return new TSUserSearchresultDTO(tsBenutzers, response.paginationDTO.totalItemCount);
        })).toPromise();
    }

    public findBenutzer(username: string): Promise<TSBenutzer> {
        return this.getSingleBenutzer(`${this.serviceURL}/username/${encodeURIComponent(username)}`);
    }

    public inactivateBenutzer(user: TSBenutzer): Promise<TSBenutzer> {
        const userRest = this.ebeguRestUtil.userToRestObject({}, user);
        return this.$http.put(`${this.serviceURL}/inactivate/`, userRest).pipe(map((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response);
        })).toPromise();
    }

    public reactivateBenutzer(benutzer: TSBenutzer): Promise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.put(`${this.serviceURL}/reactivate/`, benutzerRest).pipe(map((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response);
        })).toPromise();
    }

    public einladen(benutzer: TSBenutzer): Promise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.post(`${this.serviceURL}/einladen/`, benutzerRest).pipe(map((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response);
        })).toPromise();
    }

    public erneutEinladen(benutzer: TSBenutzer): Promise<any> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.post(`${this.serviceURL}/erneutEinladen/`, benutzerRest).toPromise();
    }

    public saveBenutzerBerechtigungen(benutzer: TSBenutzer): Promise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.put(`${this.serviceURL}/saveBenutzerBerechtigungen/`, benutzerRest)
            .pipe(map((response: any) => {
                return this.ebeguRestUtil.parseUser(new TSBenutzer(), response);
            })).toPromise();
    }

    public getBerechtigungHistoriesForBenutzer(username: string): Promise<TSBerechtigungHistory[]> {
        return this.$http.get(`${this.serviceURL}/berechtigunghistory/${encodeURIComponent(username)}`)
            .pipe(map((response: any) => {
                this.LOG.debug('PARSING benutzer REST object ', response);
                return this.ebeguRestUtil.parseBerechtigungHistoryList(response);
            })).toPromise();
    }

    public isBenutzerDefaultBenutzerOfAnyGemeinde(username: string): Promise<boolean> {
        return this.$http.get(`${this.serviceURL}/isdefaultuser/${encodeURIComponent(username)}`)
            .pipe(map((response: any) => {
                return JSON.parse(response);
            })).toPromise();
    }

    public removeBenutzer(username: string): Promise<boolean> {
        return this.$http.delete(`${this.serviceURL}/delete/${encodeURIComponent(username)}`)
            .pipe(map((response: any) => {
                return response;
            })).toPromise();
    }

    public deleteExternalUuidForBenutzer(user: TSBenutzer): Promise<any> {
        return this.$http.put(`${this.serviceURL}/reset/${user.username}`, {}).toPromise();
    }
}
