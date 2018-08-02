/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import TSDossier from '../../../models/TSDossier';
import TSFall from '../../../models/TSFall';
import TestDataUtil from '../../../utils/TestDataUtil';
import DossierRS from '../../service/dossierRS.rest';
import FallRS from '../../service/fallRS.rest';
import {FallToolbarComponent} from './fallToolbar.component';

describe('fallToolbar', function () {

    const DOSSIER_ID_1 = 'ea02b313-e7c3-4b26-1122-e413f4041111';
    const DOSSIER_ID_2 = 'ea02b313-e7c3-4b26-1122-e413f4042222';

    let component: FallToolbarComponent;
    let fixture: ComponentFixture<FallToolbarComponent>;
    let fall: TSFall;
    let dossier1: TSDossier;
    let dossier2: TSDossier;
    let dossierList: TSDossier[];


    beforeEach(async(() => {
        fall = TestDataUtil.createFall();
        dossier1 = TestDataUtil.createDossier(DOSSIER_ID_1, fall);
        dossier2 = TestDataUtil.createDossier(DOSSIER_ID_2, fall);
        dossierList = [dossier1, dossier2];

        const dossierServiceSpy = jasmine.createSpyObj('DossierRS', {
            'findDossiersByFall': new Promise(() => dossierList)
        });
        const fallServiceSpy = jasmine.createSpyObj('FallRS', {
            'findFall': new Promise(() => fall)
        });

        TestBed.configureTestingModule({
            imports: [
            ],
            providers: [
                {provide: DossierRS, useValue: dossierServiceSpy},
                {provide: FallRS, useValue: fallServiceSpy},
            ],
            declarations: [FallToolbarComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(FallToolbarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    describe('functions', function () {
        it('should create', () => {
            expect(component).toBeTruthy();
        });
    });

    fdescribe('isDossierActive', function () {
        it('should return true for the selected dossier', () => {
            component.openDossier(dossier1);
            expect(component.isDossierActive(dossier1)).toBe(true);
        });
        it('should return false for a different dossier', () => {
            component.openDossier(dossier1);
            expect(component.isDossierActive(dossier2)).toBe(false);
        });
        it('should return false for undefined', () => {
            component.openDossier(dossier1);
            expect(component.isDossierActive(undefined)).toBe(false);
        });
        it('should return false for the no selected dossier', () => {
            component.openDossier(undefined);
            expect(component.isDossierActive(dossier1)).toBe(false);
        });
    });

});
