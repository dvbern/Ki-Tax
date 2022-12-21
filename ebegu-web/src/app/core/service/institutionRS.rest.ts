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
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSInstitutionExternalClientAssignment} from '../../../models/TSInstitutionExternalClientAssignment';
import {TSInstitutionListDTO} from '../../../models/TSInstitutionListDTO';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSInstitutionUpdate} from '../../../models/TSInstitutionUpdate';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../constants/CONSTANTS';

@Injectable({
    providedIn: 'root'
})
export class InstitutionRS {

    public readonly serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor(
        public $http: HttpClient
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}institutionen`;
    }

    public findInstitution(institutionID: string): Observable<TSInstitution> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(institutionID)}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitution(new TSInstitution(), response)));
    }

    public updateInstitution(institutionID: string, update: TSInstitutionUpdate): Observable<TSInstitutionStammdaten> {
        const restInstitution = this.ebeguRestUtil.institutionUpdateToRestObject(update);

        return this.$http.put(`${this.serviceURL}/${encodeURIComponent(institutionID)}`, restInstitution)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(), response)));
    }

    /**
     * It sends all required parameters (new Institution, startDate, Betreuungsangebot and User) to the server so
     * the server can create all required objects within a single transaction.
     */
    public createInstitution(
        institution: TSInstitution,
        startDate: moment.Moment,
        betreuungsangebot: TSBetreuungsangebotTyp,
        adminMail: string,
        gemeindeId: string
    ): Observable<TSInstitution> {
        const params: any = {
            date: DateUtil.momentToLocalDate(startDate),
            betreuung: betreuungsangebot,
            adminMail
        };

        if (EbeguUtil.isNotNullOrUndefined(gemeindeId)) {
            params.gemeindeId = gemeindeId;
        }

        const restInstitution = this.ebeguRestUtil.institutionToRestObject({}, institution);
        return this.$http.post(this.serviceURL, restInstitution,
            {
                params
            })
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitution(new TSInstitution(), response)));
    }

    public removeInstitution(institutionID: string): Observable<any> {
        return this.$http.delete(`${this.serviceURL}/${encodeURIComponent(institutionID)}`);
    }

    public getAllInstitutionen(): Observable<TSInstitution[]> {
        return this.$http.get(this.serviceURL)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionen(response)));
    }

    public getAllBgInstitutionen(): Observable<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/bg`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionen(response)));
    }

    public getInstitutionenEditableForCurrentBenutzer(): Observable<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/editable/currentuser`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionen(response)));
    }

    public getInstitutionenListDTOEditableForCurrentBenutzer(): Observable<TSInstitutionListDTO[]> {
        return this.$http.get(`${this.serviceURL}/editable/currentuser/listdto`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionenListDTO(response)));
    }

    public getInstitutionenReadableForCurrentBenutzer(): Observable<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/readable/currentuser`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionen(response)));
    }

    public hasInstitutionenInStatusAngemeldet(): Observable<boolean> {
        return this.$http.get<boolean>(`${this.serviceURL}/hasEinladungen/currentuser`);
    }

    public getExternalClients(institutionId: string): Observable<TSInstitutionExternalClientAssignment> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(institutionId)}/externalclients`)
            .pipe(map(response => this.ebeguRestUtil.parseInstitutionExternalClientAssignment(response)));
    }

    public isStammdatenCheckRequired(): Observable<boolean> {
        return this.$http.get<boolean>(`${this.serviceURL}/isStammdatenCheckRequired/currentuser`);
    }

    public deactivateStammdatenCheckRequired(institutionId: string): Observable<TSInstitution> {
        return this.$http.put(`${this.serviceURL}/deactivateStammdatenCheckRequired/${institutionId}`, {})
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitution(new TSInstitution(), response)));
    }

    public isCurrentUserTagesschuleUser(): Observable<boolean> {
        return this.$http.get(`${this.serviceURL}/istagesschulenutzende/currentuser`)
            .pipe(map((response: any) => response));
    }

    public getInstitutionenForGemeinde(gemeindeId: string): Observable<TSInstitutionListDTO[]> {
        return this.$http.get(`${this.serviceURL}/gemeinde/listdto/${gemeindeId}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionenListDTO(response)));
    }

    public findAllInstitutionen(dossierId: string): Observable<Array<TSInstitution>> {
        return this.$http.get(`${this.serviceURL}/findAllInstitutionen/${encodeURIComponent(dossierId)}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseInstitutionen(response)));
    }
}
