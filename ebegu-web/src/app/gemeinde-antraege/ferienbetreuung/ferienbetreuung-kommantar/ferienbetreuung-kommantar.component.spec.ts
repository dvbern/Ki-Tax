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

import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {FerienbetreuungKommantarComponent} from './ferienbetreuung-kommantar.component';

const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name,
    ['addMesageAsError']);

const translateServiceSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name,
    ['instant']);

const einstellungRSSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name,
    ['getPauschalbetraegeFerienbetreuung']);

describe('FerienbetreuungKommantarComponent', () => {
    let component: FerienbetreuungKommantarComponent;
    let fixture: ComponentFixture<FerienbetreuungKommantarComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungKommantarComponent],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TranslateService, useValue: translateServiceSpy},
                {provide: EinstellungRS, useValue: einstellungRSSpy},
            ],
            imports: [
                HttpClientModule
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FerienbetreuungKommantarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
