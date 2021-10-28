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

import {LOCALE_ID} from '@angular/core';
import {downgradeComponent, downgradeInjectable} from '@angular/upgrade/static';
import * as angular from 'angular';
// tslint:disable:no-import-side-effect
import 'angular-animate';
import 'angular-aria';
import 'angular-cookies';
import 'angular-hotkeys';
import 'angular-i18n/angular-locale_de-ch';
import 'angular-material';
import 'angular-messages';
import 'angular-moment';
import 'angular-sanitize';
import 'angular-smart-table';
import 'angular-translate';
import 'angular-translate-loader-static-files';
import 'angular-ui-bootstrap';
import 'angular-unsavedchanges';
import 'angular-utf8-base64';
import 'ng-file-upload';
import 'raven-js';
import 'raven-js/plugins/angular';
import {BetreuungMonitoringRS} from '../../admin/service/betreuungMonitoringRS.rest';
// tslint:enable-no-import-side-effect
import {DatabaseMigrationRS} from '../../admin/service/databaseMigrationRS.rest';
import {EinstellungRS} from '../../admin/service/einstellungRS.rest';
import {AUTHENTICATION_JS_MODULE} from '../../authentication/authentication.module';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import router from '../../dvbModules/router/router.module';
import {environment} from '../../environments/environment';
import {InternePendenzenRS} from '../../gesuch/component/internePendenzenView/internePendenzenRS.rest';
import {BerechnungsManager} from '../../gesuch/service/berechnungsManager';
import {DokumenteRS} from '../../gesuch/service/dokumenteRS.rest';
import {DossierRS} from '../../gesuch/service/dossierRS.rest';
import {EinkommensverschlechterungContainerRS} from '../../gesuch/service/einkommensverschlechterungContainerRS.rest';
import {EinkommensverschlechterungInfoRS} from '../../gesuch/service/einkommensverschlechterungInfoRS.rest';
import {ExportRS} from '../../gesuch/service/exportRS.rest';
import {FallRS} from '../../gesuch/service/fallRS.rest';
import {FamiliensituationRS} from '../../gesuch/service/familiensituationRS.rest';
import {FinanzielleSituationRS} from '../../gesuch/service/finanzielleSituationRS.rest';
import {GemeindeRS} from '../../gesuch/service/gemeindeRS.rest';
import {GesuchGenerator} from '../../gesuch/service/gesuchGenerator';
import {GesuchModelManager} from '../../gesuch/service/gesuchModelManager';
import {GesuchRS} from '../../gesuch/service/gesuchRS.rest';
import {GlobalCacheService} from '../../gesuch/service/globalCacheService';
import {MahnungRS} from '../../gesuch/service/mahnungRS.rest';
import {SearchRS} from '../../gesuch/service/searchRS.rest';
import {SupportRS} from '../../gesuch/service/supportRS.rest';
import {WizardStepManager} from '../../gesuch/service/wizardStepManager';
import {WizardStepRS} from '../../gesuch/service/WizardStepRS.rest';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {BenutzerComponent} from '../benutzer/benutzer/benutzer.component';
import {DvLanguageSelectorComponentConfig} from '../i18n/components/dv-language-selector/dv-language-selector.component';
import {HttpI18nInterceptor} from '../i18n/httpInterceptor/http-i18n-Interceptor';
import {I18nServiceRSRest} from '../i18n/services/i18nServiceRS.rest';
import {PosteingangService} from '../posteingang/service/posteingang.service';
import {IeDeprecationWarningComponent} from '../shared/component/ie-deprecation-warning/ie-deprecation-warning.component';
import {ColorService} from '../shared/services/color.service';
import {MandantService} from '../shared/services/mandant.service';
import {DvAccordionComponentConfig} from './component/dv-accordion/dv-accordion';
import {DvAccordionTabComponentConfig} from './component/dv-accordion/dv-accordion-tab/dv-accordion-tab';
import {AdresseComponentConfig} from './component/dv-adresse/dv-adresse';
import {DVAntragListConfig} from './component/dv-antrag-list/dv-antrag-list';
import {DvBenutzerEntry} from './component/dv-benutzer-entry/dv-benutzer-entry';
import {DVBenutzerListConfig} from './component/dv-benutzer-list/dv-benutzer-list';
import {DvBisherComponentConfig} from './component/dv-bisher/dv-bisher';
import {DvCountdownComponentConfig} from './component/dv-countdown/dv-countdown';
import {DVDokumenteListConfig} from './component/dv-dokumente-list/dv-dokumente-list';
import {DvErrorMessagesComponentConfig} from './component/dv-error-messages/dv-error-messages';
import {DVErwerbspensumListConfig} from './component/dv-erwerbspensum-list/dv-erwerbspensum-list';
import {DvFooterComponentConfig} from './component/dv-footer/dv-footer';
import {DvHelpmenuComponent} from './component/dv-helpmenu/dv-helpmenu';
import {DvHomeIconComponentConfig} from './component/dv-home-icon/dv-home-icon';
import {DvInputContainerComponentConfig} from './component/dv-input-container/dv-input-container';
import {DVLoginButtonConfig} from './component/dv-login-button/dv-login-button';
import {DvMitteilungDelegationComponent} from './component/dv-mitteilung-delegation/dv-mitteilung-delegation';
import {DVMitteilungListConfig} from './component/dv-mitteilung-list/dv-mitteilung-list';
import {DvMobileNavigationToggleComponentConfig} from './component/dv-mobile-navigation-toggle/dv-mobile-navigation-toggle';
import {DvPulldownUserMenuComponentConfig} from './component/dv-pulldown-user-menu/dv-pulldown-user-menu';
import {DvRadioContainerComponentConfig} from './component/dv-radio-container/dv-radio-container';
import {DvQuicksearchboxComponentConfig} from './component/dv-search/dv-quicksearchbox/dv-quicksearchbox';
import {DvSearchResultIconComponentConfig} from './component/dv-search/dv-search-result-icon/dv-search-result-icon';
import {DvSkiplinksComponentConfig} from './component/dv-skiplinks/dv-skiplinks';
import {DvTooltipComponentConfig} from './component/dv-tooltip/dv-tooltip';
import {DVVersionComponentConfig} from './component/dv-version/dv-version';
import {DVVorlageListConfig} from './component/dv-vorlage-list/dv-vorlage-list';
import {NavbarComponent} from './component/navbar/navbar.component';
import {configure} from './config';
import {CONSTANTS} from './constants/CONSTANTS';
import {DVRoleElementController} from './controller/DVRoleElementController';
import {appRun} from './core.route';
import {DVBarcodeListener} from './directive/dv-barcode-listener';
import {DVDatepicker} from './directive/dv-datepicker/dv-datepicker';
import {DvDialog} from './directive/dv-dialog/dv-dialog';
import {DVDisplayElement} from './directive/dv-display-element/dv-display-element';
import {DVEnableElement} from './directive/dv-enable-element/dv-enable-element';
import {DVLoadingButton} from './directive/dv-loading-button/dv-loading-button';
import {DVLoading} from './directive/dv-loading/dv-loading';
import {DVMaxLength} from './directive/dv-max-length';
import {DVNavigation} from './directive/dv-navigation/dv-navigation';
import {DVShowElement} from './directive/dv-show-element/dv-show-element';
import {DVSTPersistAntraege} from './directive/dv-st-persist-antraege/dv-st-persist-antraege';
import {DVSTPersistPendenzen} from './directive/dv-st-persist-quicksearch/d-v-s-t-persist-pendenzen';
import {DVSTResetSearch} from './directive/dv-st-reset-search/dv-st-reset-search';
import {DVSuppressFormSubmitOnEnter} from './directive/dv-suppress-form-submit-on-enter/dv-suppress-form-submit-on-enter';
import {DVTimepicker} from './directive/dv-timepicker/dv-timepicker';
import {DVTrimEmpty} from './directive/dv-trim-empty/dv-trim-empty';
import {DvUserSelectConfig} from './directive/dv-userselect/dv-userselect';
import {DVValueinput} from './directive/dv-valueinput/dv-valueinput';
import {DvVerantwortlicherselect} from './directive/dv-verantwortlicherselect/dv-verantwortlicherselect';
import {DvSearchListComponent} from './dv-search-list/dv-search-list.component';
import {ERRORS_JS_MODULE} from './errors/errors';
import {ErrorServiceX} from './errors/service/ErrorServiceX';
import {arrayToString} from './filters/array-to-string.filter';
import {gemeindenToString} from './filters/gemeinden-to-string.filter';
import {NewAntragListComponent} from './new-antrag-list/new-antrag-list.component';
import {ApplicationPropertyRS} from './rest-services/applicationPropertyRS.rest';
import {AdresseRS} from './service/adresseRS.rest';
import {AntragStatusHistoryRS} from './service/antragStatusHistoryRS.rest';
import {BenutzerRSX} from './service/benutzerRSX.rest';
import {BetreuungRS} from './service/betreuungRS.rest';
import {BroadcastService} from './service/broadcast.service';
import {DownloadRS} from './service/downloadRS.rest';
import {DVsTPersistService} from './service/dVsTPersistService';
import {ErwerbspensumRS} from './service/erwerbspensumRS.rest';
import {EwkRS} from './service/ewkRS.rest';
import {FachstelleRS} from './service/fachstelleRS.rest';
import {GesuchsperiodeRS} from './service/gesuchsperiodeRS.rest';
import {GesuchstellerRS} from './service/gesuchstellerRS.rest';
import {HttpResponseInterceptor} from './service/HttpResponseInterceptor';
import {InstitutionRS} from './service/institutionRS.rest';
import {InstitutionStammdatenRS} from './service/institutionStammdatenRS.rest';
import {KindRS} from './service/kindRS.rest';
import {ListResourceRS} from './service/listResourceRS.rest';
import {MandantRS} from './service/mandantRS.rest';
import {MitteilungRS} from './service/mitteilungRS.rest';
import {NotrechtRS} from './service/notrechtRS.rest';
import {ReportRS} from './service/reportRS.rest';
import {SearchIndexRS} from './service/searchIndexRS.rest';
import {SozialdienstRS} from './service/SozialdienstRS.rest';
import {SozialhilfeZeitraumRS} from './service/sozialhilfeZeitraumRS.rest';
import {TraegerschaftRS} from './service/traegerschaftRS.rest';
import {UploadRS} from './service/uploadRS.rest';
import {VerfuegungRS} from './service/verfuegungRS.rest';
import {HttpVersionInterceptor} from './service/version/HttpVersionInterceptor';
import {VersionService} from './service/version/version.service';
import {WizardStepXRS} from './service/wizardStepXRS.rest';

