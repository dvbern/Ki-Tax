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

import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingComponent} from './onboarding.component';

describe('OnboardingComponent', () => {
    let component: OnboardingComponent;
    let fixture: ComponentFixture<OnboardingComponent>;

    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
            ['isDummyMode', 'isMultimandantEnabled']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

    beforeEach(waitForAsync(() => {
        applicationPropertyRSSpy.isDummyMode.and.returnValue(of(true).toPromise());
        applicationPropertyRSSpy.isMultimandantEnabled.and.returnValue(of(true).toPromise());
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true}),
            ],
            declarations: [OnboardingComponent],
            providers: [
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(OnboardingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
