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
import * as moment from 'moment';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSInstitutionExternalClientAssignment} from '../../../models/TSInstitutionExternalClientAssignment';
import {TSInstitutionListDTO} from '../../../models/TSInstitutionListDTO';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSInstitutionUpdate} from '../../../models/TSInstitutionUpdate';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class InstitutionRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
    ) {
        this.serviceURL = `${REST_API}institutionen`;
    }

    public findInstitution(institutionID: string): IPromise<TSInstitution> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(institutionID)}`)
            .then(response => this.ebeguRestUtil.parseInstitution(new TSInstitution(), response.data));
    }

    public updateInstitution(institutionID: string, update: TSInstitutionUpdate): IPromise<TSInstitutionStammdaten> {
        const restInstitution = this.ebeguRestUtil.institutionUpdateToRestObject(update);

        return this.$http.put(`${this.serviceURL}/${encodeURIComponent(institutionID)}`, restInstitution)
            .then(response => response.data)
            .then(data => this.ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(), data));
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
        gemeindeId: string,
    ): IPromise<TSInstitution> {
        const restInstitution = this.ebeguRestUtil.institutionToRestObject({}, institution);
        return this.$http.post(this.serviceURL, restInstitution,
            {
                params: {
                    date: DateUtil.momentToLocalDate(startDate),
                    betreuung: betreuungsangebot,
                    adminMail,
                    gemeindeId,
                },
            })
            .then(response => {
                return this.ebeguRestUtil.parseInstitution(new TSInstitution(), response.data);
            });
    }

    public removeInstitution(institutionID: string): IHttpPromise<any> {
        return this.$http.delete(`${this.serviceURL}/${encodeURIComponent(institutionID)}`);
    }

    public getAllInstitutionen(): IPromise<TSInstitution[]> {
        return this.$http.get(this.serviceURL).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionen(response.data);
        });
    }

    public getInstitutionenEditableForCurrentBenutzer(): IPromise<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/editable/currentuser`).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionen(response.data);
        });
    }

    public getInstitutionenListDTOEditableForCurrentBenutzer(): IPromise<TSInstitutionListDTO[]> {
        return this.$http.get(`${this.serviceURL}/editable/currentuser/listdto`).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionenListDTO(response.data);
        });
    }

    public getInstitutionenReadableForCurrentBenutzer(): IPromise<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/readable/currentuser`).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionen(response.data);
        });
    }

    public hasInstitutionenInStatusAngemeldet(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/hasEinladungen/currentuser`).then((response: any) => {
            return response.data;
        });
    }

    public getExternalClients(institutionId: string): IPromise<TSInstitutionExternalClientAssignment> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(institutionId)}/externalclients`)
            .then(response => this.ebeguRestUtil.parseInstitutionExternalClientAssignment(response.data));
    }

    public isStammdatenCheckRequired(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/isStammdatenCheckRequired/currentuser`).then((response: any) => {
            return response.data;
        });
    }

    public getServiceName(): string {
        return 'InstitutionRS';
    }

    public deactivateStammdatenCheckRequired(institutionId: string): IPromise<TSInstitution> {
        return this.$http.put(`${this.serviceURL}/deactivateStammdatenCheckRequired/${institutionId}`, {})
            .then((response: any) => {
                return this.ebeguRestUtil.parseInstitution(new TSInstitution(), response.data);
            });
    }

    public isCurrentUserTagesschuleUser(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/istagesschulenutzende/currentuser`).then((response: any) => {
            return response.data;
        });
    }
}
