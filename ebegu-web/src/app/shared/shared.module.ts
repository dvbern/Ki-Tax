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
import {FormsModule} from '@angular/forms';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {DvNgHelpDialogComponent} from '../../gesuch/dialog/dv-ng-help-dialog.component';
import {DvNgErrorMessages} from '../core/component/dv-error-messages/dv-ng-error-messages';
import {DvHelpmenuComponent} from '../core/component/dv-helpmenu/dv-helpmenu';
import {DvNgGemeindeDialogComponent} from '../core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {DvNgLinkDialogComponent} from '../core/component/dv-ng-link-dialog/dv-ng-link-dialog.component';
import {DvNgOkDialogComponent} from '../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {DvPosteingangComponent} from '../core/component/dv-posteingang/dv-posteingang';
import {NavbarComponent} from '../core/component/navbar/navbar.component';
import {DvNgDebounceClickDirective} from '../core/directive/dv-ng-debounce-click/dv-ng-debounce-click.directive';
import {DvNgShowElementDirective} from '../core/directive/dv-ng-show-element/dv-ng-show-element.directive';
import {MaterialModule} from './material.module';
import {UIRouterModule} from '@uirouter/angular';

export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, './assets/translations/translations_', '.json');
}

@NgModule({
    imports: [
        CommonModule,
        HttpClientModule,
        FormsModule,
        UIRouterModule,

        MaterialModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: (createTranslateLoader),
                deps: [HttpClient]
            }
        }),
    ],
    declarations: [
        DvHelpmenuComponent,
        DvNgDebounceClickDirective,
        DvNgErrorMessages,
        DvNgGemeindeDialogComponent,
        DvNgHelpDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgRemoveDialogComponent,
        NavbarComponent,
        DvNgShowElementDirective,
        DvPosteingangComponent,
    ],
    entryComponents: [
        DvNgGemeindeDialogComponent,
        DvNgHelpDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgRemoveDialogComponent,
        DvHelpmenuComponent,
        NavbarComponent,
        DvPosteingangComponent,
    ],
    exports: [
        CommonModule,
        HttpClientModule,
        FormsModule,

        MaterialModule,
        TranslateModule,

        DvHelpmenuComponent,
        DvNgDebounceClickDirective,
        DvNgErrorMessages,
        DvNgGemeindeDialogComponent,
        DvNgHelpDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgRemoveDialogComponent,
        DvNgShowElementDirective,
    ],
    providers: [
        // Leave empty (if you have singleton services, add them to CoreModule)
    ]
})
export class SharedModule {

    constructor(translate: TranslateService) {
        SharedModule.initTranslateService(translate);
    }

    private static initTranslateService(translate: TranslateService) {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang('de');
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        translate.use('de');
    }
}
