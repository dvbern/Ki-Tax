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
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {WindowRef} from '../../core/service/windowRef.service';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeListComponent} from './gemeinde-list.component';

describe('GemeindeListComponent', () => {

    let component: GemeindeListComponent;
    let fixture: ComponentFixture<GemeindeListComponent>;

    beforeEach(waitForAsync(() => {
        const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
        const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
            ['isRole', 'isOneOfRoles']);
        const i18nServiceSpy = jasmine
            .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                WindowRef,
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
            declarations: [GemeindeListComponent],
        }).compileComponents();

        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of(
            [TestDataUtil.createGemeindeParis(), TestDataUtil.createGemeindeLondon()]));
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GemeindeListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
