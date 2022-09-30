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
import {GlobalCacheService} from '../../../gesuch/service/globalCacheService';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSApplicationProperty} from '../../../models/TSApplicationProperty';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';

export class ApplicationPropertyRS {

    public static $inject = ['$http', 'EbeguRestUtil', 'GlobalCacheService'];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly globalCacheService: GlobalCacheService
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}application-properties`;
    }

    public getAllowedMimetypes(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.whitelist);
    }

    public isDevMode(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.devmode);
    }

    public isDummyMode(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.dummyMode);
    }

    public isMultimandantEnabled(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.mulitmandantAktiv);
    }

    public getSentryEnvName(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.sentryEnvName);
    }

    public isEbeguKibonAnfrageTestGuiEnabled(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.ebeguKibonAnfrageTestGuiEnabled);
    }

    public create(name: string, value: string): IHttpPromise<any> {
        return this.http.post(`${this.serviceURL}/${encodeURIComponent(name)}`, value, {
            headers: {
                'Content-Type': 'text/plain'
            }
        });
    }

    public update(name: string, value: string): IHttpPromise<any> {
        return this.create(name, value);
    }

    public remove(name: string): IHttpPromise<any> {
        return this.http.delete(`${this.serviceURL}/${encodeURIComponent(name)}`);
    }

    public getAllApplicationProperties(): IPromise<TSApplicationProperty[]> {
        return this.http.get(`${this.serviceURL}/`).then(
            (response: any) => this.ebeguRestUtil.parseApplicationProperties(response.data)
        );
    }

    public isZahlungenTestMode(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.zahlungentestmode);
    }

    public isPersonensucheDisabledForSystem(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.personenSucheDisabled);
    }

    public getKitaxHost(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.kitaxHost);
    }

    public getKitaxUrl(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.kitaxHost + response.kitaxEndpoint);
    }

    public getActivatedDemoFeatures(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.activatedDemoFeatures);
    }

    public getPublicPropertiesCached(): IPromise<TSPublicAppConfig> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_PUBLIC_APP_CONFIG);
        return this.http.get(`${this.serviceURL}/public/all`, {cache}).then(
            (response: any) => this.ebeguRestUtil.parsePublicAppConfig(response.data)
        );
    }

    // we keep this method because it is used to perform healthchecks
    public getBackgroundColorFromServer(): IPromise<TSApplicationProperty> {
        return this.http.get(`${this.serviceURL}/public/background`).then(response => this.ebeguRestUtil.parseApplicationProperty(new TSApplicationProperty(), response.data));
    }

    public getNotverordnungDefaultEinreichefristPrivat(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.notverordnungDefaultEinreichefristOeffentlich);
    }

    public getNotverordnungDefaultEinreichefristOeffentlich(): IPromise<string> {
        return this.getPublicPropertiesCached().then(response => response.notverordnungDefaultEinreichefristPrivat);
    }

    public getFrenchEnabled(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.frenchEnabled);
    }

    public getGeresEnabledForMandant(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.geresEnabledForMandant);
    }

    public getZusatzinformationenInstitutionEnabled(): IPromise<boolean> {
        return this.getPublicPropertiesCached().then(response => response.zusatzinformationenInstitution);
    }
}
