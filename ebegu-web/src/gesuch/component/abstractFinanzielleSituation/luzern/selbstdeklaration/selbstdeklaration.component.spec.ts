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
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

import {SelbstdeklarationComponent} from './selbstdeklaration.component';

describe('SelbstdeklarationComponent', () => {
    let component: SelbstdeklarationComponent;
    let fixture: ComponentFixture<SelbstdeklarationComponent>;
    const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(BerechnungsManager.name, ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']);
    berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));
    const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name, ['getGesuch', 'getBasisjahr']);
    gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SelbstdeklarationComponent],
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
        fixture = TestBed.createComponent(SelbstdeklarationComponent);
        component = fixture.componentInstance;
        component.model = new TSFinanzielleSituation();
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
