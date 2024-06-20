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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSBrowserLanguage} from '../../../../models/enums/TSBrowserLanguage';
import {I18nServiceRSRest} from '../../../i18n/services/i18nServiceRS.rest';

import {LanguageSelectorComponent} from './language-selector.component';

describe('LanguageSelectorComponent', () => {
    let component: LanguageSelectorComponent;
    let fixture: ComponentFixture<LanguageSelectorComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['getPrincipalRole']
    );
    const i18nServiceSpy = jasmine.createSpyObj<I18nServiceRSRest>(
        I18nServiceRSRest.name,
        ['currentLanguage']
    );
    i18nServiceSpy.currentLanguage.and.returnValue(TSBrowserLanguage.DE);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [LanguageSelectorComponent],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy}
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(LanguageSelectorComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
