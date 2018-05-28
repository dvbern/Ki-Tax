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

import 'angular';
import './core.module.less';
import {DVBenutzerListConfig} from './component/dv-benutzer-list/dv-benutzer-list';
import {DVBenutzerConfig} from './component/dv-benutzer/dv-benutzer';
import {appRun} from './core.route';
import {configure} from './config';
import router from '../dvbModules/router/router.module';
import AdresseRS from './service/adresseRS.rest';
import ListResourceRS from './service/listResourceRS.rest';
import EbeguRestUtil from '../utils/EbeguRestUtil';
import GesuchstellerRS from './service/gesuchstellerRS.rest';
import {AdresseComponentConfig} from './component/dv-adresse/dv-adresse';
import {DvErrorMessagesComponentConfig} from './component/dv-error-messages/dv-error-messages';
import FallRS from '../gesuch/service/fallRS.rest';
import GesuchModelManager from '../gesuch/service/gesuchModelManager';
import GesuchRS from '../gesuch/service/gesuchRS.rest';
import FamiliensituationRS from '../gesuch/service/familiensituationRS.rest';
import FinanzielleSituationRS from '../gesuch/service/finanzielleSituationRS.rest';
import EinkommensverschlechterungContainerRS from '../gesuch/service/einkommensverschlechterungContainerRS.rest';
import DVMaxLength from './directive/dv-max-length';
import {DVDatepicker} from './directive/dv-datepicker/dv-datepicker';
import {DVTimepicker} from './directive/dv-timepicker/dv-timepicker';
import {FachstelleRS} from './service/fachstelleRS.rest';
import {DvInputContainerComponentConfig} from './component/dv-input-container/dv-input-container';
import {DvRadioContainerComponentConfig} from './component/dv-radio-container/dv-radio-container';
import {MandantRS} from './service/mandantRS.rest';
import {TraegerschaftRS} from './service/traegerschaftRS.rest';
import {InstitutionRS} from './service/institutionRS.rest';
import {InstitutionStammdatenRS} from './service/institutionStammdatenRS.rest';
import {DvBisherComponentConfig} from './component/dv-bisher/dv-bisher';
import KindRS from './service/kindRS.rest';
import {DvDialog} from './directive/dv-dialog/dv-dialog';
import BetreuungRS from './service/betreuungRS.rest';
import {DVErwerbspensumListConfig} from './component/dv-erwerbspensum-list/dv-erwerbspensum-list';
import ErwerbspensumRS from './service/erwerbspensumRS.rest';
import BerechnungsManager from '../gesuch/service/berechnungsManager';
import {DvTooltipComponentConfig} from './component/dv-tooltip/dv-tooltip';
import GesuchsperiodeRS from './service/gesuchsperiodeRS.rest';
import {EbeguErrors} from './errors/errors';
import EbeguUtil from '../utils/EbeguUtil';
import {EbeguAuthentication} from '../authentication/authentication.module';
import {DvPulldownUserMenuComponentConfig} from './component/dv-pulldown-user-menu/dv-pulldown-user-menu';
import UserRS from './service/userRS.rest';
import {DVUserselect} from './directive/dv-userselect/dv-userselect';
import DokumenteRS from '../gesuch/service/dokumenteRS.rest';
import {DVDokumenteListConfig} from './component/dv-dokumente-list/dv-dokumente-list';
import {DVLoading} from './directive/dv-loading/dv-loading';
import {DVLoadingButton} from './directive/dv-loading-button/dv-loading-button';
import {DvAccordionComponentConfig} from './component/dv-accordion/dv-accordion';
import {DvAccordionTabComponentConfig} from './component/dv-accordion/dv-accordion-tab/dv-accordion-tab';
import HttpResponseInterceptor from './service/HttpResponseInterceptor';
import DVSubmitevent from './directive/dv-submitevent/dv-submitevent';
import 'ng-file-upload';
import {UploadRS} from './service/uploadRS.rest';
import {DownloadRS} from './service/downloadRS.rest';
import VerfuegungRS from './service/verfuegungRS.rest';
import {DVShowElement} from './directive/dv-show-element/dv-show-element';
import {DVEnableElement} from './directive/dv-enable-element/dv-enable-element';
import {DVRoleElementController} from './controller/DVRoleElementController';
import WizardStepManager from '../gesuch/service/wizardStepManager';
import WizardStepRS from '../gesuch/service/WizardStepRS.rest';
import EinkommensverschlechterungInfoRS from '../gesuch/service/einkommensverschlechterungInfoRS.rest';
import {DVNavigation} from './directive/dv-navigation/dv-navigation';
import {DVAntragListConfig} from './component/dv-antrag-list/dv-antrag-list';
import AntragStatusHistoryRS from './service/antragStatusHistoryRS.rest';
import {NavigationLogger} from './service/NavigationLogger';
import GlobalCacheService from '../gesuch/service/globalCacheService';
import MahnungRS from '../gesuch/service/mahnungRS.rest';
import {DvHomeIconComponentConfig} from './component/dv-home-icon/dv-home-icon';
import {DVBarcodeListener} from './directive/dv-barcode-listener';
import DVTrimEmpty from './directive/dv-trim-empty/dv-trim-empty';
import {DvMobileNavigationToggleComponentConfig} from './component/dv-mobile-navigation-toggle/dv-mobile-navigation-toggle';
import {SearchIndexRS} from './service/searchIndexRS.rest';
import {DvSearchResultIconComponentConfig} from './component/dv-search/dv-search-result-icon/dv-search-result-icon';
import {DvQuicksearchboxComponentConfig} from './component/dv-search/dv-quicksearchbox/dv-quicksearchbox';
import {DVValueinput} from './directive/dv-valueinput/dv-valueinput';
import MitteilungRS from './service/mitteilungRS.rest';
import {DVMitteilungListConfig} from './component/dv-mitteilung-list/dv-mitteilung-list';
import {DvPosteingangComponentConfig} from './component/dv-posteingang/dv-posteingang';
import {ReportRS} from './service/reportRS.rest';
import DVSupressFormSubmitOnEnter from './directive/dv-suppress-form-submit-on-enter/dv-suppress-form-submit-on-enter';
import ExportRS from '../gesuch/service/exportRS.rest';
import {DvCountdownComponentConfig} from './component/dv-countdown/dv-countdown';
import ZahlungRS from './service/zahlungRS.rest';
import EwkRS from './service/ewkRS.rest';
import {DVDisplayElement} from './directive/dv-display-element/dv-display-element';
import {DVsTPersistService} from './service/dVsTPersistService';
import DVSTResetSearch from './directive/dv-st-reset-search/dv-st-reset-search';
import DVSTPersistAntraege from './directive/dv-st-persist-antraege/dv-st-persist-antraege';
import DVSTPersistPendenzen from './directive/dv-st-persist-quicksearch/dv-st-persist-quicksearch';
import {DvDownloadmenuComponentConfig} from './component/dv-downloadmenu/dv-downloadmenu';
import {DVVorlageListConfig} from './component/dv-vorlage-list/dv-vorlage-list';
import {DatabaseMigrationRS} from '../admin/service/databaseMigrationRS.rest';
import HttpVersionInterceptor from './service/version/HttpVersionInterceptor';
import {DVVersionComponentConfig} from './component/dv-version/dv-version';
import {DvSkiplinksComponentConfig} from './component/dv-skiplinks/dv-skiplinks';
import SearchRS from '../gesuch/service/searchRS.rest';
import {DvVerantwortlicherselect} from './directive/dv-verantwortlicherselect/dv-verantwortlicherselect';
import {ReportAsyncRS} from './service/reportAsyncRS.rest';
import BatchJobRS from './service/batchRS.rest';

