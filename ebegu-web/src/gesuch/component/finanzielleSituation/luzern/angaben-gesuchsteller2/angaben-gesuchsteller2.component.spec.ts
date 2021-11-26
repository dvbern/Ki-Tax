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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchsteller} from '../../../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';

import {AngabenGesuchsteller2Component} from './angaben-gesuchsteller2.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name,
    ['areThereOnlyFerieninsel', 'getBasisjahr', 'getBasisjahrPlus', 'getGesuch', 'isGesuchsteller2Required']);
const wizardStepMangerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name, ['getCurrentStep', 'setCurrentStep']);
const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(FinanzielleSituationRS.name, []);

describe('AngabenGesuchsteller2Component', () => {
    let component: AngabenGesuchsteller2Component;
    let fixture: ComponentFixture<AngabenGesuchsteller2Component>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AngabenGesuchsteller2Component],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepMangerSpy},
                {provide: FinanzielleSituationRS, useValue: finanzielleSituationRSSpy},
            ],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                SharedModule,
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
        gesuchModelManagerSpy.isGesuchsteller2Required.and.returnValue(false);
        fixture = TestBed.createComponent(AngabenGesuchsteller2Component);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller1.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller2.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller1.finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        gesuch.gesuchsteller2.finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA = new TSFamiliensituation();
        return gesuch;
    }
});
