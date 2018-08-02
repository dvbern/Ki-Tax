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
import DossierRS from '../../service/dossierRS.rest';
import FallRS from '../../service/fallRS.rest';
import {FallToolbarComponent} from './fallToolbar.component';

describe('fallToolbar', function () {

    let component: FallToolbarComponent;
    let fixture: ComponentFixture<FallToolbarComponent>;

    beforeEach(async(() => {
        const fall = new TSFall();
        const dossierServiceSpy = jasmine.createSpyObj('DossierRS', ['findDossiersByFall']);
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

    describe('isDossierActive', function () {
        it('should return true for the selected dossier', () => {
            let dossier = createDossier('1111');
            component.selectedDossier = dossier;
            expect(component.isDossierActive(dossier)).toBe(true);
        });
        it('should return false for a different dossier', () => {
            component.selectedDossier = createDossier('1111');
            let dossier2 = createDossier('2222');
            expect(component.isDossierActive(dossier2)).toBe(false);
        });
        it('should return false for undefined', () => {
            component.selectedDossier = createDossier('1111');
            expect(component.isDossierActive(undefined)).toBe(false);
        });
        it('should return false for the no selected dossier', () => {
            let dossier = createDossier('1111');
            component.selectedDossier = undefined;
            expect(component.isDossierActive(dossier)).toBe(false);
        });
    });

    function createDossier(id: string) {
        let dossier = new TSDossier();
        dossier.id = id;
        return dossier;
    }

});
