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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService} from '@uirouter/core';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';

import {AddSozialdienstComponent} from './add-sozialdienst.component';

describe('AddSozialdienstComponent', () => {
    let component: AddSozialdienstComponent;
    let fixture: ComponentFixture<AddSozialdienstComponent>;

    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const benutzerServiceSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['removeBenutzer']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const sozialdienstRSSpy = jasmine.createSpyObj<SozialdienstRS>(SozialdienstRS.name,
        ['createSozialdienst']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AddSozialdienstComponent],
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
            ],
            providers: [
                WindowRef,
                {provide: StateService, useValue: stateServiceSpy},
                {provide: SozialdienstRS, useValue: sozialdienstRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: BenutzerRS, useValue: benutzerServiceSpy},
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AddSozialdienstComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
