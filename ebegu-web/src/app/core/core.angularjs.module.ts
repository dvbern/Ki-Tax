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
import 'ng-file-upload';
import {DatabaseMigrationRS} from '../../admin/service/databaseMigrationRS.rest';
import {EbeguAuthentication} from '../../authentication/authentication.module';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import router from '../../dvbModules/router/router.module';
import BerechnungsManager from '../../gesuch/service/berechnungsManager';
import DokumenteRS from '../../gesuch/service/dokumenteRS.rest';
import DossierRS from '../../gesuch/service/dossierRS.rest';
import EinkommensverschlechterungContainerRS from '../../gesuch/service/einkommensverschlechterungContainerRS.rest';
import EinkommensverschlechterungInfoRS from '../../gesuch/service/einkommensverschlechterungInfoRS.rest';
import ExportRS from '../../gesuch/service/exportRS.rest';
import FallRS from '../../gesuch/service/fallRS.rest';
import FamiliensituationRS from '../../gesuch/service/familiensituationRS.rest';
import FinanzielleSituationRS from '../../gesuch/service/finanzielleSituationRS.rest';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import {GesuchGenerator} from '../../gesuch/service/gesuchGenerator';
import GesuchModelManager from '../../gesuch/service/gesuchModelManager';
import GesuchRS from '../../gesuch/service/gesuchRS.rest';
import GlobalCacheService from '../../gesuch/service/globalCacheService';
import MahnungRS from '../../gesuch/service/mahnungRS.rest';
import SearchRS from '../../gesuch/service/searchRS.rest';
import WizardStepManager from '../../gesuch/service/wizardStepManager';
import WizardStepRS from '../../gesuch/service/WizardStepRS.rest';
import {PosteingangService} from '../../posteingang/service/posteingang.service';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import EbeguUtil from '../../utils/EbeguUtil';
import {BenutzerComponent} from '../benutzer/benutzer/benutzer.component';
import {DvAccordionComponentConfig} from './component/dv-accordion/dv-accordion';
import {DvAccordionTabComponentConfig} from './component/dv-accordion/dv-accordion-tab/dv-accordion-tab';
import {AdresseComponentConfig} from './component/dv-adresse/dv-adresse';
import {DVAntragListConfig} from './component/dv-antrag-list/dv-antrag-list';
import {DVBenutzerListConfig} from './component/dv-benutzer-list/dv-benutzer-list';
import {DvBisherComponentConfig} from './component/dv-bisher/dv-bisher';
import {DvCountdownComponentConfig} from './component/dv-countdown/dv-countdown';
import {DVDokumenteListConfig} from './component/dv-dokumente-list/dv-dokumente-list';
import {DvErrorMessagesComponentConfig} from './component/dv-error-messages/dv-error-messages';
import {DVErwerbspensumListConfig} from './component/dv-erwerbspensum-list/dv-erwerbspensum-list';
import {DvHelpmenuComponent} from './component/dv-helpmenu/dv-helpmenu';
import {DvHomeIconComponentConfig} from './component/dv-home-icon/dv-home-icon';
import {DvInputContainerComponentConfig} from './component/dv-input-container/dv-input-container';
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
import DVMaxLength from './directive/dv-max-length';
import {DVNavigation} from './directive/dv-navigation/dv-navigation';
import {DVShowElement} from './directive/dv-show-element/dv-show-element';
import DVSTPersistAntraege from './directive/dv-st-persist-antraege/dv-st-persist-antraege';
import DVSTPersistPendenzen from './directive/dv-st-persist-quicksearch/dv-st-persist-quicksearch';
import DVSTResetSearch from './directive/dv-st-reset-search/dv-st-reset-search';
import DVSubmitevent from './directive/dv-submitevent/dv-submitevent';
import DVSupressFormSubmitOnEnter from './directive/dv-suppress-form-submit-on-enter/dv-suppress-form-submit-on-enter';
import {DVTimepicker} from './directive/dv-timepicker/dv-timepicker';
import DVTrimEmpty from './directive/dv-trim-empty/dv-trim-empty';
import {DVUserselect} from './directive/dv-userselect/dv-userselect';
import {DVValueinput} from './directive/dv-valueinput/dv-valueinput';
import {DvVerantwortlicherselect} from './directive/dv-verantwortlicherselect/dv-verantwortlicherselect';
import {EbeguErrors} from './errors/errors';
import {arrayToString} from './filters/array-to-string.filter';
import {gemeindenToString} from './filters/gemeinden-to-string.filter';
import {ApplicationPropertyRS} from './rest-services/applicationPropertyRS.rest';
import AdresseRS from './service/adresseRS.rest';
import AntragStatusHistoryRS from './service/antragStatusHistoryRS.rest';
import BatchJobRS from './service/batchRS.rest';
import BetreuungRS from './service/betreuungRS.rest';
import {DownloadRS} from './service/downloadRS.rest';
import {DVsTPersistService} from './service/dVsTPersistService';
import ErwerbspensumRS from './service/erwerbspensumRS.rest';
import EwkRS from './service/ewkRS.rest';
import {FachstelleRS} from './service/fachstelleRS.rest';
import GesuchsperiodeRS from './service/gesuchsperiodeRS.rest';
import GesuchstellerRS from './service/gesuchstellerRS.rest';
import HttpResponseInterceptor from './service/HttpResponseInterceptor';
import {InstitutionRS} from './service/institutionRS.rest';
import {InstitutionStammdatenRS} from './service/institutionStammdatenRS.rest';
import KindRS from './service/kindRS.rest';
import ListResourceRS from './service/listResourceRS.rest';
import {MandantRS} from './service/mandantRS.rest';
import MitteilungRS from './service/mitteilungRS.rest';
import {NavigationLogger} from './service/NavigationLogger';
import {ReportAsyncRS} from './service/reportAsyncRS.rest';
import {ReportRS} from './service/reportRS.rest';
import {SearchIndexRS} from './service/searchIndexRS.rest';
import {TraegerschaftRS} from './service/traegerschaftRS.rest';
import {UploadRS} from './service/uploadRS.rest';
import BenutzerRS from './service/benutzerRS.rest';
import VerfuegungRS from './service/verfuegungRS.rest';
import HttpVersionInterceptor from './service/version/HttpVersionInterceptor';
import ZahlungRS from './service/zahlungRS.rest';

