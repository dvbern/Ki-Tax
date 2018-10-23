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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingComponent} from './onboarding.component';
import createSpyObj = jasmine.createSpyObj;

describe('OnboardingComponent', () => {
    let component: OnboardingComponent;
    let fixture: ComponentFixture<OnboardingComponent>;

    const gemeindeRSSpy = createSpyObj<GemeindeRS>(GemeindeRS.name, ['getAktiveGemeinden']);
    const applicationPropertyRSSpy = createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['isDummyMode']);

    beforeEach(async(() => {
        gemeindeRSSpy.getAktiveGemeinden.and.returnValue(of([]).toPromise());
        applicationPropertyRSSpy.isDummyMode.and.returnValue(of(true).toPromise());

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true}),
            ],
            declarations: [OnboardingComponent],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
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

    it('should load all active Gemeinden', () => {
        expect(gemeindeRSSpy.getAktiveGemeinden).toHaveBeenCalled();
    });
});
