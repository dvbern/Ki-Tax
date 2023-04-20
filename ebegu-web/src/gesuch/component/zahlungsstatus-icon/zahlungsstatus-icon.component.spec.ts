/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuung} from '../../../models/TSBetreuung';

import {ZahlungsstatusIconComponent} from './zahlungsstatus-icon.component';

describe('ZahlungsstatusIconComponent', () => {
    let component: ZahlungsstatusIconComponent;
    let fixture: ComponentFixture<ZahlungsstatusIconComponent>;

    const translateSpy = jasmine.createSpyObj<TranslateService>(
        TranslateService.name, ['instant']
    );
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name, ['isRole']
    );

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ZahlungsstatusIconComponent],
            providers: [
                {provide: TranslateService, useValue: translateSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ZahlungsstatusIconComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
