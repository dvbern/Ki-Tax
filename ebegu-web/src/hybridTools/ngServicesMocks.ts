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
import {Observable, of} from 'rxjs';
import {BenutzerRS} from '../app/core/service/benutzerRS.rest';
import {I18nServiceRSRest} from '../app/i18n/services/i18nServiceRS.rest';
import {AuthLifeCycleService} from '../authentication/service/authLifeCycle.service';
import {GesuchGenerator} from '../gesuch/service/gesuchGenerator';
import {TSAuthEvent} from '../models/enums/TSAuthEvent';
import {TSBrowserLanguage} from '../models/enums/TSBrowserLanguage';
import {TSCreationAction} from '../models/enums/TSCreationAction';
import {TSEingangsart} from '../models/enums/TSEingangsart';
import {TSDossier} from '../models/TSDossier';
import {TSFall} from '../models/TSFall';
import {TSGesuch} from '../models/TSGesuch';

ngServicesMock.$inject = ['$provide'];

class GesuchGeneratorMock extends GesuchGenerator {

    public constructor() {
        super(undefined, undefined, undefined, undefined, undefined, undefined, undefined, undefined);
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

export function ngServicesMock($provide: angular.auto.IProvideService): void {
    $provide.service('I18nServiceRSRest', I18nServiceMock);
    $provide.service('AuthLifeCycleService', AuthLifeCycleServiceMock);
    $provide.service('GesuchGenerator', GesuchGeneratorMock);
    $provide.service('BenutzerRS', BenutzerRS);
    $provide.value('LOCALE_ID', 'de-CH');
}