const dependencies = [
    /* Angular modules */
    'ngAnimate',
    'ngSanitize',
    'ngMessages',
    'ngAria',
    'ngCookies',
    /* shared DVBern modules */
    router.name,
    ERRORS_JS_MODULE.name,
    AUTHENTICATION_JS_MODULE.name,
    /* 3rd-party modules */
    'ui.bootstrap',
    'smart-table',
    'ngMaterial',
    'pascalprecht.translate',
    'angularMoment',
    'cfp.hotkeys',
    'ngFileUpload',
    'unsavedChanges',
    'utf8-base64',
];

const dynamicDependencies = (): string[] => {

    // hier kommen plugins die wir fuer dev disablen wollen
    if (environment.sentryDSN) {
        return ['ngRaven'];
    }
    return [];
};

const calculatedDeps = dependencies.concat(dynamicDependencies());

export const CORE_JS_MODULE = angular
    .module('ebeguWeb.core', calculatedDeps)
    .run(appRun)
    .config(configure)
    .constant('REST_API', '/ebegu/api/v1/')
    .constant('CONSTANTS', CONSTANTS)
    .factory('LOCALE_ID', downgradeInjectable(LOCALE_ID))
    .service('ApplicationPropertyRS', ApplicationPropertyRS)
    .service('EbeguRestUtil', EbeguRestUtil)
    .service('EbeguUtil', EbeguUtil)
    .service('GesuchstellerRS', GesuchstellerRS)
    .service('AdresseRS', AdresseRS)
    .service('ListResourceRS', ListResourceRS)
    .service('FallRS', FallRS)
    .service('FamiliensituationRS', FamiliensituationRS)
    .service('GesuchModelManager', GesuchModelManager)
    .service('GesuchRS', GesuchRS)
    .service('SearchRS', SearchRS)
    .service('FinanzielleSituationRS', FinanzielleSituationRS)
    .service('EinkommensverschlechterungContainerRS', EinkommensverschlechterungContainerRS)
    .service('EinkommensverschlechterungInfoRS', EinkommensverschlechterungInfoRS)
    .service('MandantRS', MandantRS)
    .service('TraegerschaftRS', TraegerschaftRS)
    .service('InstitutionRS', InstitutionRS)
    .service('InstitutionStammdatenRS', InstitutionStammdatenRS)
    .service('ErwerbspensumRS', ErwerbspensumRS)
    .service('KindRS', KindRS)
    .service('DvDialog', DvDialog)
    .service('BetreuungRS', BetreuungRS)
    .service('GesuchsperiodeRS', GesuchsperiodeRS)
    .service('VerfuegungRS', VerfuegungRS)
    .service('DokumenteRS', DokumenteRS)
    .service('UploadRS', UploadRS)
    .service('DownloadRS', DownloadRS)
    .service('WizardStepRS', WizardStepRS)
    .service('AntragStatusHistoryRS', AntragStatusHistoryRS)
    .service('MitteilungRS', MitteilungRS)
    .service('GlobalCacheService', GlobalCacheService)
    .service('ExportRS', ExportRS)
    .service('DossierRS', DossierRS)
    .service('GemeindeRS', GemeindeRS)
    .service('NotrechtRS', NotrechtRS)
    .service('EinstellungRS', EinstellungRS)
    .service('SozialhilfeZeitraumRS', SozialhilfeZeitraumRS)
    .service('BetreuungMonitoringRS', BetreuungMonitoringRS)
    .factory('PosteingangService', downgradeInjectable(PosteingangService) as any)
    .factory('AuthLifeCycleService', downgradeInjectable(AuthLifeCycleService) as any)
    .factory('GesuchGenerator', downgradeInjectable(GesuchGenerator) as any)
    .factory('I18nServiceRSRest', downgradeInjectable(I18nServiceRSRest) as any)
    .factory('SozialdienstRS', downgradeInjectable(SozialdienstRS) as any)
    .factory('InternePendenzenRS', downgradeInjectable(InternePendenzenRS) as any)
    .factory('VersionService', downgradeInjectable(VersionService) as any)
    .factory('BroadcastService', downgradeInjectable(BroadcastService) as any)
    .factory('BenutzerRS', downgradeInjectable(BenutzerRSX) as any)
    .factory('ErrorServiceX', downgradeInjectable(ErrorServiceX) as any)
    .factory('MandantService', downgradeInjectable(MandantService) as any)
    .factory('ColorService', downgradeInjectable(ColorService) as any)
    .directive('dvMaxLength', DVMaxLength.factory())
    .directive('dvDatepicker', DVDatepicker.factory())
    .directive('dvTimepicker', DVTimepicker.factory())
    .directive('dvValueinput', DVValueinput.factory())
    .directive('dvVerantwortlicherselect', DvVerantwortlicherselect.factory())
    .directive('dvNavigation', DVNavigation.factory())
    .directive('dvLoading', DVLoading.factory())
    .directive('dvStPersistAntraege', DVSTPersistAntraege.factory())
    .directive('dvStPersistPendenzen', DVSTPersistPendenzen.factory())
    .directive('dvStResetSearch', DVSTResetSearch.factory())
    .directive('dvShowElement', DVShowElement.factory())
    .directive('dvDisplayElement', DVDisplayElement.factory())
    .directive('dvEnableElement', DVEnableElement.factory())
    .directive('dvBarcodeListener', DVBarcodeListener.factory())
    .directive('dvTrimEmpty', DVTrimEmpty.factory())
    .directive('dvSuppressFormSubmitOnEnter', DVSuppressFormSubmitOnEnter.factory())
    .service('FachstelleRS', FachstelleRS)
    .service('BerechnungsManager', BerechnungsManager)
    .service('HttpResponseInterceptor', HttpResponseInterceptor)
    .service('HttpVersionInterceptor', HttpVersionInterceptor)
    .service('HttpI18nInterceptor', HttpI18nInterceptor)
    .service('WizardStepManager', WizardStepManager)
    .service('SearchIndexRS', SearchIndexRS)
    .service('DVsTPersistService', DVsTPersistService)
    .service('applicationPropertyRS', ApplicationPropertyRS)
    .factory('WizardStepXRS', downgradeInjectable(WizardStepXRS) as any)
    .controller('DVElementController', DVRoleElementController)
    .component('dvLoadingButton', new DVLoadingButton())
    .component('dvAdresse', new AdresseComponentConfig())
    .component('dvLanguageSelector', new DvLanguageSelectorComponentConfig())
    .component('dvErrorMessages', new DvErrorMessagesComponentConfig())
    .component('dvErwerbspensumList', new DVErwerbspensumListConfig())
    .component('dvRadioContainer', new DvRadioContainerComponentConfig())
    .component('dvTooltip', new DvTooltipComponentConfig())
    .component('dvPulldownUserMenu', new DvPulldownUserMenuComponentConfig())
    .component('dvMobileNavigationToggle', new DvMobileNavigationToggleComponentConfig())
    .component('dvHomeIcon', new DvHomeIconComponentConfig())
    .component('dvSkiplinks', new DvSkiplinksComponentConfig())
    .component('dvCountdown', new DvCountdownComponentConfig())
    .component('dvUserselect', new DvUserSelectConfig())
    .component('dvBisher', new DvBisherComponentConfig())
    .component('dvDokumenteList', new DVDokumenteListConfig())
    .component('dvAntragList', new DVAntragListConfig())
    .component('dvVorlageList', new DVVorlageListConfig())
    .component('dvQuicksearchbox', new DvQuicksearchboxComponentConfig())
    .component('dvSearchResultIcon', new DvSearchResultIconComponentConfig())
    .component('dvMitteilungList', new DVMitteilungListConfig())
    .component('dvAccordion', new DvAccordionComponentConfig())
    .component('dvAccordionTab', new DvAccordionTabComponentConfig())
    .component('dvVersion', new DVVersionComponentConfig())
    .component('dvBenutzerList', new DVBenutzerListConfig())
    .component('dvLoginButton', new DVLoginButtonConfig())
    .component('dvFooter', new DvFooterComponentConfig())
    .component('dvInputContainer', new DvInputContainerComponentConfig())
    .directive('dvNewAntragList', downgradeComponent({component: NewAntragListComponent}))
    .directive('dvHelpmenu', downgradeComponent({component: DvHelpmenuComponent}))
    .directive('dvMitteilungDelegation', downgradeComponent({component: DvMitteilungDelegationComponent}))
    .directive('dvBenutzerEntry', downgradeComponent({component: DvBenutzerEntry}))
    .directive('dvNavbar', downgradeComponent({component: NavbarComponent}))
    .directive('dvBenutzer', downgradeComponent({component: BenutzerComponent}))
    .directive('dvSearchList', downgradeComponent({component: DvSearchListComponent}))
    .directive('dvIeDeprecationWarning', downgradeComponent({component: IeDeprecationWarningComponent}))
    .service('MahnungRS', MahnungRS)
    .service('ReportRS', ReportRS)
    .service('EwkRS', EwkRS)
    .service('DatabaseMigrationRS', DatabaseMigrationRS)
    .service('SupportRS', SupportRS)
    .filter('arrayToString', () => arrayToString)
    .filter('gemeindenToString', () => gemeindenToString);