let dynamicDependencies = function (): string[] {

    let dynDep: string [] = [];
    //hier kommen plugins die wir fuer dev disablen wollen
    if (ENV === 'development') {
        return [];
    }
    return dynDep;
};

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
    'unsavedChanges'
];

export const EbeguWebCore: angular.IModule = angular
    .module('ebeguWeb.core', dependencies.concat(dynamicDependencies()))
    .run(appRun)
    .config(configure)
    .constant('REST_API', '/ebegu/api/v1/')
    .constant('MAX_LENGTH', 255)
    .constant('CONSTANTS', {
        name: 'EBEGU',
        REST_API: '/ebegu/api/v1/',
        MAX_LENGTH: 255,
        FALLNUMMER_LENGTH: 6,
        PATTERN_BETRAG: '([0-9]{0,12})',
        PATTERN_PERCENTAGE: '^[0-9][0-9]?$|^100$',
        PATTERN_PHONE: '(0|\\+41|0041)\\s?([\\d]{2})\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
        PATTERN_MOBILE: '(0|\\+41|0041)\\s?(74|75|76|77|78|79)\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
        PATTERN_EMAIL: '[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}',
        INSTITUTIONSSTAMMDATENID_DUMMY_TAGESSCHULE: '199ac4a1-448f-4d4c-b3a6-5aee21f89613'

    })
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
    .service('UserRS', UserRS)
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
    .directive('dvMaxLength', DVMaxLength.factory())
    .directive('dvDatepicker', DVDatepicker.factory())
    .directive('dvTimepicker', DVTimepicker.factory())
    .directive('dvValueinput', DVValueinput.factory())
    .directive('dvUserselect', DVUserselect.factory())
    .directive('dvVerantwortlicherselect', DvVerantwortlicherselect.factory())
    .directive('dvNavigation', DVNavigation.factory())
    .directive('dvLoading', DVLoading.factory())
    .directive('dvLoadingButton', DVLoadingButton.factory())
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
    .component('dvDownloadmenu', new DvDownloadmenuComponentConfig())
    .component('dvCountdown', new DvCountdownComponentConfig())
    .component('dvPosteingang', new DvPosteingangComponentConfig())
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
    .component('dvBenutzer', new DVBenutzerConfig())
    .service('MahnungRS', MahnungRS)
    .service('ReportRS', ReportRS)
    .service('ReportAsyncRS', ReportAsyncRS)
    .service('EwkRS', EwkRS)
    .service('DatabaseMigrationRS', DatabaseMigrationRS)
    .filter('arrayToString', () => {
        return function (input: Array<string>) {
            if (input) {
                return input.join(', ');
            }
            return '';
        };
    });

