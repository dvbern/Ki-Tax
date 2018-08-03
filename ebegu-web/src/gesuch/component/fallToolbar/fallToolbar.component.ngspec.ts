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

import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {MatDialogModule} from '@angular/material';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import TSDossier from '../../../models/TSDossier';
import TSFall from '../../../models/TSFall';
import TSGemeinde from '../../../models/TSGemeinde';
import TSUser from '../../../models/TSUser';
import TestDataUtil from '../../../utils/TestDataUtil';
import DossierRS from '../../service/dossierRS.rest';
import GemeindeRS from '../../service/gemeindeRS.rest';
import {FallToolbarComponent} from './fallToolbar.component';

describe('fallToolbar', function () {

    const DOSSIER_ID_1 = 'ea02b313-e7c3-4b26-1122-e413f4041111';
    const DOSSIER_ID_2 = 'ea02b313-e7c3-4b26-1122-e413f4042222';

    let component: FallToolbarComponent;
    let fixture: ComponentFixture<FallToolbarComponent>;
    let fall: TSFall;
    let dossier1: TSDossier;
    let dossier2: TSDossier;
    let gemeinde1: TSGemeinde;
    let gemeinde2: TSGemeinde;
    let gemeinde3: TSGemeinde;
    let user: TSUser;


    beforeEach(async(() => {
        // by default input values are empty/undefined
        initObjects();

        const gemeindeServiceSpy = jasmine.createSpyObj('GemeindeRS', {
            'getAllGemeinden': Promise.resolve([gemeinde1, gemeinde2]),
        });
        const authServiceSpy = jasmine.createSpyObj('AuthServiceRS', {
            'getPrincipalRole': Promise.resolve(TSRole.SUPER_ADMIN),
            'getPrincipal': Promise.resolve(user),
        });
        const dossierServiceSpy = jasmine.createSpyObj('DossierRS', {
            'findDossiersByFall': Promise.resolve([dossier1, dossier2])
        });

        TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
            ],
            providers: [
                {provide: DossierRS, useValue: dossierServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
            ],
            declarations: [FallToolbarComponent]
        });
    }));

    describe('functions', function () {
        beforeEach(async(() => {
            initTestBed();
        }));

        it('should create', () => {
            expect(component).toBeTruthy();
        });
    });

    describe('isDossierActive', function () {
        beforeEach(async(() => {
            initTestBed();
        }));

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

    describe('ngOnChanges', function () {
        beforeEach(async(() => {
            initTestBed();
        }));

        it('should reload all data when changing input values', fakeAsync( () => {
            component.dossierId = dossier2.id;
            component.fallId = fall.id;
            component.ngOnChanges({
                'fallId': fall.id,
                'dossierId': dossier2.id,
            });

            tick();

            expect(component.isDossierActive(dossier1)).toBe(false);
            expect(component.isDossierActive(dossier2)).toBe(true);
        }));
    });

    describe('showCreateNewDossier', function () {
        it('should return false for default values', () => {
            initTestBed();
            expect(component.showCreateNewDossier()).toBe(false);
        });
        it('should return true for non default values and available Gemeinden', fakeAsync(() => {
            // we need a different testbed because we need to provide a different object
            const threeGemeindeServiceSpy = jasmine.createSpyObj('GemeindeRS', {
                'getAllGemeinden': Promise.resolve([gemeinde1, gemeinde2, gemeinde3]),
            });
            TestBed.overrideProvider(GemeindeRS, {useValue: threeGemeindeServiceSpy});
            initTestBed();

            component.dossierId = dossier1.id;
            component.fallId = fall.id;
            component.ngOnInit(); // to update all depending objects in the component

            tick();
            expect(component.showCreateNewDossier()).toBe(true);
        }));
        it('should return false for non default values and no available Gemeinden', fakeAsync(() => {
            initTestBed();
            component.dossierId = dossier2.id;
            component.fallId = fall.id;
            component.ngOnInit(); // to update all depending objects in the component

            tick();
            expect(component.showCreateNewDossier()).toBe(false);
        }));
    });


    function initObjects() {
        gemeinde1 = TestDataUtil.createGemeindeOstermundigen();
        gemeinde2 = TestDataUtil.createGemeindeBern();
        gemeinde3 = TestDataUtil.createGemeindeThun();

        fall = TestDataUtil.createFall();
        dossier1 = TestDataUtil.createDossier(DOSSIER_ID_1, fall);
        dossier1.gemeinde = gemeinde1;
        dossier2 = TestDataUtil.createDossier(DOSSIER_ID_2, fall);
        dossier2.gemeinde = gemeinde2;

        user = new TSUser();
    }

    function initTestBed() {
        TestBed.compileComponents();
        fixture = TestBed.createComponent(FallToolbarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }
});
