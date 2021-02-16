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
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {BenutzerEinladenComponent} from './benutzer-einladen.component';

describe('BenutzerEinladenComponent', () => {
    let component: BenutzerEinladenComponent;
    let fixture: ComponentFixture<BenutzerEinladenComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isRole', 'getVisibleRolesForPrincipal']);
    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenEditableForCurrentBenutzer']);
    const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
    const gemeindeSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
    const sozialdienstRSSpy = jasmine.createSpyObj<SozialdienstRS>(SozialdienstRS.name,
        ['getSozialdienstList']);
    const benutzerSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['einladen']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);

    beforeEach(waitForAsync(() => {
        const superadmin = TestDataUtil.createSuperadmin();
        authServiceSpy.principal$ = of(superadmin) as any;
        authServiceSpy.getVisibleRolesForPrincipal.and.returnValue([]);
        insitutionSpy.getInstitutionenEditableForCurrentBenutzer.and.resolveTo([]);
        traegerschaftSpy.getAllTraegerschaften.and.resolveTo([]);
        gemeindeSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
        sozialdienstRSSpy.getSozialdienstList.and.resolveTo([]);
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                UIRouterModule.forRoot(),
            ],
            declarations: [BenutzerEinladenComponent],
            providers: [
                {provide: APP_BASE_HREF, useValue: '/'},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeSpy},
                {provide: BenutzerRS, useValue: benutzerSpy},
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
                {provide: SozialdienstRS, useValue: sozialdienstRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
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
