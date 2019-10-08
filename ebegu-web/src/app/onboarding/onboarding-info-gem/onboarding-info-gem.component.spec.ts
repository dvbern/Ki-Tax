/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingInfoGemComponent} from './onboarding-info-gem.component';

describe('OnboardingInfoGemComponent', () => {
    let component: OnboardingInfoGemComponent;
    let fixture: ComponentFixture<OnboardingInfoGemComponent>;

    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getAktiveGemeinden']);
    const i18nServiceSpy =
        jasmine.createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

    beforeEach(async(() => {
        gemeindeRSSpy.getAktiveGemeinden.and.returnValue(of([]).toPromise());
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true}),
            ],
            declarations: [OnboardingInfoGemComponent],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(OnboardingInfoGemComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load all active Gemeinden', () => {
        expect(gemeindeRSSpy.getAktiveGemeinden).toHaveBeenCalled();
    });
});
