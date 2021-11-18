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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateService} from '@ngx-translate/core';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {MaterialModule} from '../../../shared/material.module';
import {SharedModule} from '../../../shared/shared.module';
import {WindowRef} from '../../service/windowRef.service';

import {DvBisherXComponent} from './dv-bisher-x.component';

const translateSpy = jasmine.createSpyObj<TranslateService>(
    TranslateService.name, ['instant', 'setDefaultLang', 'use']
);
const dvBisherClass = '.dv-bisher-content-row';

describe('DvBisherXComponent', () => {
    let component: DvBisherXComponent;
    let fixture: ComponentFixture<DvBisherXComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DvBisherXComponent],
            imports: [
                MaterialModule,
                BrowserAnimationsModule,
                SharedModule,
            ],
            providers: [
                WindowRef,
                {provide: TranslateService, useValue: translateSpy}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DvBisherXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display dv-bisher', () => {
        component.deklaration = 10;
        component.korrektur = 11;
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css(dvBisherClass))).not.toBeNull();
    });

    it('should not display dv-bisher', () => {
        component.deklaration = 10;
        component.korrektur = 10;
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css(dvBisherClass))).toBeNull();
    });

    it('should not display dv-bisher because showBisher is false', () => {
        component.deklaration = 10;
        component.korrektur = 11;
        component.showBisher = false;
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css(dvBisherClass))).toBeNull();
    });

    it('should display dv-bisher for string and number', () => {
        component.deklaration = 10;
        component.korrektur = '11';
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css(dvBisherClass))).not.toBeNull();
    });
});
