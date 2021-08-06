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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {DownloadRS} from '../../../../core/service/downloadRS.rest';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

import {LastenausgleichTsBerechnungComponent} from './lastenausgleich-ts-berechnung.component';

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
const translateSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name, ['instant']);
const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name, ['openDownload']);
const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['principal$', 'isOneOfRoles']);
const lastenausgleichTSServiceSpy = jasmine.createSpyObj<LastenausgleichTSService>(LastenausgleichTSService.name,
    ['getLATSAngabenGemeindeContainer', 'latsDocxErstellen']);

describe('LastenausgleichTsBerechnungComponent', () => {
    let component: LastenausgleichTsBerechnungComponent;
    let fixture: ComponentFixture<LastenausgleichTsBerechnungComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [LastenausgleichTsBerechnungComponent],
            providers: [
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TranslateService, useValue: translateSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: LastenausgleichTSService, useValue: lastenausgleichTSServiceSpy},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LastenausgleichTsBerechnungComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
