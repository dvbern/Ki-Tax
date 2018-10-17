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
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import BenutzerRS from '../../core/service/benutzerRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {BenutzerRolleComponent} from '../../shared/component/benutzer-rolle/benutzer-rolle.component';
import {BerechtigungComponent} from '../../shared/component/berechtigung/berechtigung.component';
import {BenutzerEinladenComponent} from './benutzer-einladen.component';

describe('BenutzerEinladenComponent', () => {
    let component: BenutzerEinladenComponent;
    let fixture: ComponentFixture<BenutzerEinladenComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isRole', 'getGemeindenForPrincipal$', 'getVisibleRolesForPrincipal']);
    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenForCurrentBenutzer']);
    const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
    const gemeindeSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
    const benutzerSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['einladen']);

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
                UIRouterModule.forRoot(),
            ],
            declarations: [BenutzerEinladenComponent, BerechtigungComponent, BenutzerRolleComponent],
            providers: [
                {provide: APP_BASE_HREF, useValue: '/'},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeSpy},
                {provide: BenutzerRS, useValue: benutzerSpy},
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BenutzerEinladenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
