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
import {TranslateModule} from '@ngx-translate/core';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {WindowRef} from '../../../core/service/windowRef.service';
import {SharedModule} from '../../../shared/shared.module';
import {LastenausgleichTSService} from '../services/lastenausgleich-ts.service';

import {LastenausgleichTsKommentarComponent} from './lastenausgleich-ts-kommentar.component';

const lastenausgleichTSServiceSpy = jasmine.createSpyObj<LastenausgleichTSService>(LastenausgleichTSService.name,
    ['getLATSAngabenGemeindeContainer']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);

describe('LastenausgleichTsKommentarComponent', () => {
    let component: LastenausgleichTsKommentarComponent;
    let fixture: ComponentFixture<LastenausgleichTsKommentarComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                SharedModule,
                BrowserAnimationsModule,
                FormsModule,
                ReactiveFormsModule,
                TranslateModule.forRoot()
            ],
            declarations: [
                LastenausgleichTsKommentarComponent
            ],
            providers: [
                WindowRef,
                {provide: LastenausgleichTSService, useValue: lastenausgleichTSServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        lastenausgleichTSServiceSpy.getLATSAngabenGemeindeContainer.and.returnValue(
            of(new TSLastenausgleichTagesschuleAngabenGemeindeContainer())
        );
        fixture = TestBed.createComponent(LastenausgleichTsKommentarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
