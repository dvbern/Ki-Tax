/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {SharedModule} from '../../shared/shared.module';

import {NotrechtComponent} from './notrecht.component';

describe('NotrechtComponent', () => {
    let component: NotrechtComponent;
    let fixture: ComponentFixture<NotrechtComponent>;
    const notrechtRSSpy = jasmine.createSpyObj<NotrechtRS>(NotrechtRS.name,
        ['initializeRueckforderungFormulare', 'getRueckforderungFormulareForCurrentBenutzer']);
    const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isRole', 'isOneOfRoles']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            declarations: [NotrechtComponent],
            providers: [
                WindowRef,
                {provide: NotrechtRS, useValue: notrechtRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NotrechtComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        spyOn(notrechtRSSpy, 'getRueckforderungFormulareForCurrentBenutzer')
            .and.returnValue( Promise.resolve([] ));
        expect(component).toBeTruthy();
    });
});
