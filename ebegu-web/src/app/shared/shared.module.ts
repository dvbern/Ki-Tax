/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {CommonModule} from '@angular/common';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
    TranslateLoader,
    TranslateModule,
    TranslateService
} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {GuidedTourModule} from 'ngx-guided-tour';
import {AuszahlungsdatenComponent} from '../../gesuch/auszahlungsdaten/auszahlungsdaten.component';
import {DvEingabeHintComponent} from '../../gesuch/component/dv-eingabe-hint/dv-eingabe-hint.component';
import {SteuerveranlagungGemeinsamComponent} from '../../gesuch/component/finanzielleSituation/solothurn/steuerveranlagung-gemeinsam/steuerveranlagung-gemeinsam.component';
import {DvNgHelpDialogComponent} from '../../gesuch/dialog/dv-ng-help-dialog/dv-ng-help-dialog.component';
import {DvNgSupportDialogComponent} from '../../gesuch/dialog/dv-ng-support-dialog.component';
import {TSBrowserLanguage} from '../../models/enums/TSBrowserLanguage';
import {DvBenutzerEntryComponent} from '../core/component/dv-benutzer-entry/dv-benutzer-entry.component';
import {DvBisherXComponent} from '../core/component/dv-bisher/dv-bisher-x.component';
import {DvCheckboxXComponent} from '../core/component/dv-checkbox-x/dv-checkbox-x.component';
import {ErrorMessagesComponent} from '../core/component/dv-error-messages/error-messages.component';
import {DvHelpmenuComponent} from '../core/component/dv-helpmenu/dv-helpmenu.component';
import {DVInputContainerXComponent} from '../core/component/dv-input-container/dv-input-container-x.component';
import {DvInputLabelFieldComponent} from '../core/component/dv-input-label-field/dv-input-label-field.component';
import {DvMitteilungDelegationComponent} from '../core/component/dv-mitteilung-delegation/dv-mitteilung-delegation.component';
import {DvNavigationXComponent} from '../core/component/dv-navigation-x/dv-navigation-x.component';
import {DvNgBackDialogComponent} from '../core/component/dv-ng-back-dialog/dv-ng-back-dialog.component';
import {DvNgCancelDialogComponent} from '../core/component/dv-ng-confirm-dialog/dv-ng-cancel-dialog.component';
import {DvNgConfirmDialogComponent} from '../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgDisplayObjectDialogComponent} from '../core/component/dv-ng-display-object-dialog/dv-ng-display-object-dialog.component';
import {DvNgGemeindeDialogComponent} from '../core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {DvNgGesuchstellerDialogComponent} from '../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {DvNgLinkDialogComponent} from '../core/component/dv-ng-link-dialog/dv-ng-link-dialog.component';
import {DvNgMitteilungDelegationDialogComponent} from '../core/component/dv-ng-mitteilung-delegation-dialog/dv-ng-mitteilung-delegation-dialog.component';
import {DvNgMitteilungResultDialogComponent} from '../core/component/dv-ng-mitteilung-result-dialog/dv-ng-mitteilung-result-dialog.component';
import {DvNgMultiSelectDialogComponent} from '../core/component/dv-ng-multi-select-dialog/dv-ng-multi-select-dialog.component';
import {DvNgOkDialogComponent} from '../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {DvNgSelectTraegerschaftEmailDialogComponent} from '../core/component/dv-ng-select-traegerschaft-email-dialog/dv-ng-select-traegerschaft-email-dialog.component';
import {DvNgSozialdienstDialogComponent} from '../core/component/dv-ng-sozialdienst-dialog/dv-ng-sozialdienst-dialog.component';
import {DvNgThreeButtonDialogComponent} from '../core/component/dv-ng-three-button-dialog/dv-ng-three-button-dialog.component';
import {DvPosteingangComponent} from '../core/component/dv-posteingang/dv-posteingang.component';
import {DvRadioContainerXComponent} from '../core/component/dv-radio-container/dv-radio-container-x.component';
import {DvRadioInputXComponent} from '../core/component/dv-radio-input-x/dv-radio-input-x.component';
import {DvValueinputXComponent} from '../core/component/dv-valueinput-x/dv-valueinput-x.component';
import {LanguageSelectorComponent} from '../core/component/language-selector/language-selector.component';
import {NavbarComponent} from '../core/component/navbar/navbar.component';
import {PulldownUserMenuButtonComponent} from '../core/component/pulldown-user-menu-button/pulldown-user-menu-button.component';
import {PulldownUserMenuComponent} from '../core/component/pulldown-user-menu/pulldown-user-menu.component';
import {DvDemoFeatureDirective} from '../core/directive/dv-hide-feature/dv-demo-feature.directive';
import {DvLoadingButtonXDirective} from '../core/directive/dv-loading-button/dv-loading-button-x.directive';
import {DvNgDebounceClickDirective} from '../core/directive/dv-ng-debounce-click/dv-ng-debounce-click.directive';
import {DvNgEnableElementDirective} from '../core/directive/dv-ng-enable-element/dv-ng-enable-element.directive';
import {DvNgShowElementDirective} from '../core/directive/dv-ng-show-element/dv-ng-show-element.directive';
import {DvSearchListComponent} from '../core/dv-search-list/dv-search-list.component';
import {NewAntragListComponent} from '../core/new-antrag-list/new-antrag-list.component';
import {NewUserSelectDirective} from '../core/new-antrag-list/new-user-select.directive';
import {MultiMandantHttpLoader} from '../i18n/MultiMandantHttpLoader';
import {I18nServiceRSRest} from '../i18n/services/i18nServiceRS.rest';
import {KiBonGuidedTourComponent} from '../kibonTour/component/KiBonGuidedTourComponent';
import {BenutzerRolleComponent} from './component/benutzer-rolle/benutzer-rolle.component';
import {BerechtigungComponent} from './component/berechtigung/berechtigung.component';
import {DvDatePickerXComponent} from './component/dv-date-picker/dv-date-picker-x.component';
import {DvDemoFeatureWrapperComponent} from './component/dv-demo-feture-wrapper/dv-demo-feature-wrapper.component';
import {DvMonthPickerComponent} from './component/dv-month-picker/dv-month-picker.component';
import {DvSimpleTableComponent} from './component/dv-simple-table/dv-simple-table.component';
import {ExternalClientAssignmentComponent} from './component/external-client-assignment/external-client-assignment.component';
import {ExternalClientMultiselectComponent} from './component/external-client-multiselect/external-client-multiselect.component';
import {GemeindeMultiselectComponent} from './component/gemeinde-multiselect/gemeinde-multiselect.component';
import {MultipleFileUploadComponent} from './component/multpile-file-upload/multiple-file-upload.component';
import {SavingInfoComponent} from './component/save-input-info/saving-info.component';
import {SingleFileUploadComponent} from './component/single-file-upload/single-file-upload.component';
import {StammdatenHeaderComponent} from './component/stammdaten-header/stammdaten-header.component';
import {AccordionTabDirective} from './directive/accordion-tab.directive';
import {AccordionDirective} from './directive/accordion.directive';
import {DvIfViewportSizeDirective} from './directive/dv-if-viewport-size/dv-if-viewport-size.directive';
import {EnableElementDirective} from './directive/enable-element.directive';
import {LoadingButtonDirective} from './directive/loading-button.directive';
import {NumbersMinMaxDirective} from './directive/numbers-min-max.directive';
import {TooltipDirective} from './directive/TooltipDirective';
import {FullHeightContainerComponent} from './full-height-container/full-height-container.component';
import {FullHeightInnerPaddingContainerComponent} from './full-height-inner-padding-container/full-height-inner-padding-container.component';
import {MaterialModule} from './material.module';
import {EbeguDateTimePipe} from './pipe/ebegu-date-time.pipe';
import {EbeguDatePipe} from './pipe/ebegu-date.pipe';
import {EbeguNumberPipe} from './pipe/ebegu-number.pipe';
import {NextPeriodeStrPipe} from './pipe/next-periode-str.pipe';
import {PreviousPeriodeStrPipe} from './pipe/previous-periode-str.pipe';
import {MandantService} from './services/mandant.service';
import {UiViewComponent} from './ui-view/ui-view.component';
import {QrIbanValidatorDirective} from './validators/qr-iban-validator.directive';

