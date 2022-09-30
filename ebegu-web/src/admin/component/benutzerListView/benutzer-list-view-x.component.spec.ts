/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService} from '@uirouter/core';
import {BenutzerRSX} from '../../../app/core/service/benutzerRSX.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';

import {BenutzerListViewXComponent} from './benutzer-list-view-x.component';

describe('BenutzerListViewXComponent', () => {
    let component: BenutzerListViewXComponent;
    let fixture: ComponentFixture<BenutzerListViewXComponent>;

    const benutzerRSSpy = jasmine.createSpyObj(BenutzerRSX.name, ['searchUsers']);
    const authServiceRSSpy = jasmine.createSpyObj(AuthServiceRS.name, [
        'isOneOfRoles',
        'getVisibleRolesForPrincipal'
    ]);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            providers: [
                {provide: BenutzerRSX, useValue: benutzerRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: StateService, useValue: stateServiceSpy}
            ],
            declarations: [BenutzerListViewXComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(BenutzerListViewXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
