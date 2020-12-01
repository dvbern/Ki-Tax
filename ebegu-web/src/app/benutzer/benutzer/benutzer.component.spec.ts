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
import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {Transition, UIRouterModule} from '@uirouter/angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {BenutzerComponent} from './benutzer.component';

describe('BenutzerComponent', () => {
    let component: BenutzerComponent;
    let fixture: ComponentFixture<BenutzerComponent>;

    beforeEach(waitForAsync(() => {
        const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name, ['getAllInstitutionen']);
        const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
        const benutzerSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name,
            [
                'getBerechtigungHistoriesForBenutzer', 'saveBenutzerBerechtigungen', 'findBenutzer',
                'inactivateBenutzer', 'reactivateBenutzer',
            ]);

        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isRole']);
        const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
        const i18nServiceSpy = jasmine
            .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);

        TestBed.configureTestingModule({
            imports: [SharedModule, UIRouterModule.forRoot()],
            declarations: [BenutzerComponent],
            providers: [
                {
                    provide: InstitutionRS,
                    useValue: insitutionSpy,
                },
                {
                    provide: TraegerschaftRS,
                    useValue: traegerschaftSpy,
                },
                {
                    provide: BenutzerRS,
                    useValue: benutzerSpy,
                },
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: APP_BASE_HREF, useValue: '/'},
                {provide: Transition, useValue: transitionSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
            ],
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
