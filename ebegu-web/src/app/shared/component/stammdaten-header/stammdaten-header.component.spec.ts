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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService} from '@uirouter/core';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {StammdatenHeaderComponent} from './stammdaten-header.component';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';

describe('StammdatenHeaderComponent', () => {

    const url = 'http://logo.png';
    let component: StammdatenHeaderComponent;
    let fixture: ComponentFixture<StammdatenHeaderComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['isSupportedImage']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
            ],
            declarations: [
                StammdatenHeaderComponent,
            ],
        }).compileComponents();
    }));

    beforeEach(async(() => {
        fixture = TestBed.createComponent(StammdatenHeaderComponent);
        component = fixture.componentInstance;
        component.logoImageUrl = url;
        fixture.detectChanges();
    }));

    it('should create', async(() => {
        expect(component).toBeTruthy();
    }));

});