export function createTranslateLoader(
    http: HttpClient,
    mandantService: MandantService
): TranslateLoader {
    return new MultiMandantHttpLoader(http, mandantService);
}

@NgModule({
    imports: [
        CommonModule,
        HttpClientModule,
        FormsModule,
        ReactiveFormsModule,
        UIRouterModule,

        MaterialModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: createTranslateLoader,
                deps: [HttpClient, MandantService]
            }
        }),
        GuidedTourModule.forRoot()
    ],
    declarations: [
        AccordionDirective,
        AccordionTabDirective,
        NumbersMinMaxDirective,
        QrIbanValidatorDirective,
        BenutzerRolleComponent,
        BerechtigungComponent,
        DvHelpmenuComponent,
        DvMitteilungDelegationComponent,
        DvNgMitteilungDelegationDialogComponent,
        DvNgMitteilungResultDialogComponent,
        DvBenutzerEntryComponent,
        DvNgDebounceClickDirective,
        DvNgGemeindeDialogComponent,
        DvNgHelpDialogComponent,
        DvNgSupportDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgDisplayObjectDialogComponent,
        DvNgRemoveDialogComponent,
        DvNgBackDialogComponent,
        DvNgCancelDialogComponent,
        DvNgMultiSelectDialogComponent,
        DvNgThreeButtonDialogComponent,
        DvNgShowElementDirective,
        DvPosteingangComponent,
        ErrorMessagesComponent,
        FullHeightContainerComponent,
        FullHeightInnerPaddingContainerComponent,
        GemeindeMultiselectComponent,
        LoadingButtonDirective,
        NavbarComponent,
        StammdatenHeaderComponent,
        TooltipDirective,
        UiViewComponent,
        KiBonGuidedTourComponent,
        DvNgGesuchstellerDialogComponent,
        DvNgSelectTraegerschaftEmailDialogComponent,
        ExternalClientAssignmentComponent,
        ExternalClientMultiselectComponent,
        MultipleFileUploadComponent,
        SingleFileUploadComponent,
        DvNgConfirmDialogComponent,
        DvMonthPickerComponent,
        EbeguDatePipe,
        NextPeriodeStrPipe,
        PreviousPeriodeStrPipe,
        DvMonthPickerComponent,
        NewAntragListComponent,
        NewUserSelectDirective,
        DvLoadingButtonXDirective,
        DvDemoFeatureDirective,
        DvSimpleTableComponent,
        DvRadioContainerXComponent,
        DvSearchListComponent,
        SavingInfoComponent,
        DVInputContainerXComponent,
        DvBisherXComponent,
        DvNgSozialdienstDialogComponent,
        EbeguDateTimePipe,
        DvDatePickerXComponent,
        DvIfViewportSizeDirective,
        DvNavigationXComponent,
        DvInputLabelFieldComponent,
        EnableElementDirective,
        DvEingabeHintComponent,
        EbeguNumberPipe,
        DvValueinputXComponent,
        DvRadioInputXComponent,
        DvCheckboxXComponent,
        DvNgEnableElementDirective,
        DvDemoFeatureWrapperComponent,
        PulldownUserMenuComponent,
        PulldownUserMenuButtonComponent,
        LanguageSelectorComponent,
        AuszahlungsdatenComponent,
        SteuerveranlagungGemeinsamComponent
    ],
    // adding custom elements schema disables Angular's element validation: you can now use transclusion for the
    // dv-accordion-tab with multi-slot transclusion (tab-title & tab-body elements).
    // See https://stackoverflow.com/a/51214263
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    exports: [
        CommonModule,
        HttpClientModule,
        FormsModule,

        MaterialModule,
        TranslateModule,

        AccordionDirective,
        AccordionTabDirective,
        NumbersMinMaxDirective,
        QrIbanValidatorDirective,
        BenutzerRolleComponent,
        BerechtigungComponent,
        DvHelpmenuComponent,
        DvMitteilungDelegationComponent,
        DvNgMitteilungDelegationDialogComponent,
        DvNgMitteilungResultDialogComponent,
        DvNgDebounceClickDirective,
        DvNgGemeindeDialogComponent,
        DvBenutzerEntryComponent,
        DvNgHelpDialogComponent,
        DvNgSupportDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgDisplayObjectDialogComponent,
        DvNgRemoveDialogComponent,
        DvNgBackDialogComponent,
        DvNgCancelDialogComponent,
        DvNgMultiSelectDialogComponent,
        DvNgThreeButtonDialogComponent,
        DvNgShowElementDirective,
        ErrorMessagesComponent,
        FullHeightContainerComponent,
        FullHeightInnerPaddingContainerComponent,
        GemeindeMultiselectComponent,
        LoadingButtonDirective,
        StammdatenHeaderComponent,
        UiViewComponent,
        TooltipDirective,
        ExternalClientAssignmentComponent,
        ExternalClientMultiselectComponent,
        MultipleFileUploadComponent,
        SingleFileUploadComponent,
        DvNgConfirmDialogComponent,
        DvMonthPickerComponent,
        EbeguDatePipe,
        NewAntragListComponent,
        NewUserSelectDirective,
        DvLoadingButtonXDirective,
        DvDemoFeatureDirective,
        EbeguDatePipe,
        DvSimpleTableComponent,
        DvRadioContainerXComponent,
        DvSearchListComponent,
        SavingInfoComponent,
        DVInputContainerXComponent,
        DvBisherXComponent,
        DvNgSozialdienstDialogComponent,
        NextPeriodeStrPipe,
        PreviousPeriodeStrPipe,
        EbeguDateTimePipe,
        DvDatePickerXComponent,
        DvNavigationXComponent,
        DvInputLabelFieldComponent,
        EnableElementDirective,
        DvEingabeHintComponent,
        DvValueinputXComponent,
        EbeguNumberPipe,
        DvValueinputXComponent,
        DvRadioInputXComponent,
        DvCheckboxXComponent,
        DvNgEnableElementDirective,
        DvIfViewportSizeDirective,
        AuszahlungsdatenComponent,
        SteuerveranlagungGemeinsamComponent
    ],
    providers: [
        // Leave empty (if you have singleton services, add them to CoreModule)
    ]
})
export class SharedModule {
    public constructor(
        translate: TranslateService,
        i18nServiceRS: I18nServiceRSRest
    ) {
        SharedModule.initTranslateService(translate, i18nServiceRS);
    }

    private static initTranslateService(
        translate: TranslateService,
        i18nServiceRS: I18nServiceRSRest
    ): void {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang(TSBrowserLanguage.DE);
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        translate.use(i18nServiceRS.extractPreferredLanguage());
    }
}
