import {CommonModule} from '@angular/common';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
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
    declarations: [],
    exports: [
        CommonModule,
        HttpClientModule,
        FormsModule,

        MaterialModule,
        TranslateModule,
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
