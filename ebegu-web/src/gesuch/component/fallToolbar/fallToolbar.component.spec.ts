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

import {waitForAsync, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import {TranslateModule} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {DvNgShowElementDirective} from '../../../app/core/directive/dv-ng-show-element/dv-ng-show-element.directive';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDossier} from '../../../models/TSDossier';
import {TSFall} from '../../../models/TSFall';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {DossierRS} from '../../service/dossierRS.rest';
import {GemeindeRS} from '../../service/gemeindeRS.rest';
import {GesuchRS} from '../../service/gesuchRS.rest';
import {FallToolbarComponent} from './fallToolbar.component';

// tslint:disable:no-big-function
describe('fallToolbar', () => {

    const dossierId1 = 'ea02b313-e7c3-4b26-1122-e413f4041111';
    const dossierId2 = 'ea02b313-e7c3-4b26-1122-e413f4042222';
    const mockGesuchId = 'ea02b313-e7c3-4b26-1122-e413f4043333';

    let component: FallToolbarComponent;
    let fixture: ComponentFixture<FallToolbarComponent>;
    let fall: TSFall;
    let dossier1: TSDossier;
    let dossier2: TSDossier;
    let gemeinde1: TSGemeinde;
    let gemeinde2: TSGemeinde;
    let gemeinde3: TSGemeinde;
    const user = new TSBenutzer();

    beforeEach(waitForAsync(() => {
        // by default input values are empty/undefined
        initObjects();

        const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, {
            getAktiveGemeinden: Promise.resolve([gemeinde1, gemeinde2]),
        });
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, {
            getPrincipalRole: TSRole.SUPER_ADMIN,
            getPrincipal: user,
            isRole: false,
            isOneOfRoles: false,
        });
        authServiceSpy.principal$ = of(user) as any;

        const dossierServiceSpy = jasmine.createSpyObj<DossierRS>(DossierRS.name, {
            findDossiersByFall: Promise.resolve([dossier1, dossier2]),
        });
        const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
        const gesuchServiceSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name, {
            getIdOfNewestGesuchForDossier: Promise.resolve(mockGesuchId),
        });

        const applicationPropertySpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, {
            getKitaxUrl: Promise.resolve('http://google.com'),
        });

        TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                TranslateModule,
            ],
            providers: [
                {provide: DossierRS, useValue: dossierServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: GesuchRS, useValue: gesuchServiceSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertySpy},
            ],
            declarations: [
                FallToolbarComponent,
                DvNgShowElementDirective,
            ],
        });
    }));

    describe('functions', () => {
        beforeEach(waitForAsync(() => {
            initTestBed();
        }));

        it('should create', () => {
            expect(component).toBeTruthy();
        });
    });

    describe('isDossierActive', () => {
        beforeEach(waitForAsync(() => {
            initTestBed();
            component.fallId = fall.id;
        }));

        it('should return true for the selected dossier', done => {
            component.openDossier$(dossier1).subscribe(
                () => {
                    expect(component.isDossierActive(dossier1)).toBe(true);
                    done();
                },
                done.fail,
            );
        });

        it('should return false for a different dossier', done => {
            component.openDossier$(dossier1).subscribe(
                () => {
                    expect(component.isDossierActive(dossier2)).toBe(false);
                    done();
                },
                done.fail,
            );
        });
        it('should return false for undefined', done => {
            component.openDossier$(dossier1).subscribe(
                () => {
                    expect(component.isDossierActive(undefined)).toBe(false);
                    done();
                },
                done.fail,
            );
        });
        it('should return false for the no selected dossier', done => {
            component.openDossier$(undefined).subscribe(
                () => {
                    expect(component.isDossierActive(dossier1)).toBe(false);
                    done();
                },
                done.fail,
            );
        });
    });

    describe('ngOnChanges', () => {
        beforeEach(waitForAsync(() => {
            initTestBed();
        }));

        it('should reload all data when changing input values', fakeAsync(() => {
            component.dossierId = dossier2.id;
            component.fallId = fall.id;
            component.ngOnChanges({
                fallId: fall.id,
                dossierId: dossier2.id,
            });

            tick();

            expect(component.isDossierActive(dossier1)).toBe(false);
            expect(component.isDossierActive(dossier2)).toBe(true);
        }));
    });

    describe('showCreateNewDossier with available Gemeinden', () => {
        beforeEach(waitForAsync(() => {
            // we need a different testbed because we need to provide a different object
            const threeGemeindeServiceSpy = jasmine.createSpyObj('GemeindeRS', {
                getAktiveGemeinden: Promise.resolve([gemeinde1, gemeinde2, gemeinde3]),
            });
            TestBed.overrideProvider(GemeindeRS, {useValue: threeGemeindeServiceSpy});
            initTestBed();
        }));

        it('should return true if currentDossier is set and available Gemeinden', fakeAsync(() => {
            component.dossierId = dossier1.id;
            component.fallId = fall.id;
            component.currentDossier = undefined;
            component.ngOnChanges({fallId: component.fallId}); // to update all depending objects in the component

            tick();
            expect(component.showCreateNewDossier()).toBe(true);
        }));
    });

    describe('showCreateNewDossier with available Gemeinden but onlineGesuch', () => {
        beforeEach(waitForAsync(() => {
            // we need a different testbed because we need to provide a different object
            const threeGemeindeServiceSpy = jasmine.createSpyObj('GemeindeRS', {
                getAllGemeinden: Promise.resolve([gemeinde1, gemeinde2, gemeinde3]),
            });
            dossier1.fall.besitzer = new TSBenutzer(); // it  is now an onlineGesuch
            const dossierServiceSpy = jasmine.createSpyObj('DossierRS', {
                findDossiersByFall: Promise.resolve([dossier1, dossier2]),
            });
            TestBed.overrideProvider(GemeindeRS, {useValue: threeGemeindeServiceSpy});
            TestBed.overrideProvider(DossierRS, {useValue: dossierServiceSpy});
            initTestBed();
        }));

        it('should return false for non default values, available Gemeinden but onlineGesuch', fakeAsync(() => {
            component.dossierId = dossier1.id;
            component.fallId = fall.id;

            tick();
            expect(component.showCreateNewDossier()).toBe(false);
        }));
    });

    describe('showCreateNewDossier with no available Gemeinden', () => {
        beforeEach(waitForAsync(() => {
            initTestBed();
        }));

        it('should return false for default values', () => {
            expect(component.showCreateNewDossier()).toBe(false);
        });
        it('should return false for non default values and no available Gemeinden', fakeAsync(() => {
            component.dossierId = dossier2.id;
            component.fallId = fall.id;

            tick();
            expect(component.showCreateNewDossier()).toBe(false);
        }));
    });

    function initObjects(): void {
        gemeinde1 = TestDataUtil.createGemeindeLondon();
        gemeinde2 = TestDataUtil.createGemeindeParis();
        gemeinde3 = TestDataUtil.createGemeindeThun();

        fall = TestDataUtil.createFall();
        dossier1 = TestDataUtil.createDossier(dossierId1, fall);
        dossier1.gemeinde = gemeinde1;
        dossier2 = TestDataUtil.createDossier(dossierId2, fall);
        dossier2.gemeinde = gemeinde2;
    }

    function initTestBed(): void {
        TestBed.compileComponents();
        fixture = TestBed.createComponent(FallToolbarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }
});
