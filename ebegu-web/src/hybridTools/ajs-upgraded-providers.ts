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

import {ApplicationPropertyRS} from '../admin/service/applicationPropertyRS.rest';
import {DailyBatchRS} from '../admin/service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../admin/service/databaseMigrationRS.rest';
import {ReindexRS} from '../admin/service/reindexRS.rest';
import {TestFaelleRS} from '../admin/service/testFaelleRS.rest';
import AuthServiceRS from '../authentication/service/AuthServiceRS.rest';
import {DvDialog} from '../core/directive/dv-dialog/dv-dialog';
import ErrorService from '../core/errors/service/ErrorService';
import GesuchsperiodeRS from '../core/service/gesuchsperiodeRS.rest';
import {TraegerschaftRS} from '../core/service/traegerschaftRS.rest';
import UserRS from '../core/service/userRS.rest';
import ZahlungRS from '../core/service/zahlungRS.rest';
import GemeindeRS from '../gesuch/service/gemeindeRS.rest';
import GesuchRS from '../gesuch/service/gesuchRS.rest';

// AuthServiceRS
export function authServiceRSServiceFactory(i: any) {
    return i.get('AuthServiceRS');
}

export const authServiceRSProvider = {
    provide: AuthServiceRS,
    useFactory: authServiceRSServiceFactory,
    deps: ['$injector']
};

// ApplicationPropertyRS
export function applicationPropertyRSServiceFactory(i: any) {
    return i.get('ApplicationPropertyRS');
}

export const applicationPropertyRSProvider = {
    provide: ApplicationPropertyRS,
    useFactory: applicationPropertyRSServiceFactory,
    deps: ['$injector']
};

// TraegerschaftRS
export function traegerschaftRSProviderServiceFactory(i: any) {
    return i.get('TraegerschaftRS');
}

export const traegerschaftRSProvider = {
    provide: TraegerschaftRS,
    useFactory: traegerschaftRSProviderServiceFactory,
    deps: ['$injector']
};

// ErrorService
export function errorServiceProviderServiceFactory(i: any) {
    return i.get('ErrorService');
}

export const errorServiceProvider = {
    provide: ErrorService,
    useFactory: errorServiceProviderServiceFactory,
    deps: ['$injector']
};

// DvDialog
export function dvDialogProviderServiceFactory(i: any) {
    return i.get('DvDialog');
}

export const dvDialogProvider = {
    provide: DvDialog,
    useFactory: dvDialogProviderServiceFactory,
    deps: ['$injector']
};

// TestFaelleRS
export function testFaelleRSProviderServiceFactory(i: any) {
    return i.get('TestFaelleRS');
}

export const testFaelleRSProvider = {
    provide: TestFaelleRS,
    useFactory: testFaelleRSProviderServiceFactory,
    deps: ['$injector']
};

// UserRS
export function userRSProviderServiceFactory(i: any) {
    return i.get('UserRS');
}

export const userRSProvider = {
    provide: UserRS,
    useFactory: userRSProviderServiceFactory,
    deps: ['$injector']
};

// ReindexRS
export function reindexRSProviderServiceFactory(i: any) {
    return i.get('ReindexRS');
}

export const reindexRSProvider = {
    provide: ReindexRS,
    useFactory: reindexRSProviderServiceFactory,
    deps: ['$injector']
};

// GesuchsperiodeRS
export function gesuchsperiodeRSProviderServiceFactory(i: any) {
    return i.get('GesuchsperiodeRS');
}

export const gesuchsperiodeRSProvider = {
    provide: GesuchsperiodeRS,
    useFactory: gesuchsperiodeRSProviderServiceFactory,
    deps: ['$injector']
};

// DatabaseMigrationRS
export function databaseMigrationRSProviderServiceFactory(i: any) {
    return i.get('DatabaseMigrationRS');
}

export const databaseMigrationRSProvider = {
    provide: DatabaseMigrationRS,
    useFactory: databaseMigrationRSProviderServiceFactory,
    deps: ['$injector']
};

// ZahlungRS
export function zahlungRSProviderServiceFactory(i: any) {
    return i.get('ZahlungRS');
}

export const zahlungRSProvider = {
    provide: ZahlungRS,
    useFactory: zahlungRSProviderServiceFactory,
    deps: ['$injector']
};

// GesuchRS
export function gesuchRSProviderServiceFactory(i: any) {
    return i.get('GesuchRS');
}

export const gesuchRSProvider = {
    provide: GesuchRS,
    useFactory: gesuchRSProviderServiceFactory,
    deps: ['$injector']
};

// DailyBatchRS
export function dailyBatchRSProviderServiceFactory(i: any) {
    return i.get('DailyBatchRS');
}

export const dailyBatchRSProvider = {
    provide: DailyBatchRS,
    useFactory: dailyBatchRSProviderServiceFactory,
    deps: ['$injector']
};

// GemeindeRS
export function gemeindeRSProviderServiceFactory(i: any) {
    return i.get('GemeindeRS');
}

export const gemeindeRSProvider = {
    provide: GemeindeRS,
    useFactory: gemeindeRSProviderServiceFactory,
    deps: ['$injector']
};
