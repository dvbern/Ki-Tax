/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {TSMandant} from '../../../models/TSMandant';
import {KiBonMandant} from '../../core/constants/MANDANTS';
import {MandantService} from '../../shared/services/mandant.service';

import {PortalSelectionComponent} from './portal-selection.component';

describe('PortalSelectionComponent', () => {
    let component: PortalSelectionComponent;
    let fixture: ComponentFixture<PortalSelectionComponent>;

    beforeEach(async () => {

        const mandantService = jasmine.createSpyObj<MandantService>(MandantService.name,
            ['mandant$', 'getAll', 'mandantToKibonMandant', 'getMandantLogoName']);
        const uiRouterGlobals = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name,
            ['$current']);

        mandantService.getAll.and.returnValue(of([new TSMandant()]));
        mandantService.mandantToKibonMandant.and.returnValue(KiBonMandant.BE);
        await TestBed.configureTestingModule({
            declarations: [PortalSelectionComponent],
            providers: [
                {provide: MandantService, useValue: mandantService},
                {provide: UIRouterGlobals, useValue: uiRouterGlobals},
            ],
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PortalSelectionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
