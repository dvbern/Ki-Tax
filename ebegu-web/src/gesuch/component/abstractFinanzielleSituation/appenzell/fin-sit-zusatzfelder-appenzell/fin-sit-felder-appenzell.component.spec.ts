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
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../../../../../models/TSFinSitZusatzangabenAppenzell';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

import {FinSitFelderAppenzellComponent} from './fin-sit-felder-appenzell.component';

describe('FinanzielleVerhaeltnisseComponent', () => {
    let component: FinSitFelderAppenzellComponent;
    let fixture: ComponentFixture<FinSitFelderAppenzellComponent>;
    const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(BerechnungsManager.name, ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']);
    berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));
    const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name, ['getGesuch', 'getBasisjahr']);
    gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FinSitFelderAppenzellComponent],
            imports: [SharedModule],
            providers: [
                {provide: NgForm, useValue: new NgForm([], [])},
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FinSitFelderAppenzellComponent);
        component = fixture.componentInstance;
        component.finSitZusatzangabenAppenzell = new TSFinSitZusatzangabenAppenzell();
        component.finSitZusatzangabenAppenzell.zusatzangabenPartner = new TSFinSitZusatzangabenAppenzell();
        component.finanzModel = new TSFinanzModel(0, false, 1);
        component.finanzModel.finanzielleSituationContainerGS1 = new TSFinanzielleSituationContainer();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        return gesuch;
    }
});
