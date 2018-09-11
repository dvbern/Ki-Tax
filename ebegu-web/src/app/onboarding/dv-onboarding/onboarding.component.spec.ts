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
import * as angular from 'angular';
import {of} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {EbeguWebCore} from '../../core/core.angularjs.module';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingComponent} from './onboarding.component';
import createSpyObj = jasmine.createSpyObj;

describe('OnboardingComponent', () => {
    let component: OnboardingComponent;
    let fixture: ComponentFixture<OnboardingComponent>;

    const gemeindeRSSpy = createSpyObj<GemeindeRS>(GemeindeRS.name, ['getAllGemeinden']);
    const applicationPropertyRSSpy = createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['isDummyMode']);

    let $injector: angular.auto.IInjectorService;

    beforeEach(angular.mock.module(EbeguWebCore.name));
    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject((_$injector_: angular.auto.IInjectorService) => {
        $injector = _$injector_;
    }));

    beforeEach(async(() => {
        gemeindeRSSpy.getAllGemeinden.and.returnValue(of([]).toPromise());
        applicationPropertyRSSpy.isDummyMode.and.returnValue(of(true).toPromise());

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true})
            ],
            declarations: [OnboardingComponent],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                {
                    provide: '$injector',
                    useFactory: () => $injector,
                    deps: []
                },
                {
                    provide: '$scope',
                    useFactory: () => $injector.get('$rootScope').$new(),
                    deps: []
                },
            ]
        })
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

    it('should load all Gemeinden', () => {
        expect(gemeindeRSSpy.getAllGemeinden).toHaveBeenCalled();
    });
});
