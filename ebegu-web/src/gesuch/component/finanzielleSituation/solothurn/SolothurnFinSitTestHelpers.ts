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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/angular';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSFinanzielleSituationTyp} from '../../../../models/enums/TSFinanzielleSituationTyp';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituation} from '../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSGesuchsteller} from '../../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../../models/TSGesuchstellerContainer';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {FinanzielleSituationRS} from '../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {FinanzielleSituationSolothurnService} from './finanzielle-situation-solothurn.service';
import SpyObj = jasmine.SpyObj;

export class SolothurnFinSitTestHelpers {

    public static getMockProvidersExceptGesuchModelManager(): { provide: any, useValue: any }[] {
        const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(
            WizardStepManager.name,
            ['getCurrentStep', 'setCurrentStep', 'isNextStepBesucht', 'isNextStepEnabled', 'getCurrentStepName', 'updateCurrentWizardStepStatusSafe']);
        const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(FinanzielleSituationRS.name,
            ['saveFinanzielleSituationStart', 'getFinanzielleSituationTyp']);
        const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
            ['go']);
        const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['clearError']);
        const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);

        return [
            {provide: WizardStepManager, useValue: wizardStepManagerSpy},
            {provide: FinanzielleSituationRS, useValue: finanzielleSituationRSSpy},
            {provide: StateService, useValue: stateServiceSpy},
            {provide: ErrorService, useValue: errorServiceSpy},
            {provide: AuthServiceRS, useValue: authServiceSpy},
        ];
    }

    public static createGesuchModelManagerMock(): SpyObj<GesuchModelManager> {
        return jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
            [
                'areThereOnlyFerieninsel',
                'getBasisjahr',
                'getBasisjahrPlus',
                'getGesuch',
                'isGesuchReadonlyForRole',
                'isGesuchsteller2Required',
                'isGesuchReadonly',
                'getGesuchsperiode',
                'getGemeinde',
                'isKorrekturModusJugendamt',
                'areThereOnlyBgBetreuungen',
                'isKorrekturModusJugendamt',
                'setGesuchstellerNumber',
                'areThereOnlySchulamtAngebote'
            ]);
    }

    public static getMockProvidersExceptFinSitSolothurnServiceMock(): { provide: any, useValue: any }[] {
        const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(
            BerechnungsManager.name,
            ['calculateFinanzielleSituationTemp']);
        return [
            {provide: BerechnungsManager, useValue: berechnungsManagerSpy}
        ];
    }

    public static createFinSitSolothurnServiceMock(): SpyObj<FinanzielleSituationSolothurnService> {
        return jasmine.createSpyObj<FinanzielleSituationSolothurnService>(FinanzielleSituationSolothurnService.name,
            [
                'massgebendesEinkommenStore',
                'calculateMassgebendesEinkommen'
            ]);
    }

    public static createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.finSitTyp = TSFinanzielleSituationTyp.SOLOTHURN;
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

    public static createFinanzModel(): TSFinanzModel {
        // tslint:disable-next-line:no-magic-numbers
        const model = new TSFinanzModel(2019, false, 1);
        model.finanzielleSituationContainerGS1 = new TSFinanzielleSituationContainer();
        model.finanzielleSituationContainerGS1.finanzielleSituationJA = new TSFinanzielleSituation();
        return model;
    }
}
