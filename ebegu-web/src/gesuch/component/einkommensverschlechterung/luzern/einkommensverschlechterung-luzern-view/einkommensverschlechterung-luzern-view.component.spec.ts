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
import {StateService, Transition} from '@uirouter/angular';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationTyp} from '../../../../../models/enums/TSFinanzielleSituationTyp';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchsteller} from '../../../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';

import {EinkommensverschlechterungLuzernViewComponent} from './einkommensverschlechterung-luzern-view.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name,
    [
        'areThereOnlyFerieninsel',
        'getBasisjahr',
        'getBasisjahrPlus',
        'getGesuch',
        'isGesuchsteller2Required',
        'isGesuchReadonly',
        'getGesuchsperiode',
        'getGemeinde',
        'setGesuchstellerNumber',
        'setBasisJahrPlusNumber',
        'getBasisjahrToWorkWith',
        'getStammdatenToWorkWith',
        'getBasisJahrPlusNumber'
    ]);
const wizardStepMangerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name, [
        'getCurrentStep', 'setCurrentStep', 'isNextStepBesucht', 'isNextStepEnabled',
        'getCurrentStepName', 'updateCurrentWizardStepStatusSafe'
    ]);
const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(FinanzielleSituationRS.name,
    ['saveFinanzielleSituationStart', 'getFinanzielleSituationTyp']);
const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(BerechnungsManager.name,
    ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']);
berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));
const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['clearError']);

describe('EinkommensverschlechterungLuzernViewComponent', () => {
    let component: EinkommensverschlechterungLuzernViewComponent;
    let fixture: ComponentFixture<EinkommensverschlechterungLuzernViewComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [EinkommensverschlechterungLuzernViewComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepMangerSpy},
                {provide: FinanzielleSituationRS, useValue: finanzielleSituationRSSpy},
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy}
            ],
            imports: [
                SharedModule
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        transitionSpy.params.and.returnValue({gesuchstellerNumber: 1, basisjahrPlus: 1});
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
        gesuchModelManagerSpy.getStammdatenToWorkWith.and.returnValue(new TSGesuchstellerContainer());
        fixture = TestBed.createComponent(EinkommensverschlechterungLuzernViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.finSitTyp = TSFinanzielleSituationTyp.LUZERN;
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller1.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller1.finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA = new TSFamiliensituation();
        return gesuch;
    }
});
