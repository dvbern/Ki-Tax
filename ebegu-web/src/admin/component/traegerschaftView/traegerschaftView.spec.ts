/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {SharedModule} from '../../../app/shared/shared.module';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../../app/core/service/traegerschaftRS.rest';

import {TraegerschaftViewComponent} from './traegerschaftView';

describe('traegerschaftView', () => {

    let component: TraegerschaftViewComponent;
    let fixture: ComponentFixture<TraegerschaftViewComponent>;

    beforeEach(async(() => {
        const traegerschaftServiceSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['createTraegerschaft']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: TraegerschaftRS, useValue: traegerschaftServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [TraegerschaftViewComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TraegerschaftViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
