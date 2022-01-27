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
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {MassgebendesEinkommenComponent} from './resultat.component';

describe('ResultatComponent', () => {
    let component: MassgebendesEinkommenComponent;
    let fixture: ComponentFixture<MassgebendesEinkommenComponent>;

    const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(BerechnungsManager.name,
        ['calculateFinanzielleSituationTemp']);
    berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [MassgebendesEinkommenComponent],
            providers: [
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                NgForm
            ],
            imports: [
                SharedModule,
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));
        berechnungsManagerSpy.finanzielleSituationResultate = new TSFinanzielleSituationResultateDTO();
        fixture = TestBed.createComponent(MassgebendesEinkommenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
