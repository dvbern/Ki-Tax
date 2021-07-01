/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/angular';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {WizardStepXRS} from '../../../../core/service/wizardStepXRS.rest';
import {MaterialModule} from '../../../../shared/material.module';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

import {FreigabeComponent} from './freigabe.component';

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
const translateServiceSpy = jasmine.createSpyObj<TranslateService>(ErrorService.name, ['instant']);
const lastenausgleichTSServiceSpy = jasmine.createSpyObj<LastenausgleichTSService>(LastenausgleichTSService.name,
    ['getLATSAngabenGemeindeContainer']);
const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go']);
const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['getPrincipal', 'isOneOfRoles']);
const wizardStepXRSSpy = jasmine.createSpyObj<WizardStepXRS>(WizardStepXRS.name,
    ['updateSteps']);

authServiceSpy.principal$ = of(new TSBenutzer());

describe('FreigabeComponent', () => {
    let component: FreigabeComponent;
    let fixture: ComponentFixture<FreigabeComponent>;

    lastenausgleichTSServiceSpy.getLATSAngabenGemeindeContainer.and.returnValue(
        of(new TSLastenausgleichTagesschuleAngabenGemeindeContainer()),
    );

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            declarations: [FreigabeComponent],
            imports: [MaterialModule],
            providers: [
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TranslateService, useValue: translateServiceSpy},
                {provide: LastenausgleichTSService, useValue: lastenausgleichTSServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: WizardStepXRS, useValue: wizardStepXRSSpy}
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(FreigabeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