const dependencies: string[] = [
    /* Angular modules */
    'ngAnimate',
    'ngSanitize',
    'ngMessages',
    'ngAria',
    'ngCookies',
    /* shared DVBern modules */
    router.name,
    EbeguErrors.name,
    EbeguAuthentication.name,
    /* 3rd-party modules */
    'ui.bootstrap',
    'smart-table',
    'ngMaterial',
    'ngMessages',
    'pascalprecht.translate',
    'angularMoment',
    'cfp.hotkeys',
    'ngFileUpload',
    'unsavedChanges',
];

export const EbeguWebCore: angular.IModule = angular
    .module('ebeguWeb.core', dependencies)
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
    .service('BatchJobRS', BatchJobRS)
    .service('BetreuungRS', BetreuungRS)
    .service('GesuchsperiodeRS', GesuchsperiodeRS)
    .service('BenutzerRS', BenutzerRS)
    .service('VerfuegungRS', VerfuegungRS)
    .service('DokumenteRS', DokumenteRS)
    .service('UploadRS', UploadRS)
    .service('DownloadRS', DownloadRS)
    .service('WizardStepRS', WizardStepRS)
    .service('AntragStatusHistoryRS', AntragStatusHistoryRS)
    .service('MitteilungRS', MitteilungRS)
    .service('ZahlungRS', ZahlungRS)
    .service('GlobalCacheService', GlobalCacheService)
    .service('ExportRS', ExportRS)
    .service('DossierRS', DossierRS)
    .service('GemeindeRS', GemeindeRS)
    .factory('PosteingangService', downgradeInjectable(PosteingangService) as any)
    .factory('AuthLifeCycleService', downgradeInjectable(AuthLifeCycleService) as any)
    .factory('GesuchGenerator', downgradeInjectable(GesuchGenerator) as any)
    .directive('dvMaxLength', DVMaxLength.factory())
    .directive('dvDatepicker', DVDatepicker.factory())
    .directive('dvTimepicker', DVTimepicker.factory())
    .directive('dvValueinput', DVValueinput.factory())
    .directive('dvUserselect', DVUserselect.factory())
    .directive('dvVerantwortlicherselect', DvVerantwortlicherselect.factory())
    .directive('dvNavigation', DVNavigation.factory())
    .directive('dvLoading', DVLoading.factory())
    .directive('dvSubmitevent', DVSubmitevent.factory())
    .directive('dvStPersistAntraege', DVSTPersistAntraege.factory())
    .directive('dvStPersistPendenzen', DVSTPersistPendenzen.factory())
    .directive('dvStResetSearch', DVSTResetSearch.factory())
    .directive('dvShowElement', DVShowElement.factory())
    .directive('dvDisplayElement', DVDisplayElement.factory())
    .directive('dvEnableElement', DVEnableElement.factory())
    .directive('dvBarcodeListener', DVBarcodeListener.factory())
    .directive('dvTrimEmpty', DVTrimEmpty.factory())
    .directive('dvSuppressFormSubmitOnEnter', DVSupressFormSubmitOnEnter.factory())
    .service('FachstelleRS', FachstelleRS)
    .service('BerechnungsManager', BerechnungsManager)
    .service('HttpResponseInterceptor', HttpResponseInterceptor)
    .service('HttpVersionInterceptor', HttpVersionInterceptor)
    .service('WizardStepManager', WizardStepManager)
    .service('NavigationLogger', NavigationLogger)
    .service('SearchIndexRS', SearchIndexRS)
    .service('DVsTPersistService', DVsTPersistService)
    .controller('DVElementController', DVRoleElementController)
    .component('dvLoadingButton', new DVLoadingButton())
    .component('dvAdresse', new AdresseComponentConfig())
    .component('dvErrorMessages', new DvErrorMessagesComponentConfig())
    .component('dvErwerbspensumList', new DVErwerbspensumListConfig())
    .component('dvInputContainer', new DvInputContainerComponentConfig())
    .component('dvRadioContainer', new DvRadioContainerComponentConfig())
    .component('dvTooltip', new DvTooltipComponentConfig())
    .component('dvPulldownUserMenu', new DvPulldownUserMenuComponentConfig())
    .component('dvMobileNavigationToggle', new DvMobileNavigationToggleComponentConfig())
    .component('dvHomeIcon', new DvHomeIconComponentConfig())
    .component('dvSkiplinks', new DvSkiplinksComponentConfig())
    .component('dvCountdown', new DvCountdownComponentConfig())
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
    .directive('dvHelpmenu', downgradeComponent({component: DvHelpmenuComponent}))
    .directive('dvNavbar', downgradeComponent({component: NavbarComponent}))
    .directive('dvBenutzer', downgradeComponent({component: BenutzerComponent}))
    .service('MahnungRS', MahnungRS)
    .service('ReportRS', ReportRS)
    .service('ReportAsyncRS', ReportAsyncRS)
    .service('EwkRS', EwkRS)
    .service('DatabaseMigrationRS', DatabaseMigrationRS)
    .filter('arrayToString', () => arrayToString)
    .filter('gemeindenToString', () => gemeindenToString);

