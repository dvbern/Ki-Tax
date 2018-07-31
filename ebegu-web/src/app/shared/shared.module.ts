import {CommonModule} from '@angular/common';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {DvNgErrorMessages} from '../../core/component/dv-error-messages/dv-ng-error-messages';
import {DvHelpmenuComponent} from '../../core/component/dv-helpmenu/dv-helpmenu';
import {DvNgGemeindeDialogComponent} from '../../core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {DvNgLinkDialogComponent} from '../../core/component/dv-ng-link-dialog/dv-ng-link-dialog.component';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {DvNgDebounceClickDirective} from '../../core/directive/dv-ng-debounce-click/dv-ng-debounce-click.directive';
import {DvNgShowElementDirective} from '../../core/directive/dv-ng-show-element/dv-ng-show-element.directive';
import {DvNgHelpDialogComponent} from '../../gesuch/dialog/dv-ng-help-dialog.component';
import {MaterialModule} from './material.module';

export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, './src/assets/translations/translations_', '.json');
}

@NgModule({
    imports: [
        CommonModule,
        HttpClientModule,
        FormsModule,

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
    ],
    entryComponents: [
        DvNgGemeindeDialogComponent,
        DvNgHelpDialogComponent,
        DvNgLinkDialogComponent,
        DvNgOkDialogComponent,
        DvNgRemoveDialogComponent,
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
