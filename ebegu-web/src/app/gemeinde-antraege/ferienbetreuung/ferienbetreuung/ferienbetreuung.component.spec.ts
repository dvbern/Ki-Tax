/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);
const ferienbetreuungServiceSpy = jasmine.createSpyObj<FerienbetreuungService>(FerienbetreuungService.name,
    ['updateFerienbetreuungContainerStore', 'getFerienbetreuungContainer', 'emptyStore']);
const wizardStepXRSSpy = jasmine.createSpyObj<WizardStepXRS>(WizardStepXRS.name,
    ['updateSteps']);

import {HttpClientModule} from '@angular/common/http';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {WindowRef} from '../../../core/service/windowRef.service';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {SharedModule} from '../../../shared/shared.module';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

import {FerienbetreuungComponent} from './ferienbetreuung.component';

describe('FerienbetreuungComponent', () => {
    let component: FerienbetreuungComponent;
    let fixture: ComponentFixture<FerienbetreuungComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungComponent],
            providers: [
                WindowRef,
                { provide: AuthServiceRS, useValue: authServiceRSSpy },
                { provide: WizardStepXRS, useValue: wizardStepXRSSpy },
                { provide: FerienbetreuungService, useValue: ferienbetreuungServiceSpy },
            ],
            imports: [
                HttpClientModule,
                SharedModule
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FerienbetreuungComponent);
        component = fixture.componentInstance;
        ferienbetreuungServiceSpy.getFerienbetreuungContainer.and.returnValue(
            of(new TSFerienbetreuungAngabenContainer())
        );
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
