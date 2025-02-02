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

import {Provider} from '@angular/core';
import {DatabaseMigrationRS} from '../../admin/service/databaseMigrationRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {BerechnungsManager} from '../../gesuch/service/berechnungsManager';
import {DossierRS} from '../../gesuch/service/dossierRS.rest';
import {EinkommensverschlechterungContainerRS} from '../../gesuch/service/einkommensverschlechterungContainerRS.rest';
import {EinkommensverschlechterungInfoRS} from '../../gesuch/service/einkommensverschlechterungInfoRS.rest';
import {FallRS} from '../../gesuch/service/fallRS.rest';
import {FinanzielleSituationRS} from '../../gesuch/service/finanzielleSituationRS.rest';
import {FinanzielleSituationSubStepManager} from '../../gesuch/service/finanzielleSituationSubStepManager';
import {GemeindeRS} from '../../gesuch/service/gemeindeRS.rest';
import {GesuchModelManager} from '../../gesuch/service/gesuchModelManager';
import {GesuchRS} from '../../gesuch/service/gesuchRS.rest';
import {GlobalCacheService} from '../../gesuch/service/globalCacheService';
import {SupportRS} from '../../gesuch/service/supportRS.rest';
import {WizardStepManager} from '../../gesuch/service/wizardStepManager';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {ErrorService} from './errors/service/ErrorService';
import {ApplicationPropertyRS} from './rest-services/applicationPropertyRS.rest';
import {AntragStatusHistoryRS} from './service/antragStatusHistoryRS.rest';
import {DownloadRS} from './service/downloadRS.rest';
import {GesuchsperiodeRS} from './service/gesuchsperiodeRS.rest';
import {GesuchstellerRS} from './service/gesuchstellerRS.rest';
import {InstitutionStammdatenRS} from './service/institutionStammdatenRS.rest';
import {ListResourceRS} from './service/listResourceRS.rest';
import {MitteilungRS} from './service/mitteilungRS.rest';
import {NotrechtRS} from './service/notrechtRS.rest';
import {ReportRS} from './service/reportRS.rest';
import {TraegerschaftRS} from './service/traegerschaftRS.rest';
import {UploadRS} from './service/uploadRS.rest';
import IInjectorService = angular.auto.IInjectorService;

/* eslint-disable */

// AuthServiceRS
export function authServiceRSServiceFactory(
    i: IInjectorService
): AuthServiceRS {
    return i.get('AuthServiceRS');
}

export const authServiceRSProvider = {
    provide: AuthServiceRS,
    useFactory: authServiceRSServiceFactory,
    deps: ['$injector']
};

// ApplicationPropertyRS
export function applicationPropertyRSServiceFactory(
    i: IInjectorService
): ApplicationPropertyRS {
    return i.get('ApplicationPropertyRS');
}

export const applicationPropertyRSProvider = {
    provide: ApplicationPropertyRS,
    useFactory: applicationPropertyRSServiceFactory,
    deps: ['$injector']
};

// TraegerschaftRS
export function traegerschaftRSProviderServiceFactory(
    i: IInjectorService
): TraegerschaftRS {
    return i.get('TraegerschaftRS');
}

export const traegerschaftRSProvider = {
    provide: TraegerschaftRS,
    useFactory: traegerschaftRSProviderServiceFactory,
    deps: ['$injector']
};

// ErrorService
export function errorServiceProviderServiceFactory(
    i: IInjectorService
): ErrorService {
    return i.get('ErrorService');
}

export const errorServiceProvider = {
    provide: ErrorService,
    useFactory: errorServiceProviderServiceFactory,
    deps: ['$injector']
};

// GesuchsperiodeRS
export function gesuchsperiodeRSProviderServiceFactory(
    i: IInjectorService
): GesuchsperiodeRS {
    return i.get('GesuchsperiodeRS');
}

export const gesuchsperiodeRSProvider = {
    provide: GesuchsperiodeRS,
    useFactory: gesuchsperiodeRSProviderServiceFactory,
    deps: ['$injector']
};

// DatabaseMigrationRS
export function databaseMigrationRSProviderServiceFactory(
    i: IInjectorService
): DatabaseMigrationRS {
    return i.get('DatabaseMigrationRS');
}

