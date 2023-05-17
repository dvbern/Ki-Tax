import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSBrowserLanguage} from '../../../../models/enums/TSBrowserLanguage';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {I18nServiceRSRest} from '../../../i18n/services/i18nServiceRS.rest';
import {LogFactory} from '../../logging/LogFactory';

const LOG = LogFactory.createLog('LangageSelectorComponent');

@Component({
    selector: 'dv-language-selector',
    templateUrl: './language-selector.component.html',
    styleUrls: ['./language-selector.component.less'],
    changeDetection: ChangeDetectionStrategy.Default
})
export class LanguageSelectorComponent implements OnInit {

    @Input()
    public hideForLoggedUser: boolean = false;

    public readonly DE = TSBrowserLanguage.DE;
    public readonly FR = TSBrowserLanguage.FR;

    public constructor(
        private readonly i18nServiceRS: I18nServiceRSRest,
        private readonly authService: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
    }

    public changeLanguage(language: TSBrowserLanguage): void {
        this.i18nServiceRS.changeClientLanguage(language);
        LOG.info('language changed', language);
    }

    public isLanguage(language: TSBrowserLanguage): boolean {
        return this.i18nServiceRS.currentLanguage() === language;
    }

    public hideLanguageSelector(): boolean {
        return this.hideForLoggedUser && EbeguUtil.isUndefined(this.authService.getPrincipalRole());
    }
}
