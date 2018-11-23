/*
 * AGPL File-Header
 *
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

import {Provider} from '@angular/core';
import {DailyBatchRS} from '../../admin/service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../../admin/service/databaseMigrationRS.rest';
import {EinstellungRS} from '../../admin/service/einstellungRS.rest';
import {TestFaelleRS} from '../../admin/service/testFaelleRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import DossierRS from '../../gesuch/service/dossierRS.rest';
import FallRS from '../../gesuch/service/fallRS.rest';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import GesuchRS from '../../gesuch/service/gesuchRS.rest';
import WizardStepManager from '../../gesuch/service/wizardStepManager';
import ErrorService from './errors/service/ErrorService';
import {ApplicationPropertyRS} from './rest-services/applicationPropertyRS.rest';
import AntragStatusHistoryRS from './service/antragStatusHistoryRS.rest';
import BenutzerRS from './service/benutzerRS.rest';
import {DownloadRS} from './service/downloadRS.rest';
import GesuchsperiodeRS from './service/gesuchsperiodeRS.rest';
import {InstitutionRS} from './service/institutionRS.rest';
import {InstitutionStammdatenRS} from './service/institutionStammdatenRS.rest';
import MitteilungRS from './service/mitteilungRS.rest';
import {TraegerschaftRS} from './service/traegerschaftRS.rest';
import ZahlungRS from './service/zahlungRS.rest';
import IInjectorService = angular.auto.IInjectorService;

// tslint:disable:naming-convention

// AuthServiceRS
export function authServiceRSServiceFactory(i: IInjectorService): AuthServiceRS {
    return i.get('AuthServiceRS');
}

export const authServiceRSProvider = {
    provide: AuthServiceRS,
    useFactory: authServiceRSServiceFactory,
    deps: ['$injector'],
};

// ApplicationPropertyRS
export function applicationPropertyRSServiceFactory(i: IInjectorService): ApplicationPropertyRS {
    return i.get('ApplicationPropertyRS');
}

export const applicationPropertyRSProvider = {
    provide: ApplicationPropertyRS,
    useFactory: applicationPropertyRSServiceFactory,
    deps: ['$injector'],
};

// TraegerschaftRS
export function traegerschaftRSProviderServiceFactory(i: IInjectorService): TraegerschaftRS {
    return i.get('TraegerschaftRS');
}

export const traegerschaftRSProvider = {
    provide: TraegerschaftRS,
    useFactory: traegerschaftRSProviderServiceFactory,
    deps: ['$injector'],
};

// ErrorService
export function errorServiceProviderServiceFactory(i: IInjectorService): ErrorService {
    return i.get('ErrorService');
}

export const errorServiceProvider = {
    provide: ErrorService,
    useFactory: errorServiceProviderServiceFactory,
    deps: ['$injector'],
};

// TestFaelleRS
export function testFaelleRSProviderServiceFactory(i: IInjectorService): TestFaelleRS {
    return i.get('TestFaelleRS');
}

export const testFaelleRSProvider = {
    provide: TestFaelleRS,
    useFactory: testFaelleRSProviderServiceFactory,
    deps: ['$injector'],
};

// UserRS
export function benutzerRSProviderServiceFactory(i: IInjectorService): BenutzerRS {
    return i.get('BenutzerRS');
}

export const benutzerRSProvider = {
    provide: BenutzerRS,
    useFactory: benutzerRSProviderServiceFactory,
    deps: ['$injector'],
};

// GesuchsperiodeRS
export function gesuchsperiodeRSProviderServiceFactory(i: IInjectorService): GesuchsperiodeRS {
    return i.get('GesuchsperiodeRS');
}

export const gesuchsperiodeRSProvider = {
    provide: GesuchsperiodeRS,
    useFactory: gesuchsperiodeRSProviderServiceFactory,
    deps: ['$injector'],
};

// DatabaseMigrationRS
export function databaseMigrationRSProviderServiceFactory(i: IInjectorService): DatabaseMigrationRS {
    return i.get('DatabaseMigrationRS');
}

export const databaseMigrationRSProvider = {
    provide: DatabaseMigrationRS,
    useFactory: databaseMigrationRSProviderServiceFactory,
    deps: ['$injector'],
};

// ZahlungRS
export function zahlungRSProviderServiceFactory(i: IInjectorService): ZahlungRS {
    return i.get('ZahlungRS');
}

export const zahlungRSProvider = {
    provide: ZahlungRS,
    useFactory: zahlungRSProviderServiceFactory,
    deps: ['$injector'],
};

// GesuchRS
export function gesuchRSProviderServiceFactory(i: IInjectorService): GesuchRS {
    return i.get('GesuchRS');
}

export const gesuchRSProvider = {
    provide: GesuchRS,
    useFactory: gesuchRSProviderServiceFactory,
    deps: ['$injector'],
};

// DailyBatchRS
export function dailyBatchRSProviderServiceFactory(i: IInjectorService): DailyBatchRS {
    return i.get('DailyBatchRS');
}

export const dailyBatchRSProvider = {
    provide: DailyBatchRS,
    useFactory: dailyBatchRSProviderServiceFactory,
    deps: ['$injector'],
};

// GemeindeRS
export function gemeindeRSProviderServiceFactory(i: IInjectorService): GemeindeRS {
    return i.get('GemeindeRS');
}

export const gemeindeRSProvider = {
    provide: GemeindeRS,
    useFactory: gemeindeRSProviderServiceFactory,
    deps: ['$injector'],
};

// MitteilungRS
export function mitteilungRSServiceFactory(i: IInjectorService): MitteilungRS {
    return i.get('MitteilungRS');
}

export const mitteilungRSProvider = {
    provide: MitteilungRS,
    useFactory: mitteilungRSServiceFactory,
    deps: ['$injector'],
};

// DownloadRS
export function downloadRSServiceFactory(i: IInjectorService): DownloadRS {
    return i.get('DownloadRS');
}

export const downloadRSProvider = {
    provide: DownloadRS,
    useFactory: downloadRSServiceFactory,
    deps: ['$injector'],
};

// DossierRS
export function dossierRSServiceFactory(i: IInjectorService): DossierRS {
    return i.get('DossierRS');
}

export const dossierRSProvider = {
    provide: DossierRS,
    useFactory: dossierRSServiceFactory,
    deps: ['$injector'],
};

// AntragStatusHistoryRS
export function antragStatusHistoryRSServiceFactory(i: IInjectorService): AntragStatusHistoryRS {
    return i.get('AntragStatusHistoryRS');
}

export const antragStatusHistoryRSProvider = {
    provide: AntragStatusHistoryRS,
    useFactory: antragStatusHistoryRSServiceFactory,
    deps: ['$injector'],
};

// WizardStepManager
export function wizardStepManagerServiceFactory(i: IInjectorService): WizardStepManager {
    return i.get('WizardStepManager');
}

export const wizardStepManagerProvider = {
    provide: WizardStepManager,
    useFactory: wizardStepManagerServiceFactory,
    deps: ['$injector'],
};

// FallRS
export function fallRSServiceFactory(i: IInjectorService): FallRS {
    return i.get('FallRS');
}

export const fallRSProvider = {
    provide: FallRS,
    useFactory: fallRSServiceFactory,
    deps: ['$injector'],
};

// InstitutionRS
export function institutionRSFactory(i: IInjectorService): InstitutionRS {
    return i.get('InstitutionRS');
}

export const institutionRSProvider = {
    provide: InstitutionRS,
    useFactory: institutionRSFactory,
    deps: ['$injector'],
};

// InstitutionStammdatenRS
export function institutionStammdatenRSFactory(i: IInjectorService): InstitutionStammdatenRS {
    return i.get('InstitutionStammdatenRS');
}

export const institutionStammdatenRSProvider = {
    provide: InstitutionStammdatenRS,
    useFactory: institutionStammdatenRSFactory,
    deps: ['$injector'],
};

// EinstellungRS
export function einstellungRSServiceFactory(i: IInjectorService): EinstellungRS {
    return i.get('EinstellungRS');
}

export const einstellungRSProvider = {
    provide: EinstellungRS,
    useFactory: einstellungRSServiceFactory,
    deps: ['$injector'],
};

export const UPGRADED_PROVIDERS: Provider[] = [
    authServiceRSProvider,
    applicationPropertyRSProvider,
    traegerschaftRSProvider,
    errorServiceProvider,
    testFaelleRSProvider,
    benutzerRSProvider,
    gesuchsperiodeRSProvider,
    databaseMigrationRSProvider,
    zahlungRSProvider,
    gesuchRSProvider,
    dailyBatchRSProvider,
    gemeindeRSProvider,
    mitteilungRSProvider,
    downloadRSProvider,
    dossierRSProvider,
    antragStatusHistoryRSProvider,
    wizardStepManagerProvider,
    fallRSProvider,
    institutionRSProvider,
    institutionStammdatenRSProvider,
    einstellungRSProvider,
];
