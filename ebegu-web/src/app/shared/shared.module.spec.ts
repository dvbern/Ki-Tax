import {TranslateService} from '@ngx-translate/core';
import {SharedModule} from './shared.module';

describe('SharedModule', () => {
    let sharedModule: SharedModule;
    const translateServiceSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name, ['setDefaultLang', 'use']);

    beforeEach(() => {
        sharedModule = new SharedModule(translateServiceSpy);
    });

    it('should create an instance', () => {
        expect(sharedModule).toBeTruthy();
    });

    it('should initialise the TranslateService', () => {
        const defaultLanguage = 'de';
        expect(translateServiceSpy.setDefaultLang).toHaveBeenCalledWith(defaultLanguage);
        expect(translateServiceSpy.use).toHaveBeenCalledWith(defaultLanguage);
    });
});
