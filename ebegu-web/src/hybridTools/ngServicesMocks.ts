/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import * as angular from 'angular';
import {CookieService} from 'ngx-cookie-service';
import {Observable, of} from 'rxjs';
import {KiBonMandant} from '../app/core/constants/MANDANTS';
import {ErrorServiceX} from '../app/core/errors/service/ErrorServiceX';
import {BenutzerRSX} from '../app/core/service/benutzerRSX.rest';
import {VersionService} from '../app/core/service/version/version.service';
import {WindowRef} from '../app/core/service/windowRef.service';
import {I18nServiceRSRest} from '../app/i18n/services/i18nServiceRS.rest';
import {MandantService} from '../app/shared/services/mandant.service';
import {AuthLifeCycleService} from '../authentication/service/authLifeCycle.service';
import {InternePendenzenRS} from '../gesuch/component/internePendenzenView/internePendenzenRS.rest';
import {GesuchGenerator} from '../gesuch/service/gesuchGenerator';
import {SearchRS} from '../gesuch/service/searchRS.rest';
import {TSAuthEvent} from '../models/enums/TSAuthEvent';
import {TSBrowserLanguage} from '../models/enums/TSBrowserLanguage';
import {TSCreationAction} from '../models/enums/TSCreationAction';
import {TSEingangsart} from '../models/enums/TSEingangsart';
import {TSDossier} from '../models/TSDossier';
import {TSExceptionReport} from '../models/TSExceptionReport';
import {TSFall} from '../models/TSFall';
import {TSGesuch} from '../models/TSGesuch';

ngServicesMock.$inject = ['$provide'];

class GesuchGeneratorMock extends GesuchGenerator {

    public constructor() {
        super(undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined);
    }

    public initGesuch(
        eingangsart: TSEingangsart,
        _creationAction: TSCreationAction,
        _gesuchsperiodeId: string,
        _currentFall: TSFall,
        _currentDossier: TSDossier,
    ): angular.IPromise<TSGesuch> {

        const gesuch = new TSGesuch();
        gesuch.dossier = new TSDossier();
        gesuch.eingangsart = eingangsart;
        return Promise.resolve(gesuch);
    }

    public createNewGesuch(gesuch: TSGesuch): angular.IPromise<TSGesuch> {
        return Promise.resolve(gesuch);
    }

    public createNewDossier(dossier: TSDossier): angular.IPromise<TSDossier> {
        return Promise.resolve(dossier);
    }

    public createNewFall(fall: TSFall): angular.IPromise<TSFall> {
        return Promise.resolve(fall);
    }
}

class AuthLifeCycleServiceMock extends AuthLifeCycleService {
    public get$(event: TSAuthEvent): Observable<TSAuthEvent> {
        return of(event);
    }

    public changeAuthStatus(_status: TSAuthEvent, _message?: string): void {
        return;
    }
}

class I18nServiceMock extends I18nServiceRSRest {
    public extractPreferredLanguage(): string {
        return TSBrowserLanguage.DE;
    }

    public currentLanguage(): TSBrowserLanguage {
        return TSBrowserLanguage.DE;
    }
}

class CookieServiceMock extends CookieService {
    public get(): string {
        return '';
    }
}

class ErrorServiceXMock extends ErrorServiceX {
    public getErrors(): ReadonlyArray<TSExceptionReport> {
        return [];
    }
}

class MandantServiceMock extends MandantService {
    public get mandant$(): Observable<KiBonMandant> {
        return of(KiBonMandant.BE);
    }
}

class SearchRSMock extends SearchRS {
}

export function ngServicesMock($provide: angular.auto.IProvideService): void {
    $provide.service('I18nServiceRSRest', I18nServiceMock);
    $provide.service('AuthLifeCycleService', AuthLifeCycleServiceMock);
    $provide.service('GesuchGenerator', GesuchGeneratorMock);
    $provide.service('InternePendenzenRS', InternePendenzenRS);
    $provide.service('BenutzerRS', BenutzerRSX);
    $provide.service('VersionService', VersionService);
    $provide.service('MandantService', MandantServiceMock);
    $provide.service('windowRef', WindowRef);
    $provide.service('cookieService', CookieServiceMock);
    $provide.service('ErrorServiceX', ErrorServiceXMock);
    $provide.service('SearchRS', SearchRSMock);
    $provide.value('LOCALE_ID', 'de-CH');
    $provide.value('platformId', 'de-CH');
}
