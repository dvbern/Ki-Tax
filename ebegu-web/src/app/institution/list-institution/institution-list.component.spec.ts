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
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {InstitutionListComponent} from './institution-list.component';

describe('InstitutionListComponent', () => {

    let component: InstitutionListComponent;
    let fixture: ComponentFixture<InstitutionListComponent>;

    beforeEach(waitForAsync(() => {
        const insitutionServiceSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
            [
                'getInstitutionenEditableForCurrentBenutzer',
                'getInstitutionenListDTOEditableForCurrentBenutzer'
            ]);
        const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
        const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
            ['isRole', 'isOneOfRoles', 'principal$']);
        const i18nServiceSpy = jasmine
            .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
        const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
            ['getPublicPropertiesCached', 'getInstitutionenDurchGemeindenEinladen']);
        applicationPropertyRSSpy.getPublicPropertiesCached.and.returnValue(Promise.resolve(new TSPublicAppConfig()));

        authServiceSpy.principal$ = of(new TSBenutzer());

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule
            ],
            providers: [
                {provide: InstitutionRS, useValue: insitutionServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy}
            ],
            declarations: [InstitutionListComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        insitutionServiceSpy.getInstitutionenEditableForCurrentBenutzer.and.returnValue(of([]));
        insitutionServiceSpy.getInstitutionenListDTOEditableForCurrentBenutzer.and.returnValue(of([]));
        applicationPropertyRSSpy.getInstitutionenDurchGemeindenEinladen.and.returnValue(Promise.resolve(false));

    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(InstitutionListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
