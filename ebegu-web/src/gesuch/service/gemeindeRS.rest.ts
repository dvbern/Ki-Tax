/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import * as moment from 'moment';
import {BehaviorSubject, from, Observable, of} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import TSBenutzer from '../../models/TSBenutzer';
import TSGemeinde from '../../models/TSGemeinde';
import TSGemeindeStammdaten from '../../models/TSGemeindeStammdaten';
import DateUtil from '../../utils/DateUtil';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import GlobalCacheService from './globalCacheService';

export default class GemeindeRS implements IEntityRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'GlobalCacheService', 'AuthServiceRS'];
    public serviceURL: string;

    private readonly principalGemeindenSubject$ = new BehaviorSubject<TSGemeinde[]>([]);

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
        private readonly globalCacheService: GlobalCacheService,
        private readonly authServiceRS: AuthServiceRS,
    ) {
        this.serviceURL = `${REST_API}gemeinde`;

        this.initGemeindenForPrincipal();
    }

    public getAllGemeinden(): IPromise<TSGemeinde[]> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN);
        return this.$http.get(`${this.serviceURL}/all`, {cache})
            .then(response => this.ebeguRestUtil.parseGemeindeList(response.data));
    }

    public getAktiveGemeinden(): IPromise<TSGemeinde[]> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN);
        return this.$http.get(`${this.serviceURL}/active`, {cache})
            .then(response => this.ebeguRestUtil.parseGemeindeList(response.data));
    }

    public getGemeindenForPrincipal$(): Observable<TSGemeinde[]> {
        return this.principalGemeindenSubject$.asObservable();
    }

    public findGemeinde(gemeindeId: string): IPromise<TSGemeinde> {
        return this.$http.get(`${this.serviceURL}/id/${encodeURIComponent(gemeindeId)}`)
            .then(response => this.ebeguRestUtil.parseGemeinde(new TSGemeinde(), response.data));
    }

    public findGemeindeByName(gemeindeName: string): IPromise<TSGemeinde> {
        return this.$http.get(`${this.serviceURL}/name/${encodeURIComponent(gemeindeName)}`)
            .then(response => this.ebeguRestUtil.parseGemeinde(new TSGemeinde(), response.data));
    }

    private initGemeindenForPrincipal(): void {
        this.authServiceRS.principal$
            .pipe(switchMap(user => this.toGemeindenForPrincipal$(user)))
            .subscribe(
                gemeinden => {
                    this.principalGemeindenSubject$.next(gemeinden);
                },
                err => this.$log.error(err),
            );
    }

    public toGemeindenForPrincipal$(user: TSBenutzer | null): Observable<TSGemeinde[]> {
        if (!user) {
            return of([]); // empty list for unknown user
        }

        if (TSRoleUtil.isGemeindeRole(user.getCurrentRole())) {
            return of(angular.copy(user.extractCurrentGemeinden()));
        }

        return from(this.getAllGemeinden());
    }

    /**
     * It sends all required parameters (new Gemeinde, beguStartDatum and User) to the server so the server can create
     * all required objects within a single transaction.
     */
    public createGemeinde(gemeinde: TSGemeinde, beguStartDatum: moment.Moment, email: string): IPromise<TSGemeinde> {
        const restGemeinde = this.ebeguRestUtil.gemeindeToRestObject({}, gemeinde);

        return this.$http.post(this.serviceURL, restGemeinde,
            {
                params: {
                    date: DateUtil.momentToLocalDate(beguStartDatum),
                    adminMail: email,
                },
            })
            .then(response => {
                this.resetGemeindeCache(); // damit die neue Gemeinde in der Liste erscheint
                this.$log.debug('PARSING gemeinde REST object ', response.data);
                return this.ebeguRestUtil.parseGemeinde(new TSGemeinde(), response.data);
            });
    }

    private resetGemeindeCache(): void {
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN).removeAll();
        this.initGemeindenForPrincipal();
    }

    public getGemeindeStammdaten(gemeindeId: string): IPromise<TSGemeindeStammdaten> {
        return this.$http.get(`${this.serviceURL}/stammdaten/${encodeURIComponent(gemeindeId)}`)
            .then(response => this.ebeguRestUtil.parseGemeindeStammdaten(new TSGemeindeStammdaten(), response.data));
    }

    public saveGemeindeStammdaten(stammdaten: TSGemeindeStammdaten): IPromise<TSGemeindeStammdaten> {
        let restStammdaten = {};
        restStammdaten = this.ebeguRestUtil.gemeindeStammdatenToRestObject(restStammdaten, stammdaten);
        return this.$http.put(`${this.serviceURL}/stammdaten`, restStammdaten).then((response: any) => {
            this.resetGemeindeCache(); // damit die Statusänderung (eingeladen->aktiv) geladen werden kann
            this.$log.debug('PARSING GemeindeStammdaten REST object ', response.data);
            return this.ebeguRestUtil.parseGemeindeStammdaten(new TSGemeindeStammdaten(), response.data);
        });
    }

    public postLogoImage(gemeindeId: string, fileToUpload: File): IPromise<any> {
        const formData = new FormData();
        formData.append('file', fileToUpload, encodeURIComponent(fileToUpload.name));
        formData.append('kat', fileToUpload, encodeURIComponent('logo'));
        return this.uploadLogo(gemeindeId, formData);
    }

    private uploadLogo(gemeindeId: string, formData: FormData): IPromise<any> {
        return this.$http.post(`${this.serviceURL}/logo/${encodeURIComponent(gemeindeId)}`, formData)
            .then((response: any) => {
                this.$log.debug('Upload Gemeinde Logo ', response.data);
                return response.data;
        });
    }

}
