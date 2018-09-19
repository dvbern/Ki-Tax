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

import {APP_BASE_HREF} from '@angular/common';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {BenutzerRolleComponent} from './benutzer-rolle.component';

describe('BenutzerRolleComponent', () => {
    let component: BenutzerRolleComponent;
    let fixture: ComponentFixture<BenutzerRolleComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isRole']);

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            declarations: [BenutzerRolleComponent],
            providers: [
                {provide: APP_BASE_HREF, useValue: '/'},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: NgForm, useValue: new NgForm([], [])},
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BenutzerRolleComponent);
        component = fixture.componentInstance;
        component.name = 'test-name';
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
