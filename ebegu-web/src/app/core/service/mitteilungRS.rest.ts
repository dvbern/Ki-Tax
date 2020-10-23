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

import {IHttpService, ILogService, IPromise} from 'angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';
import {TSBetreuungspensum} from '../../../models/TSBetreuungspensum';
import {TSDossier} from '../../../models/TSDossier';
import {TSMitteilung} from '../../../models/TSMitteilung';
import {TSMtteilungSearchresultDTO} from '../../../models/TSMitteilungSearchresultDTO';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import ITranslateService = angular.translate.ITranslateService;

export class MitteilungRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'AuthServiceRS', '$translate'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $translate: ITranslateService,
    ) {
        this.serviceURL = `${REST_API}mitteilungen`;
    }

    public getServiceName(): string {
        return 'MitteilungRS';
    }

    public sendMitteilung(mitteilung: TSMitteilung): IPromise<TSMitteilung> {
        let restMitteilung = {};
        restMitteilung = this.ebeguRestUtil.mitteilungToRestObject(restMitteilung, mitteilung);
        return this.$http.put(`${this.serviceURL}/send`, restMitteilung).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data);
        });
    }

    public setMitteilungGelesen(mitteilungId: string): IPromise<TSMitteilung> {
        return this.$http.put(`${this.serviceURL}/setgelesen/${mitteilungId}`, null).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data);
        });
    }

    public setMitteilungErledigt(mitteilungId: string): IPromise<TSMitteilung> {
        return this.$http.put(`${this.serviceURL}/seterledigt/${mitteilungId}`, null).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data);
        });
    }

    public getEntwurfOfDossierForCurrentRolle(dossierId: string): IPromise<TSMitteilung> {
        return this.$http.get(`${this.serviceURL}/entwurf/dossier/${dossierId}`).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data);
        });
    }

    public getMitteilungenOfDossierForCurrentRolle(dossierId: string): IPromise<Array<TSMitteilung>> {
        return this.$http.get(`${this.serviceURL}/forrole/dossier/${dossierId}`).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungen); // The response is a wrapper
        });
    }

    public getMitteilungenForCurrentRolleForBetreuung(betreuungId: string): IPromise<Array<TSMitteilung>> {
        return this.$http.get(`${this.serviceURL}/forrole/betreuung/${betreuungId}`).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungen); // The response is a wrapper
        });
    }

    public getAmountMitteilungenForCurrentBenutzer(): IPromise<number> {
        return this.$http.get(`${this.serviceURL}/amountnewforuser/notokenrefresh`).then((response: any) => {
            return response.data;
        });
    }

    public setAllNewMitteilungenOfDossierGelesen(dossierId: string): IPromise<Array<TSMitteilung>> {
        return this.$http.put(`${this.serviceURL}/setallgelesen/${dossierId}`, null).then((response: any) => {
            this.$log.debug('PARSING mitteilungen REST objects ', response.data);
            return this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungen); // The response is a wrapper
        });
    }

    public getAmountNewMitteilungenOfDossierForCurrentRolle(dossierId: string): IPromise<number> {
        return this.$http.get(`${this.serviceURL}/amountnew/dossier/${dossierId}`).then((response: any) => {
            return response.data;
        });
    }

    public sendbetreuungsmitteilung(dossier: TSDossier, betreuung: TSBetreuung): IPromise<TSBetreuungsmitteilung> {
        const mutationsmeldung = this.createBetreuungsmitteilung(dossier, betreuung);
        const restMitteilung = this.ebeguRestUtil.betreuungsmitteilungToRestObject({}, mutationsmeldung);
        return this.$http.put(`${this.serviceURL}/sendbetreuungsmitteilung`, restMitteilung).then((response: any) => {
            this.$log.debug('PARSING Betreuungsmitteilung REST object ', response.data);
            return this.ebeguRestUtil.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), response.data);
        });
    }

    public applyBetreuungsmitteilung(betreuungsmitteilungId: string): IPromise<string> {
        return this.$http.put(`${this.serviceURL}/applybetreuungsmitteilung/${betreuungsmitteilungId}`,
            null).then((response: any) => {
            return response.data;
        });
    }

    public getNewestBetreuungsmitteilung(betreuungId: string): IPromise<TSBetreuungsmitteilung> {
        return this.$http.get(`${this.serviceURL}/newestBetreuunsmitteilung/${betreuungId}`).then((response: any) => {
            this.$log.debug('PARSING Betreuungsmitteilung REST object ', response.data);
            return this.ebeguRestUtil.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), response.data);
        });
    }

    public mitteilungWeiterleiten(mitteilungId: string, userName: string): IPromise<TSMitteilung> {
        return this.$http.get(`${this.serviceURL}/weiterleiten/${mitteilungId}/${userName}`).then((response: any) => {
            return this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data);
        });
    }

    public searchMitteilungen(antragSearch: any, includeClosed: boolean): IPromise<TSMtteilungSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/search/${includeClosed}`, antragSearch).then((response: any) => {
            this.$log.debug('PARSING antraege REST array object', response);
            return new TSMtteilungSearchresultDTO(this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungDTOs),
                response.data.paginationDTO.totalItemCount);
        });
    }

    private createBetreuungsmitteilung(dossier: TSDossier, betreuung: TSBetreuung): TSBetreuungsmitteilung {
        const mutationsmeldung = new TSBetreuungsmitteilung();
        mutationsmeldung.betreuung = betreuung;
        mutationsmeldung.betreuungspensen = this.extractPensenFromBetreuung(betreuung);
        mutationsmeldung.dossier = dossier;
        mutationsmeldung.mitteilungStatus = TSMitteilungStatus.NEU;
        return mutationsmeldung;
    }

    /**
     * Kopiert alle Betreuungspensen der gegebenen Betreuung in einer neuen Liste und
     * gibt diese zurueck. By default wird eine leere Liste zurueckgegeben
     */
    private extractPensenFromBetreuung(betreuung: TSBetreuung): Array<TSBetreuungspensum> {
        const pensen: Array<TSBetreuungspensum> = [];
        betreuung.betreuungspensumContainers.forEach(betpenContainer => {
            const pensumJA = angular.copy(betpenContainer.betreuungspensumJA);
            pensumJA.id = undefined; // the id must be set to undefined in order no to duplicate it
            pensen.push(pensumJA);
        });
        return pensen;
    }

    public abweichungenFreigeben(betreuung: TSBetreuung, dossier: TSDossier): IPromise<any> {
        const mutationsmeldung = this.createBetreuungsmitteilung(dossier, betreuung);
        const restMitteilung = this.ebeguRestUtil.betreuungsmitteilungToRestObject({}, mutationsmeldung);
        const url = `${this.serviceURL}/betreuung/abweichungenfreigeben/${encodeURIComponent(betreuung.id)}`;
        return this.$http.put(url, restMitteilung)
            .then(response => {
                return this.ebeguRestUtil.parseBetreuungspensumAbweichungen(response.data);
            });
    }
}
