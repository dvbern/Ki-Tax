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
import {of} from 'rxjs';
import ErrorService from '../core/errors/service/ErrorService';
import {SharedModule} from '../shared/shared.module';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import TestDataUtil from '../../utils/TestDataUtil.spec';
import {GemeindeListComponent} from './gemeinde-list.component';


describe('gemeindenView', () => {

    let component: GemeindeListComponent;
    let fixture: ComponentFixture<GemeindeListComponent>;

    beforeEach(async(() => {
        const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [GemeindeListComponent]
        })
            .compileComponents();
        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of(
            [TestDataUtil.createGemeindeBern(), TestDataUtil.createGemeindeOstermundigen()]));
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
