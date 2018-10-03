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

import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {BenutzerRolleComponent} from '../benutzer-rolle/benutzer-rolle.component';

import {BerechtigungComponent} from './berechtigung.component';

describe('BerechtigungComponent', () => {
    let component: BerechtigungComponent;
    let fixture: ComponentFixture<BerechtigungComponent>;

    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenForCurrentBenutzer']);
    const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isRole', 'getVisibleRolesForPrincipal', 'principal$']);
    const gemeindeSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);

    const inputSelector = '.dv-input-container-medium';

    beforeEach(async(() => {
        const superadmin = TestDataUtil.createSuperadmin();
        authServiceSpy.principal$ = of(superadmin) as any;
        authServiceSpy.getVisibleRolesForPrincipal.and.returnValue([]);
        insitutionSpy.getInstitutionenForCurrentBenutzer.and.returnValue([]);
        traegerschaftSpy.getAllTraegerschaften.and.returnValue([]);
        gemeindeSpy.getGemeindenForPrincipal$.and.returnValue(of([]));

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            declarations: [BerechtigungComponent, BenutzerRolleComponent],
            providers: [
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
                {provide: GemeindeRS, useValue: gemeindeSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: NgForm, useValue: new NgForm([], [])},
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BerechtigungComponent);
        component = fixture.componentInstance;
        component.berechtigung = new TSBerechtigung();
    });

    it('should create', () => {
        fixture.detectChanges();
        expect(component).toBeTruthy();
    });

    it('should load institutionen and traegerschaften', () => {
        fixture.detectChanges();
        expect(insitutionSpy.getInstitutionenForCurrentBenutzer).toHaveBeenCalled();
    });

    it('should display gemeinde when gemeinde dependent role', () => {
        component.berechtigung.role = TSRole.ADMIN_GEMEINDE;
        expect(component.berechtigung.hasGemeindeRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(2);

        expect(fixture.debugElement.query(By.css('dv-gemeinde-multiselect'))).toBeTruthy();
    });

    it('should display institution when institution dependent role', () => {
        component.berechtigung.role = TSRole.SACHBEARBEITER_INSTITUTION;
        expect(component.berechtigung.hasInstitutionRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(2);

        expect(fixture.debugElement.query(By.css('[id^=institution-]'))).toBeTruthy();
    });

    it('should display institution when institution dependent role', () => {
        component.berechtigung.role = TSRole.SACHBEARBEITER_TRAEGERSCHAFT;
        expect(component.berechtigung.hasTraegerschaftRole()).toBe(true);
        fixture.detectChanges();

        const debugElements = fixture.debugElement.queryAll(By.css(inputSelector));
        expect(debugElements.length).toBe(2);

        expect(fixture.debugElement.query(By.css('[id^=treagerschaft-]'))).toBeTruthy();
    });
});
