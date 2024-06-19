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
import {StateService} from '@uirouter/angular';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../../../app/core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationTyp} from '../../../../../models/enums/TSFinanzielleSituationTyp';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGemeinde} from '../../../../../models/TSGemeinde';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchsteller} from '../../../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {TSPublicAppConfig} from '../../../../../models/TSPublicAppConfig';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';

import {AngabenGesuchsteller2Component} from './angaben-gesuchsteller2.component';

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
        'setGesuchstellerNumber'
    ]
);
gesuchModelManagerSpy.getGemeinde.and.returnValue(new TSGemeinde());
const wizardStepMangerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name,
    [
        'getCurrentStep',
        'setCurrentStep',
        'isNextStepBesucht',
        'isNextStepEnabled',
        'getCurrentStepName'
    ]
);
const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(
    FinanzielleSituationRS.name,
    ['saveFinanzielleSituationStart', 'getFinanzielleSituationTyp']
);
const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, [
    'go'
]);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, [
    'clearError'
]);
const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, [
    'isOneOfRoles'
]);
const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(
    ApplicationPropertyRS.name,
    ['getPublicPropertiesCached']
);
applicationPropertyRSSpy.getPublicPropertiesCached.and.returnValue(
    Promise.resolve(new TSPublicAppConfig())
);

const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(
    BerechnungsManager.name,
    ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']
);
berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(
    Promise.resolve(new TSFinanzielleSituationResultateDTO())
);

describe('AngabenGesuchsteller2Component', () => {
    let component: AngabenGesuchsteller2Component;
    let fixture: ComponentFixture<AngabenGesuchsteller2Component>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AngabenGesuchsteller2Component],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepMangerSpy},
                {
                    provide: FinanzielleSituationRS,
                    useValue: finanzielleSituationRSSpy
                },
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy
                }
            ],
            imports: [FormsModule, ReactiveFormsModule, SharedModule]
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
        gesuch.finSitTyp = TSFinanzielleSituationTyp.LUZERN;
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller1.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller2.gesuchstellerJA = new TSGesuchsteller();
        gesuch.gesuchsteller1.finanzielleSituationContainer =
            new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA =
            new TSFinanzielleSituation();
        gesuch.gesuchsteller2.finanzielleSituationContainer =
            new TSFinanzielleSituationContainer();
        gesuch.gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA =
            new TSFinanzielleSituation();
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA =
            new TSFamiliensituation();
        return gesuch;
    }
});
