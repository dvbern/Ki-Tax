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

import {CommonModule} from '@angular/common';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatMenuModule} from '@angular/material/menu';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {UIRouterModule} from '@uirouter/angular';
import {GuidedTourModule} from 'ngx-guided-tour';
import {DvNgHelpDialogComponent} from '../../gesuch/dialog/dv-ng-help-dialog/dv-ng-help-dialog.component';
import {DvNgSupportDialogComponent} from '../../gesuch/dialog/dv-ng-support-dialog.component';
import {TSBrowserLanguage} from '../../models/enums/TSBrowserLanguage';
import {DvBenutzerEntry} from '../core/component/dv-benutzer-entry/dv-benutzer-entry';
import {DvBisherXComponent} from '../core/component/dv-bisher/dv-bisher-x.component';
import {ErrorMessagesComponent} from '../core/component/dv-error-messages/error-messages.component';
import {DvHelpmenuComponent} from '../core/component/dv-helpmenu/dv-helpmenu';
import {DVInputContainerXComponent} from '../core/component/dv-input-container/dv-input-container-x.component';
import {DvMitteilungDelegationComponent} from '../core/component/dv-mitteilung-delegation/dv-mitteilung-delegation';
import {DvNgConfirmDialogComponent} from '../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgGemeindeDialogComponent} from '../core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {DvNgGesuchstellerDialogComponent} from '../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {DvNgLinkDialogComponent} from '../core/component/dv-ng-link-dialog/dv-ng-link-dialog.component';
import {DvNgMitteilungDelegationDialogComponent} from '../core/component/dv-ng-mitteilung-delegation-dialog/dv-ng-mitteilung-delegation-dialog.component';
import {DvNgOkDialogComponent} from '../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {DvNgSozialdienstDialogComponent} from '../core/component/dv-ng-sozialdienst-dialog/dv-ng-sozialdienst-dialog.component';
import {DvNgThreeButtonDialogComponent} from '../core/component/dv-ng-three-button-dialog/dv-ng-three-button-dialog.component';
import {DvPosteingangComponent} from '../core/component/dv-posteingang/dv-posteingang';
import {DvRadioContainerXComponent} from '../core/component/dv-radio-container/dv-radio-container-x.component';
import {NavbarComponent} from '../core/component/navbar/navbar.component';
import {DvLoadingButtonXDirective} from '../core/directive/dv-loading-button/dv-loading-button-x.directive';
import {DvNgDebounceClickDirective} from '../core/directive/dv-ng-debounce-click/dv-ng-debounce-click.directive';
import {DvNgShowElementDirective} from '../core/directive/dv-ng-show-element/dv-ng-show-element.directive';
import {DvSearchListComponent} from '../core/dv-search-list/dv-search-list.component';
import {NewAntragListComponent} from '../core/new-antrag-list/new-antrag-list.component';
import {NewUserSelectDirective} from '../core/new-antrag-list/new-user-select.directive';
import {I18nServiceRSRest} from '../i18n/services/i18nServiceRS.rest';
import {KiBonGuidedTourComponent} from '../kibonTour/component/KiBonGuidedTourComponent';
import {BenutzerRolleComponent} from './component/benutzer-rolle/benutzer-rolle.component';
import {BerechtigungComponent} from './component/berechtigung/berechtigung.component';
import {DvMonthPickerComponent} from './component/dv-month-picker/dv-month-picker.component';
import {DvSimpleTableComponent} from './component/dv-simple-table/dv-simple-table.component';
import {ExternalClientAssignmentComponent} from './component/external-client-assignment/external-client-assignment.component';
import {ExternalClientMultiselectComponent} from './component/external-client-multiselect/external-client-multiselect.component';
import {FileUploadComponent} from './component/file-upload/file-upload.component';
import {GemeindeMultiselectComponent} from './component/gemeinde-multiselect/gemeinde-multiselect.component';
import {SavingInfo} from './component/save-input-info/saving-info.component';
import {StammdatenHeaderComponent} from './component/stammdaten-header/stammdaten-header.component';
import {AccordionTabDirective} from './directive/accordion-tab.directive';
import {AccordionDirective} from './directive/accordion.directive';
import {LoadingButtonDirective} from './directive/loading-button.directive';
import {NumbersMinMaxDirective} from './directive/numbers-min-max.directive';
import {TooltipDirective} from './directive/TooltipDirective';
import {FullHeightContainerComponent} from './full-height-container/full-height-container.component';
import {FullHeightInnerPaddingContainerComponent} from './full-height-inner-padding-container/full-height-inner-padding-container.component';
import {MaterialModule} from './material.module';
import {EbeguDatePipe} from './pipe/ebegu-date.pipe';
import {NextPeriodeStrPipe} from './pipe/next-periode-str.pipe';
import {UiViewComponent} from './ui-view/ui-view.component';

