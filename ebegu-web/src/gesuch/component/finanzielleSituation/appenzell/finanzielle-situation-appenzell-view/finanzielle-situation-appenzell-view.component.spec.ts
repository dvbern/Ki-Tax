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
import {StateService} from '@uirouter/angular';
import {Transition} from '@uirouter/core';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {TSWizardStep} from '../../../../../models/TSWizardStep';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {FinanzielleSituationAppenzellViewComponent} from './finanzielle-situation-appenzell-view.component';

describe('FinanzielleSituationAppenzellViewComponent', () => {
    let component: FinanzielleSituationAppenzellViewComponent;
    let fixture: ComponentFixture<FinanzielleSituationAppenzellViewComponent>;

    const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(BerechnungsManager.name,
        ['calculateFinanzielleSituationTemp']);
    berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));
    const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
        ['getGesuch', 'getBasisjahr', 'isGesuchsteller2Required', 'setGesuchstellerNumber', 'isGesuchReadonly', 'getGesuchsperiode', 'isSpezialFallAR']);
    gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
    gesuchModelManagerSpy.isSpezialFallAR.and.returnValue(false);
    const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(WizardStepManager.name,
        ['setCurrentStep',
            'isNextStepBesucht',
            'isNextStepEnabled',
            'getCurrentStepName',
            'updateCurrentWizardStepStatusSafe',
            'getStepByName'
        ]);
    const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(FinanzielleSituationRS.name,
        ['saveFinanzielleSituationStart', 'getFinanzielleSituationTyp']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,['go']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['clearError']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FinanzielleSituationAppenzellViewComponent],
            providers: [
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepManagerSpy},
                {provide: FinanzielleSituationRS, useValue: finanzielleSituationRSSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: Transition, useValue: transitionSpy},
                NgForm
            ],
            imports: [
                SharedModule
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));
        transitionSpy.params.and.returnValue({});
        berechnungsManagerSpy.finanzielleSituationResultate = new TSFinanzielleSituationResultateDTO();
        wizardStepManagerSpy.getStepByName.and.returnValue(new TSWizardStep());
        fixture = TestBed.createComponent(FinanzielleSituationAppenzellViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA = new TSFamiliensituation();
        return gesuch;
    }
});
