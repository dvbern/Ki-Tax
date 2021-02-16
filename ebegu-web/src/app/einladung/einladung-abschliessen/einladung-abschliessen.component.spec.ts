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

import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {EinladungAbschliessenComponent} from './einladung-abschliessen.component';

describe('EinladungAbschliessenComponent', () => {
    let component: EinladungAbschliessenComponent;
    let fixture: ComponentFixture<EinladungAbschliessenComponent>;
    let superadmin: TSBenutzer;

    beforeEach(waitForAsync(() => {
        const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
        const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
            ['getInstitutionenEditableForCurrentBenutzer']);
        const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);
        const sozialdienstRSSpy = jasmine.createSpyObj<SozialdienstRS>(SozialdienstRS.name,
            ['getSozialdienstList']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
            ['getVisibleRolesForPrincipal']);
        const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
            ['uploadLogoImage', 'getLogoUrl', 'getGemeindenForPrincipal$']);
        const i18nServiceSpy = jasmine
            .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

        superadmin = TestDataUtil.createSuperadmin();
        authServiceSpy.principal$ = of(superadmin) as any;
        authServiceSpy.getVisibleRolesForPrincipal.and.returnValue([]);
        insitutionSpy.getInstitutionenEditableForCurrentBenutzer.and.returnValue(Promise.resolve([]));
        traegerschaftSpy.getAllTraegerschaften.and.returnValue(Promise.resolve([]));
        sozialdienstRSSpy.getSozialdienstList.and.resolveTo([]);
        transitionSpy.params.and.returnValue({inputId: undefined});

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            declarations: [
                EinladungAbschliessenComponent,
            ],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: InstitutionRS, useValue: insitutionSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: SozialdienstRS, useValue: sozialdienstRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(EinladungAbschliessenComponent);
        component = fixture.componentInstance;
        component.principal = superadmin;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