export const databaseMigrationRSProvider = {
    provide: DatabaseMigrationRS,
    useFactory: databaseMigrationRSProviderServiceFactory,
    deps: ['$injector']
};

// GesuchRS
export function gesuchRSProviderServiceFactory(i: IInjectorService): GesuchRS {
    return i.get('GesuchRS');
}

export const gesuchRSProvider = {
    provide: GesuchRS,
    useFactory: gesuchRSProviderServiceFactory,
    deps: ['$injector']
};

// GemeindeRS
export function gemeindeRSProviderServiceFactory(
    i: IInjectorService
): GemeindeRS {
    return i.get('GemeindeRS');
}

export const gemeindeRSProvider = {
    provide: GemeindeRS,
    useFactory: gemeindeRSProviderServiceFactory,
    deps: ['$injector']
};

// MitteilungRS
export function mitteilungRSServiceFactory(i: IInjectorService): MitteilungRS {
    return i.get('MitteilungRS');
}

export const mitteilungRSProvider = {
    provide: MitteilungRS,
    useFactory: mitteilungRSServiceFactory,
    deps: ['$injector']
};

// DownloadRS
export function downloadRSServiceFactory(i: IInjectorService): DownloadRS {
    return i.get('DownloadRS');
}

export const downloadRSProvider = {
    provide: DownloadRS,
    useFactory: downloadRSServiceFactory,
    deps: ['$injector']
};

// DossierRS
export function dossierRSServiceFactory(i: IInjectorService): DossierRS {
    return i.get('DossierRS');
}

export const dossierRSProvider = {
    provide: DossierRS,
    useFactory: dossierRSServiceFactory,
    deps: ['$injector']
};

// AntragStatusHistoryRS
export function antragStatusHistoryRSServiceFactory(
    i: IInjectorService
): AntragStatusHistoryRS {
    return i.get('AntragStatusHistoryRS');
}

export const antragStatusHistoryRSProvider = {
    provide: AntragStatusHistoryRS,
    useFactory: antragStatusHistoryRSServiceFactory,
    deps: ['$injector']
};

// WizardStepManager
export function wizardStepManagerServiceFactory(
    i: IInjectorService
): WizardStepManager {
    return i.get('WizardStepManager');
}

export const wizardStepManagerProvider = {
    provide: WizardStepManager,
    useFactory: wizardStepManagerServiceFactory,
    deps: ['$injector']
};

// WizardSubStepManager
export function wizardSubStepManagerServiceFactory(
    i: IInjectorService
): FinanzielleSituationSubStepManager {
    return i.get('WizardSubStepManager');
}

export const wizardSubStepManagerProvider = {
    provide: FinanzielleSituationSubStepManager,
    useFactory: wizardSubStepManagerServiceFactory,
    deps: ['$injector']
};

// FallRS
export function fallRSServiceFactory(i: IInjectorService): FallRS {
    return i.get('FallRS');
}

export const fallRSProvider = {
    provide: FallRS,
    useFactory: fallRSServiceFactory,
    deps: ['$injector']
};

// InstitutionStammdatenRS
export function institutionStammdatenRSFactory(
    i: IInjectorService
): InstitutionStammdatenRS {
    return i.get('InstitutionStammdatenRS');
}

export const institutionStammdatenRSProvider = {
    provide: InstitutionStammdatenRS,
    useFactory: institutionStammdatenRSFactory,
    deps: ['$injector']
};

// SupportRS
export function supportRSServiceFactory(i: IInjectorService): SupportRS {
    return i.get('SupportRS');
}

export const supportRSProvider = {
    provide: SupportRS,
    useFactory: supportRSServiceFactory,
    deps: ['$injector']
};

// NotrechtRS
export function notrechtRSProviderServiceFactory(
    i: IInjectorService
): NotrechtRS {
    return i.get('NotrechtRS');
}

export const notrechtRSProvider = {
    provide: NotrechtRS,
    useFactory: notrechtRSProviderServiceFactory,
    deps: ['$injector']
};

// UploadRS
export function uploadRSServiceFactory(i: IInjectorService): UploadRS {
    return i.get('UploadRS');
}

export const uploadRSProvider = {
    provide: UploadRS,
    useFactory: uploadRSServiceFactory,
    deps: ['$injector']
};
// GesuchModelManager
export function gesuchModelManagerFactory(
    i: IInjectorService
): GesuchModelManager {
    return i.get('GesuchModelManager');
}

