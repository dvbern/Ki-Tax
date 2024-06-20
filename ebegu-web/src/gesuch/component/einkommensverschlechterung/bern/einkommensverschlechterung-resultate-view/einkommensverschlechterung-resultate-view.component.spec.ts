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
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchsteller} from '../../../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';

import {EinkommensverschlechterungResultateViewComponent} from './einkommensverschlechterung-resultate-view.component';

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
        'getStammdatenToWorkWith'
    ]
);
const wizardStepMangerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name,
    [
        'getCurrentStep',
        'setCurrentStep',
        'isNextStepBesucht',
        'isNextStepEnabled',
        'getCurrentStepName',
        'updateCurrentWizardStepStatusSafe'
    ]
);
const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(
    FinanzielleSituationRS.name,
    ['saveFinanzielleSituationStart', 'getFinanzielleSituationTyp']
);
const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, [
    'params'
]);
const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(
    BerechnungsManager.name,
    [
        'calculateFinanzielleSituation',
        'calculateFinanzielleSituationTemp',
        'calculateEinkommensverschlechterungTemp'
    ]
);
berechnungsManagerSpy.calculateEinkommensverschlechterungTemp.and.returnValue(
    Promise.resolve(new TSFinanzielleSituationResultateDTO())
);
berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(
    Promise.resolve(new TSFinanzielleSituationResultateDTO())
);

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, [
    'go'
]);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, [
    'clearError'
]);

describe('EinkommensverschlechterungResultateViewComponent', () => {
    let component: EinkommensverschlechterungResultateViewComponent;
    let fixture: ComponentFixture<EinkommensverschlechterungResultateViewComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [EinkommensverschlechterungResultateViewComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepMangerSpy},
                {
                    provide: FinanzielleSituationRS,
                    useValue: finanzielleSituationRSSpy
                },
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy}
            ],
            imports: [SharedModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        transitionSpy.params.and.returnValue({basisjahrPlus: 1});
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
        fixture = TestBed.createComponent(
            EinkommensverschlechterungResultateViewComponent
        );
        component = fixture.componentInstance;
        component.model = new TSFinanzModel(1, false, 1, 2);
        component.model.einkommensverschlechterungContainerGS1 =
            new TSEinkommensverschlechterungContainer();
        component.model.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus1 =
            new TSEinkommensverschlechterung();
        component.model.einkommensverschlechterungContainerGS1.ekvGSBasisJahrPlus1 =
            new TSEinkommensverschlechterung();
        component.model.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus2 =
            new TSEinkommensverschlechterung();
        component.model.einkommensverschlechterungContainerGS1.ekvGSBasisJahrPlus2 =
            new TSEinkommensverschlechterung();
        component.model.finanzielleSituationContainerGS1 =
            new TSFinanzielleSituationContainer();
        component.model.finanzielleSituationContainerGS1.finanzielleSituationJA =
            new TSFinanzielleSituation();
        console.log(component.getEinkommensverschlechterungGS1_JA());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.finSitTyp = TSFinanzielleSituationTyp.BERN;
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller1.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller1.finanzielleSituationContainer =
            new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA =
            new TSFinanzielleSituation();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA =
            new TSFamiliensituation();
        gesuch.gesuchsteller1.einkommensverschlechterungContainer =
            new TSEinkommensverschlechterungContainer();
        gesuch.gesuchsteller1.einkommensverschlechterungContainer.ekvJABasisJahrPlus1 =
            new TSEinkommensverschlechterung();
        return gesuch;
    }
});
