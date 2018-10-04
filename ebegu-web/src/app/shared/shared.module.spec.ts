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

import {TranslateService} from '@ngx-translate/core';
import {SharedModule} from './shared.module';

describe('SharedModule', () => {
    let sharedModule: SharedModule;
    const translateServiceSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name,
        ['setDefaultLang', 'use']);

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