export const gesuchModelManagerProvider = {
    provide: GesuchModelManager,
    useFactory: gesuchModelManagerFactory,
    deps: ['$injector']
};

// ExportRS
export function reportRSFactory(i: IInjectorService): ReportRS {
    return i.get('ReportRS');
}

export const reportRSProvider = {
    provide: ReportRS,
    useFactory: reportRSFactory,
    deps: ['$injector']
};

// EbeguUtil
export function ebeguUtilFactory(i: IInjectorService): EbeguUtil {
    return i.get('EbeguUtil');
}

export const ebeguUtilProvider = {
    provide: EbeguUtil,
    useFactory: ebeguUtilFactory,
    deps: ['$injector']
};

// FinanzielleSituationRS
export function finanzielleSituationRSServiceFactory(
    i: IInjectorService
): FinanzielleSituationRS {
    return i.get('FinanzielleSituationRS');
}

export const finanzielleSituationRSProvider = {
    provide: FinanzielleSituationRS,
    useFactory: finanzielleSituationRSServiceFactory,
    deps: ['$injector']
};

// BerechnungsManager
export function berechnungsManagerFactory(
    i: IInjectorService
): BerechnungsManager {
    return i.get('BerechnungsManager');
}

export const berechnungsManagerProvider = {
    provide: BerechnungsManager,
    useFactory: berechnungsManagerFactory,
    deps: ['$injector']
};

// BerechnungsManager
export function listResourceRSFactory(i: IInjectorService): ListResourceRS {
    return i.get('ListResourceRS');
}

export const listResourceRSProvider = {
    provide: ListResourceRS,
    useFactory: listResourceRSFactory,
    deps: ['$injector']
};

// BerechnungsManager
export function gesuchstellerRSFactory(i: IInjectorService): GesuchstellerRS {
    return i.get('GesuchstellerRS');
}

export const gesuchstellerRSProvider = {
    provide: GesuchstellerRS,
    useFactory: gesuchstellerRSFactory,
    deps: ['$injector']
};

// Global Cache Service
export function globalCacheServiceFactory(
    i: IInjectorService
): GlobalCacheService {
    return i.get('GlobalCacheService');
}

export const globalCacheServiceProvider = {
    provide: GlobalCacheService,
    useFactory: globalCacheServiceFactory,
    deps: ['$injector']
};

// EKV Container RS
export function ekvContainerRSServiceFactory(
    i: IInjectorService
): EinkommensverschlechterungContainerRS {
    return i.get('EinkommensverschlechterungContainerRS');
}

export const ekvContainerRSServiceProvider = {
    provide: EinkommensverschlechterungContainerRS,
    useFactory: ekvContainerRSServiceFactory,
    deps: ['$injector']
};

// EKV Info RS
export function ekvInfoRSFactory(
    i: IInjectorService
): EinkommensverschlechterungInfoRS {
    return i.get('EinkommensverschlechterungInfoRS');
}

export const ekvInfoRSProvider = {
    provide: EinkommensverschlechterungInfoRS,
    useFactory: ekvInfoRSFactory,
    deps: ['$injector']
};

export const UPGRADED_PROVIDERS: Provider[] = [
    authServiceRSProvider,
    applicationPropertyRSProvider,
    traegerschaftRSProvider,
    errorServiceProvider,
    gesuchsperiodeRSProvider,
    databaseMigrationRSProvider,
    gesuchRSProvider,
    gemeindeRSProvider,
    mitteilungRSProvider,
    downloadRSProvider,
    dossierRSProvider,
    antragStatusHistoryRSProvider,
    wizardStepManagerProvider,
    fallRSProvider,
    institutionStammdatenRSProvider,
    supportRSProvider,
    uploadRSProvider,
    notrechtRSProvider,
    gesuchModelManagerProvider,
    reportRSProvider,
    ebeguUtilProvider,
    finanzielleSituationRSProvider,
    berechnungsManagerProvider,
    listResourceRSProvider,
    gesuchstellerRSProvider,
    globalCacheServiceProvider,
    ekvContainerRSServiceProvider,
    ekvInfoRSProvider
];
