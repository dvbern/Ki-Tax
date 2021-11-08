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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingMainComponent} from './onboarding-main.component';

describe('OnboardingMainComponent', () => {
    let component: OnboardingMainComponent;
    let fixture: ComponentFixture<OnboardingMainComponent>;

    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const applicationRSSpy = jasmine
        .createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['getPublicPropertiesCached']);

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true}),
            ],
            declarations: [OnboardingMainComponent],
            providers: [
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: ApplicationPropertyRS, useValue: applicationRSSpy},
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        const properties = new TSPublicAppConfig();
        applicationRSSpy.getPublicPropertiesCached.and.returnValue(of(properties).toPromise());
        fixture = TestBed.createComponent(OnboardingMainComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
