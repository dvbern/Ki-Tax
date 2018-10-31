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
import {Transition} from '@uirouter/core';
import {of} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {EinladungAbschliessenComponent} from './einladung-abschliessen.component';

describe('EinladungAbschliessenComponent', () => {
    let component: EinladungAbschliessenComponent;
    let fixture: ComponentFixture<EinladungAbschliessenComponent>;

    beforeEach(async(() => {
        const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
        const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
            ['getInstitutionenForCurrentBenutzer']);
        const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
            ['principal$', 'getVisibleRolesForPrincipal']);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            declarations: [
                EinladungAbschliessenComponent
            ],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ]
        })
            .compileComponents();

        const superadmin = TestDataUtil.createSuperadmin();
        authServiceSpy.principal$ = of(superadmin) as any;
        authServiceSpy.getVisibleRolesForPrincipal.and.returnValue([]);
        insitutionSpy.getInstitutionenForCurrentBenutzer.and.returnValue(Promise.resolve([]));
        traegerschaftSpy.getAllTraegerschaften.and.returnValue(Promise.resolve([]));
        transitionSpy.params.and.returnValue({inputId: undefined});
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(EinladungAbschliessenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
