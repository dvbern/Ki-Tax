/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import {EMPTY, from, Observable} from 'rxjs';
import {catchError, concatMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';
import {TSBetreuungspensum} from '../../../models/TSBetreuungspensum';
import {TSDossier} from '../../../models/TSDossier';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {TSMitteilung} from '../../../models/TSMitteilung';
import {TSMtteilungSearchresultDTO} from '../../../models/TSMitteilungSearchresultDTO';
import {TSMitteilungVerarbeitung} from '../../../models/TSMitteilungVerarbeitung';
import {TSMitteilungVerarbeitungResult} from '../../../models/TSMitteilungVerarbeitungResult';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {ErrorService} from '../errors/service/ErrorService';
import ITranslateService = angular.translate.ITranslateService;

export class MitteilungRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'AuthServiceRS', '$translate', 'ErrorService'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $translate: ITranslateService,
        private readonly errorService: ErrorService
    ) {
        this.serviceURL = `${REST_API}mitteilungen`;
    }

    public getServiceName(): string {
        return 'MitteilungRS';
    }

    public sendMitteilung(mitteilung: TSMitteilung): IPromise<TSMitteilung> {
        let restMitteilung = {};
        restMitteilung = this.ebeguRestUtil.mitteilungToRestObject(restMitteilung, mitteilung);
        return this.$http.put(`${this.serviceURL}/send`, restMitteilung)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public setMitteilungGelesen(mitteilungId: string): IPromise<TSMitteilung> {
        return this.$http.put(`${this.serviceURL}/setgelesen/${mitteilungId}`, null)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public setMitteilungErledigt(mitteilungId: string): IPromise<TSMitteilung> {
        return this.$http.put(`${this.serviceURL}/seterledigt/${mitteilungId}`, null)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public setMitteilungUngelesen(mitteilungId: string): IPromise<TSMitteilung> {
        return this.$http.put(`${this.serviceURL}/setneu/${mitteilungId}`, null)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public setMitteilungIgnoriert(mitteilungId: string): IPromise<TSMitteilung> {
        return this.$http.put(`${this.serviceURL}/setignoriert/${mitteilungId}`, null)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public getEntwurfOfDossierForCurrentRolle(dossierId: string): IPromise<TSMitteilung> {
        return this.$http.get(`${this.serviceURL}/entwurf/dossier/${dossierId}`)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public getMitteilungenOfDossierForCurrentRolle(dossierId: string): IPromise<Array<TSMitteilung>> {
        return this.$http.get(`${this.serviceURL}/forrole/dossier/${dossierId}`).then((response: any) =>
             this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungen) // The response is a wrapper
        );
    }

    public getMitteilungenForCurrentRolleForBetreuung(betreuungId: string): IPromise<Array<TSMitteilung>> {
        return this.$http.get(`${this.serviceURL}/forrole/betreuung/${betreuungId}`).then((response: any) =>
             this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungen) // The response is a wrapper
        );
    }

    public getAmountMitteilungenForCurrentBenutzer(): IPromise<number> {
        return this.$http.get(`${this.serviceURL}/amountnewforuser/notokenrefresh`).then((response: any) => response.data);
    }

    public setAllNewMitteilungenOfDossierGelesen(dossierId: string): IPromise<Array<TSMitteilung>> {
        return this.$http.put(`${this.serviceURL}/setallgelesen/${dossierId}`, null).then((response: any) => {
            this.$log.debug('PARSING mitteilungen REST objects ', response.data);
            return this.ebeguRestUtil.parseMitteilungen(response.data.mitteilungen); // The response is a wrapper
        });
    }

    public getAmountNewMitteilungenOfDossierForCurrentRolle(dossierId: string): IPromise<number> {
        return this.$http.get(`${this.serviceURL}/amountnew/dossier/${dossierId}`).then((response: any) => response.data);
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
            null).then((response: any) => response.data);
    }

    public getNewestBetreuungsmitteilung(betreuungId: string): IPromise<TSBetreuungsmitteilung> {
        return this.$http.get(`${this.serviceURL}/newestBetreuunsmitteilung/${betreuungId}`).then((response: any) => {
            this.$log.debug('PARSING Betreuungsmitteilung REST object ', response.data);
            return this.ebeguRestUtil.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), response.data);
        });
    }

    public mitteilungWeiterleiten(mitteilungId: string, userName: string): IPromise<TSMitteilung> {
        return this.$http.get(`${this.serviceURL}/weiterleiten/${mitteilungId}/${userName}`)
            .then((response: any) => this.ebeguRestUtil.parseMitteilung(new TSMitteilung(), response.data));
    }

    public searchMitteilungen(antragSearch: any, includeClosed: boolean): IPromise<TSMtteilungSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/search/${includeClosed}`, antragSearch).then((response: any) => {
            this.$log.debug('PARSING Mitteilung REST array object', response);
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
            if (EbeguUtil.isNotNullOrUndefined(pensumJA.eingewoehnungPauschale)) {
                pensumJA.eingewoehnungPauschale.id = undefined;
            }
            pensen.push(pensumJA);
        });
        return pensen;
    }

    public abweichungenFreigeben(betreuung: TSBetreuung, dossier: TSDossier): IPromise<any> {
        const mutationsmeldung = this.createBetreuungsmitteilung(dossier, betreuung);
        const restMitteilung = this.ebeguRestUtil.betreuungsmitteilungToRestObject({}, mutationsmeldung);
        const url = `${this.serviceURL}/betreuung/abweichungenfreigeben/${encodeURIComponent(betreuung.id)}`;
        return this.$http.put(url, restMitteilung)
            .then(response => this.ebeguRestUtil.parseBetreuungspensumAbweichungen(response.data));
    }

    public applyAlleBetreuungsmitteilungen(mitteilungen: TSBetreuungsmitteilung[]): Observable<TSMitteilungVerarbeitungResult> {
        const verarbeitung = new TSMitteilungVerarbeitung(mitteilungen.length);

        // group mitteilungen by fall, so that we can apply them in parallel
        const mitteilungenByFall = this.groupMitteilungenByFall(mitteilungen);

        Object.entries(mitteilungenByFall).forEach(([_, mitteilungenOfFall ]) => {
            from(mitteilungenOfFall ?? [])
                .pipe(
                    // apply all mitteilungen of one fall in sequence
                    concatMap(mitteilung =>
                        this.applyBetreuungsmitteilungSilently(mitteilung).pipe(
                            catchError((errors: TSExceptionReport[]) => {
                                verarbeitung.addError(mitteilung, errors);
                                errors.forEach(error => {
                                    // we want to display it in the dialog, not in the error bar
                                    this.errorService.clearError(error.msgKey);
                                });
                                return EMPTY;
                            })
                        )
                    )
                )
                .subscribe(appliedMitteilung => {
                    if (EbeguUtil.isEmptyStringNullOrUndefined(appliedMitteilung.errorMessage)) {
                        verarbeitung.addSuccess(appliedMitteilung);
                    } else {
                        verarbeitung.addFailure(appliedMitteilung);
                    }
                });
        });

        return verarbeitung.results;
    }

    private groupMitteilungenByFall(mitteilungen: TSBetreuungsmitteilung[]): { [fallId: string]: TSBetreuungsmitteilung[] } {
        return mitteilungen.reduce(
            (acc, mitteilung) => ({
                ...acc,
                [mitteilung.dossier.fall.id]: [...(acc[mitteilung.dossier.fall.id] ?? []), mitteilung],
            }),
            {} as Record<string, TSBetreuungsmitteilung[]>,
        );
    }

    private applyBetreuungsmitteilungSilently(
        mitteilung: TSBetreuungsmitteilung
    ): Observable<TSBetreuungsmitteilung>{
        return from(
            this.$http.post<TSBetreuungsmitteilung>(
                `${this.serviceURL}/applybetreuungsmitteilungsilently`,
                this.ebeguRestUtil.betreuungsmitteilungToRestObject({}, mitteilung))
                .then(res => res.data)
                .then(restMitteilung =>
                    this.ebeguRestUtil.parseBetreuungsmitteilung(new TSBetreuungsmitteilung(), restMitteilung)));
    }

    public neueVeranlagungsmitteilungBearbeiten(mitteilungId: string): IPromise<string> {
        return this.$http.put(`${this.serviceURL}/neueVeranlagungsmitteilungBearbeiten/${mitteilungId}`,
            null).then((response: any) => response.data);
    }
}
