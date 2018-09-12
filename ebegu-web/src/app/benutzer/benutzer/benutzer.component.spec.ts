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
import {Transition, UIRouterModule} from '@uirouter/angular';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeMultiselectComponent} from '../../core/component/gemeinde-multiselect/gemeinde-multiselect.component';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import UserRS from '../../core/service/userRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {BenutzerRolleComponent} from '../benutzer-rolle/benutzer-rolle.component';

import {BenutzerComponent} from './benutzer.component';

describe('BenutzerComponent', () => {
    let component: BenutzerComponent;
    let fixture: ComponentFixture<BenutzerComponent>;

    beforeEach(async(() => {
        const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name, ['getAllInstitutionen']);
        const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
        const userSpy = jasmine.createSpyObj<UserRS>(UserRS.name,
            ['getBerechtigungHistoriesForBenutzer', 'saveBenutzerBerechtigungen', 'findBenutzer',
                'inactivateBenutzer', 'reactivateBenutzer']);

        const applicationPropertySpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['getByName']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isRole']);
        const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);

        TestBed.configureTestingModule({
            imports: [SharedModule, UIRouterModule.forRoot()],
            declarations: [BenutzerComponent, BenutzerRolleComponent],
            providers: [
                {
                    provide: InstitutionRS,
                    useValue: insitutionSpy
                },
                {
                    provide: TraegerschaftRS,
                    useValue: traegerschaftSpy
                },
                {
                    provide: UserRS,
                    useValue: userSpy
                },
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertySpy
                },
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: APP_BASE_HREF, useValue: '/'},
                {provide: Transition, useValue: transitionSpy},
            ]
        })
            .compileComponents();

        insitutionSpy.getAllInstitutionen.and.returnValue(Promise.resolve([]));
        traegerschaftSpy.getAllTraegerschaften.and.returnValue(Promise.resolve([]));
        transitionSpy.params.and.returnValue({benutzerId: undefined});
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BenutzerComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
