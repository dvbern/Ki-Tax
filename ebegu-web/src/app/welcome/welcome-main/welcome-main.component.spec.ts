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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {WelcomeMainComponent} from './welcome-main.component';

describe('WelcomeMainComponent', () => {
    let component: WelcomeMainComponent;
    let fixture: ComponentFixture<WelcomeMainComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['getPrincipal', 'isOneOfRoles']
    );
    const stateServiceSpy = jasmine.createSpyObj<StateService>(
        StateService.name,
        ['go']
    );
    const i18nServiceSpy = jasmine.createSpyObj<I18nServiceRSRest>(
        I18nServiceRSRest.name,
        ['extractPreferredLanguage']
    );
    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(
            ApplicationPropertyRS.name,
            ['getPublicPropertiesCached']
        );

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy
                }
            ],
            declarations: [WelcomeMainComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(waitForAsync(() => {
        applicationPropertyRSSpy.getPublicPropertiesCached.and.returnValue(
            of(new TSPublicAppConfig()).toPromise()
        );
        fixture = TestBed.createComponent(WelcomeMainComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
