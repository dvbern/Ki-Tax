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

import {IHttpPromise, IHttpRequestTransformer, IHttpService, ILogService, IPromise} from 'angular';
import {BehaviorSubject, from, Observable, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import {TSDokumentTyp} from '../../models/enums/TSDokumentTyp';
import {TSRole} from '../../models/enums/TSRole';
import {TSSprache} from '../../models/enums/TSSprache';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSBfsGemeinde} from '../../models/TSBfsGemeinde';
import {TSExternalClientAssignment} from '../../models/TSExternalClientAssignment';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSGemeindeRegistrierung} from '../../models/TSGemeindeRegistrierung';
import {TSGemeindeStammdaten} from '../../models/TSGemeindeStammdaten';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {GlobalCacheService} from './globalCacheService';

export class GemeindeRS implements IEntityRS {

    public static $inject =
        ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'GlobalCacheService', 'AuthServiceRS'];
    public serviceURL: string;

    private readonly principalGemeindenSubject$ = new BehaviorSubject<TSGemeinde[]>([]);
    private readonly principalGemeindenSubjectTS$ = new BehaviorSubject<TSGemeinde[]>([]);
    private readonly principalGemeindenSubjectFI$ = new BehaviorSubject<TSGemeinde[]>([]);

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
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN_ACTIVE);
        return this.$http.get(`${this.serviceURL}/active`, {cache})
            .then(response => this.ebeguRestUtil.parseGemeindeList(response.data));
    }

    public getGemeindenForTSByPrincipal$(): Observable<TSGemeinde[]> {
        return this.principalGemeindenSubjectTS$.asObservable();
    }

    public getGemeindenForFIByPrincipal$(): Observable<TSGemeinde[]> {
        return this.principalGemeindenSubjectFI$.asObservable();
    }

    public getGemeindenForPrincipal$(): Observable<TSGemeinde[]> {
        return this.principalGemeindenSubject$.asObservable();
    }

    public findGemeinde(gemeindeId: string): IPromise<TSGemeinde> {
        return this.$http.get(`${this.serviceURL}/id/${encodeURIComponent(gemeindeId)}`)
            .then(response => this.ebeguRestUtil.parseGemeinde(new TSGemeinde(), response.data));
    }

    private initGemeindenForPrincipal(): void {
        this.authServiceRS.principal$
            .pipe(switchMap(user => this.toGemeindenForPrincipal$(user)))
            .subscribe(
                gemeinden => {
                    this.principalGemeindenSubject$.next(gemeinden);

                    const gemeindenTS = angular.copy(gemeinden.filter(g => {
                        return g.angebotTS;
                    }));
                    const gemeindenFI = angular.copy(gemeinden.filter(g => {
                        return g.angebotFI;
                    }));

                    this.principalGemeindenSubjectTS$.next(gemeindenTS);
                    this.principalGemeindenSubjectFI$.next(gemeindenFI);
                },
                err => this.$log.error(err),
            );
    }

    public toGemeindenForPrincipal$(user: TSBenutzer | null): Observable<TSGemeinde[]> {
        if (!user) {
            return of([]); // empty list for unknown user
        }

        if (TSRoleUtil.isGemeindeRole(user.getCurrentRole()) && TSRole.SUPER_ADMIN !== user.getCurrentRole()) {
            return of(angular.copy(user.extractCurrentGemeinden()));
        }

        return from(this.getAllGemeinden());
    }

    /**
     * It sends all required parameters (new Gemeinde, beguStartDatum and User) to the server so the server can create
     * all required objects within a single transaction.
     */
    public createGemeinde(gemeinde: TSGemeinde, email: string): IPromise<TSGemeinde> {

        const restGemeinde = this.ebeguRestUtil.gemeindeToRestObject({}, gemeinde);

        return this.$http.post(this.serviceURL, restGemeinde,
            {
                params: {
                    adminMail: email
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
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN_ACTIVE).removeAll();
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN_WITH_MAHLZEITENVERGUENSTIGUNG).removeAll();
        // Nur beim SuperAdmin und Mandant-User werden die Gemeinden aus dem Service gelesen,
        // bei allen Gemeinde-Benutzern aus dem User! Dieser ist aber u.U. nicht mehr aktuell
        this.authServiceRS.reloadCurrentUser();
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

    public getLogoUrl(gemeindeId: string): string {
        return `${this.serviceURL}/logo/data/${encodeURIComponent(gemeindeId)}?timestamp=${new Date().getTime()}`;
    }

    public getSupportedImageUrl(): string {
        return `${this.serviceURL}/supported/image?timestamp=${new Date().getTime()}`;
    }

    public uploadLogoImage(gemeindeId: string, fileToUpload: File): IPromise<any> {
        const formData = new FormData();
        formData.append('file', fileToUpload, encodeURIComponent(fileToUpload.name));
        formData.append('kat', fileToUpload, encodeURIComponent('logo'));
        return this.postLogo(gemeindeId, formData);
    }

    private postLogo(gemeindeId: string, formData: FormData): IPromise<any> {
        let result: IPromise<any>;
        result = this.$http.post(this.getLogoUrl(gemeindeId), formData, {
            transformRequest: (request: IHttpRequestTransformer) => request,
            headers: {'Content-Type': undefined}
        })
            .then((response: any) => {
                this.$log.debug('Upload Gemeinde Logo ', response.data);
                return response.data;
            });
        if (!result) {
            this.$log.error(`Upload Gemeinde (${gemeindeId}) Logo failed`);
        }
        return result;
    }

    public isSupportedImage(fileToUpload: File): IPromise<any> {
        const formData = new FormData();
        formData.append('file', fileToUpload, encodeURIComponent(fileToUpload.name));
        formData.append('kat', fileToUpload, encodeURIComponent('logo'));
        return this.$http.post(this.getSupportedImageUrl(), formData, {
            transformRequest: (request: IHttpRequestTransformer) => request,
            headers: {'Content-Type': undefined}
        });
    }

    public getUnregisteredBfsGemeinden(): IPromise<TSBfsGemeinde[]> {
        return this.$http.get(`${this.serviceURL}/unregistered`)
            .then(response => this.ebeguRestUtil.parseBfsGemeindeList(response.data));
    }

    public getAllBfsGemeinden(): IPromise<TSBfsGemeinde[]> {
        return this.$http.get(`${this.serviceURL}/allBfs`)
            .then(response => this.ebeguRestUtil.parseBfsGemeindeList(response.data));
    }

    public hasGemeindenInStatusAngemeldet(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/hasEinladungen/currentuser`).then((response: any) => {
            return response.data;
        });
    }

    public getGemeindenRegistrierung(gemeindeBGId: string, gemeindenTSIds: string[]
    ): IPromise<TSGemeindeRegistrierung[]> {
        const gemeindeBGIdOrNull = gemeindeBGId.length !== 0
            ? encodeURIComponent(gemeindeBGId)
            : null;
        let gemeindenTSIdOrNull = '';
        gemeindenTSIds.forEach(
            id => (gemeindenTSIdOrNull.length > 0
                ? gemeindenTSIdOrNull += ',' + encodeURIComponent(id)
                : gemeindenTSIdOrNull += encodeURIComponent(id)));
        return this.$http.get(
            `${this.serviceURL}/gemeindeRegistrierung/${gemeindeBGIdOrNull}/${gemeindenTSIdOrNull.length !== 0 ?
                encodeURIComponent(gemeindenTSIdOrNull) : null}`)
            .then(response => this.ebeguRestUtil.parseGemeindeRegistrierungList(response.data));
    }

    public getAktiveUndVonSchulverbundGemeinden(): IPromise<TSGemeinde[]> {
        return this.$http.get(`${this.serviceURL}/activeAndSchulverbund`)
            .then(response => this.ebeguRestUtil.parseGemeindeList(response.data));
    }

    public updateAngebote(gemeinde: TSGemeinde): IPromise<any> {
        let restGemeinde = {};
        restGemeinde = this.ebeguRestUtil.gemeindeToRestObject(restGemeinde, gemeinde);
        return this.$http.put(`${this.serviceURL}/updateangebote`, restGemeinde);
    }

    public removeGemeindeGesuchsperiodeDokument(gemeindeId: string, gesuchsperiodeId: string, sprache: TSSprache,
                                                dokumentTyp: TSDokumentTyp): IHttpPromise<TSGesuchsperiode> {
        // tslint:disable-next-line:max-line-length
        return this.$http.delete(`${this.serviceURL}/gemeindeGesuchsperiodeDoku/${encodeURIComponent(gemeindeId)}/${encodeURIComponent(gesuchsperiodeId)}/${sprache}/${dokumentTyp}`);
    }

    public existGemeindeGesuchsperiodeDokument(gemeindeId: string, gesuchsperiodeId: string, sprache: TSSprache,
                                               dokumentTyp: TSDokumentTyp): IPromise<boolean> {
        // tslint:disable-next-line:max-line-length
        return this.$http.get(`${this.serviceURL}/existGemeindeGesuchsperiodeDoku/${encodeURIComponent(gemeindeId)}/${encodeURIComponent(gesuchsperiodeId)}/${sprache}/${dokumentTyp}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public downloadGemeindeGesuchsperiodeDokument(gemeindeId: string, gesuchsperiodeId: string, sprache: TSSprache,
                                                  dokumentTyp: TSDokumentTyp): IPromise<BlobPart> {
        // tslint:disable-next-line:max-line-length
        return this.$http.get(`${this.serviceURL}/gemeindeGesuchsperiodeDoku/${encodeURIComponent(gemeindeId)}/${encodeURIComponent(gesuchsperiodeId)}/${sprache}/${dokumentTyp}`,
            {responseType: 'blob'})
            .then((response: any) => {
                return response.data;
            });
    }

    public getExternalClients(gemeindeId: string): IPromise<TSExternalClientAssignment> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(gemeindeId)}/externalclients`)
            .then(response => this.ebeguRestUtil.parseExternalClientAssignment(response.data));
    }

    public getGemeindenWithMahlzeitenverguenstigungForBenutzer(): IPromise<TSGemeinde[]> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN_WITH_MAHLZEITENVERGUENSTIGUNG);
        return this.$http.get(`${this.serviceURL}/mahlzeitenverguenstigung`, {cache})
            .then(response => this.ebeguRestUtil.parseGemeindeList(response.data));
    }
}
