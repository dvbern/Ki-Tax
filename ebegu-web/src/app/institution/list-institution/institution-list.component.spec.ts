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
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {InstitutionRSX} from '../../core/service/institutionRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {InstitutionListComponent} from './institution-list.component';

describe('InstitutionListComponent', () => {

    let component: InstitutionListComponent;
    let fixture: ComponentFixture<InstitutionListComponent>;

    beforeEach(waitForAsync(() => {
        const insitutionServiceSpy = jasmine.createSpyObj<InstitutionRSX>(InstitutionRSX.name,
            [
                'getInstitutionenEditableForCurrentBenutzer',
                'getInstitutionenListDTOEditableForCurrentBenutzer',
            ]);
        const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
        const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
            ['isRole', 'isOneOfRoles', 'principal$']);
        const i18nServiceSpy = jasmine
            .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

        authServiceSpy.principal$ = of(new TSBenutzer());

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: InstitutionRSX, useValue: insitutionServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
            declarations: [InstitutionListComponent],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        insitutionServiceSpy.getInstitutionenEditableForCurrentBenutzer.and.returnValue(Promise.resolve([]));
        insitutionServiceSpy.getInstitutionenListDTOEditableForCurrentBenutzer.and.returnValue(Promise.resolve([]));

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