export function createTranslateLoader(http: HttpClient): TranslateHttpLoader {
    return new TranslateHttpLoader(http, './assets/translations/translations_', `.json?t=${Date.now()}`);
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
                useFactory: (createTranslateLoader),
                deps: [HttpClient],
            },
        }),
        GuidedTourModule.forRoot(),
        MatMenuModule,
    ],
    declarations: [
        AccordionDirective,
        AccordionTabDirective,
        NumbersMinMaxDirective,
        BenutzerRolleComponent,
        BerechtigungComponent,
        DvHelpmenuComponent,
        DvMitteilungDelegationComponent,
        DvNgMitteilungDelegationDialogComponent,
        DvBenutzerEntry,
        DvNgDebounceClickDirective,
        DvNgGemeindeDialogComponent,
        DvNgHelpDialogComponent,
        DvNgSupportDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgRemoveDialogComponent,
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
        ExternalClientAssignmentComponent,
        ExternalClientMultiselectComponent,
        FileUploadComponent,
        DvNgConfirmDialogComponent,
        DvMonthPickerComponent,
        EbeguDatePipe,
        NextPeriodeStrPipe,
        DvMonthPickerComponent,
        NewAntragListComponent,
        NewUserSelectDirective,
        DvLoadingButtonXDirective,
        DvLoadingButtonXDirective,
        DvSimpleTableComponent,
        DvRadioContainerXComponent,
        DvSearchListComponent,
        SavingInfo,
        DVInputContainerXComponent,
        DvBisherXComponent,
        DvNgSozialdienstDialogComponent,
    ],
    exports: [
        CommonModule,
        HttpClientModule,
        FormsModule,

        MaterialModule,
        TranslateModule,

        AccordionDirective,
        AccordionTabDirective,
        NumbersMinMaxDirective,
        BenutzerRolleComponent,
        BerechtigungComponent,
        DvHelpmenuComponent,
        DvMitteilungDelegationComponent,
        DvNgMitteilungDelegationDialogComponent,
        DvNgDebounceClickDirective,
        DvNgGemeindeDialogComponent,
        DvBenutzerEntry,
        DvNgHelpDialogComponent,
        DvNgSupportDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgRemoveDialogComponent,
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
        FileUploadComponent,
        DvNgConfirmDialogComponent,
        DvMonthPickerComponent,
        EbeguDatePipe,
        NewAntragListComponent,
        DvLoadingButtonXDirective,
        EbeguDatePipe,
        DvSimpleTableComponent,
        DvRadioContainerXComponent,
        DvSearchListComponent,
        SavingInfo,
        DVInputContainerXComponent,
        DvBisherXComponent,
        DvNgSozialdienstDialogComponent,
        NextPeriodeStrPipe,
    ],
    providers: [
        // Leave empty (if you have singleton services, add them to CoreModule)
    ],
})
export class SharedModule {

    public constructor(
        translate: TranslateService,
        i18nServiceRS: I18nServiceRSRest,
    ) {
        SharedModule.initTranslateService(translate, i18nServiceRS);
    }

    private static initTranslateService(translate: TranslateService, i18nServiceRS: I18nServiceRSRest): void {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang(TSBrowserLanguage.DE);
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        translate.use(i18nServiceRS.extractPreferredLanguage());
    }
}
