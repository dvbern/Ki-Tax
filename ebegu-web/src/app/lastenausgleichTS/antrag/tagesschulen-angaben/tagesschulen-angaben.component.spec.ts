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

import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {WindowRef} from '../../../core/service/windowRef.service';
import {MaterialModule} from '../../../shared/material.module';
import {SharedModule} from '../../../shared/shared.module';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

import {TagesschulenAngabenComponent} from './tagesschulen-angaben.component';

const lastenausgleichTSServiceSpy = jasmine.createSpyObj<LastenausgleichTSService>(LastenausgleichTSService.name,
    ['getLATSAngabenGemeindeContainer']);
const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
const einstellungServiceSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name, ['saveEinstellung']);
describe('TagesschulenAngabenComponent', () => {
    let component: TagesschulenAngabenComponent;
    let fixture: ComponentFixture<TagesschulenAngabenComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                FormsModule,
                ReactiveFormsModule,
                SharedModule,
                HttpClientModule,
                MaterialModule,
                BrowserAnimationsModule,
            ],
            declarations: [TagesschulenAngabenComponent],
            providers: [
                WindowRef,
                {provide: LastenausgleichTSService, useValue: lastenausgleichTSServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: EinstellungRS, useValue: einstellungServiceSpy},
            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        lastenausgleichTSServiceSpy.getLATSAngabenGemeindeContainer.and.returnValue(
            of(new TSLastenausgleichTagesschuleAngabenGemeindeContainer()),
        );
        fixture = TestBed.createComponent(TagesschulenAngabenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
