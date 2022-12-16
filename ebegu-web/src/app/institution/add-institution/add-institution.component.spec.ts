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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {BenutzerRSX} from '../../core/service/benutzerRSX.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {AddInstitutionComponent} from './add-institution.component';

describe('AddInstitutionComponent', () => {

    let component: AddInstitutionComponent;
    let fixture: ComponentFixture<AddInstitutionComponent>;

    const insitutionServiceSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenReadableForCurrentBenutzer']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const benutzerServiceSpy = jasmine.createSpyObj<BenutzerRSX>(BenutzerRSX.name, ['removeBenutzer']);
    const transitionServiceSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name,
        ['getAllTraegerschaften', 'getAllActiveTraegerschaften']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['getPrincipal']);
    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
        ['getGemeindenForPrincipal$']);
    const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
        ['getPublicPropertiesCached', 'getInstitutionenDurchGemeindenEinladen']);

    beforeEach(waitForAsync(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule
            ],
            providers: [
                {provide: Transition, useValue: transitionServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: InstitutionRS, useValue: insitutionServiceSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftSpy},
                {provide: BenutzerRSX, useValue: benutzerServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy}
            ],
            declarations: [
                AddInstitutionComponent
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        traegerschaftSpy.getAllTraegerschaften.and.returnValue(Promise.resolve([]));
        traegerschaftSpy.getAllActiveTraegerschaften.and.returnValue(Promise.resolve([]));
        transitionServiceSpy.params.and.returnValue({institutionId: undefined});
        authServiceSpy.getPrincipal.and.returnValue(TestDataUtil.createSuperadmin());
        applicationPropertyRSSpy.getPublicPropertiesCached.and.returnValue(of(new TSPublicAppConfig()).toPromise());
        applicationPropertyRSSpy.getInstitutionenDurchGemeindenEinladen.and.returnValue(of(false).toPromise());
        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(AddInstitutionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
