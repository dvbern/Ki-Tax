/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import * as angular from 'angular';
import {CORE_JS_MODULE} from '../../app/core/core.angularjs.module';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSAdressetyp} from '../../models/enums/TSAdressetyp';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSRole} from '../../models/enums/TSRole';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import {TSAdresse} from '../../models/TSAdresse';
import {TSAdresseContainer} from '../../models/TSAdresseContainer';
import {TSDossier} from '../../models/TSDossier';
import {TSFall} from '../../models/TSFall';
import {TSGesuch} from '../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../models/TSGesuchstellerContainer';
import {TSWizardStep} from '../../models/TSWizardStep';
import {TSDateRange} from '../../models/types/TSDateRange';
import {DateUtil} from '../../utils/DateUtil';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {WizardStepManager} from './wizardStepManager';
import {WizardStepRS} from './WizardStepRS.rest';

// tslint:disable:no-big-function
describe('wizardStepManager', () => {

    let authServiceRS: AuthServiceRS;
    let wizardStepManager: WizardStepManager;
    let wizardStepRS: WizardStepRS;
    let scope: angular.IScope;
    let $q: angular.IQService;

    const gesuchAntrag = new TSGesuch();

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        authServiceRS = $injector.get('AuthServiceRS');
        wizardStepRS = $injector.get('WizardStepRS');
        scope = $injector.get('$rootScope').$new();
        $q = $injector.get('$q');
        wizardStepManager = $injector.get('WizardStepManager');

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($injector.get('$httpBackend'));
    }));

    beforeEach(() => {
        gesuchAntrag.typ = TSAntragTyp.ERSTGESUCH;
    });

    describe('construct the object', () => {
        it('constructs the steps for Institution', () => {
            spyOn(authServiceRS, 'getPrincipalRole').and.returnValue(TSRole.SACHBEARBEITER_INSTITUTION);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_INSTITUTION);
            expect(wizardStepManager.getAllowedSteps()).toBeDefined();
            expect(wizardStepManager.getAllowedSteps().length).toBe(6);
            expect(wizardStepManager.getAllowedSteps()[0]).toBe(TSWizardStepName.FAMILIENSITUATION);
            expect(wizardStepManager.getAllowedSteps()[1]).toBe(TSWizardStepName.GESUCHSTELLER);
            expect(wizardStepManager.getAllowedSteps()[2]).toBe(TSWizardStepName.UMZUG);
            expect(wizardStepManager.getAllowedSteps()[3]).toBe(TSWizardStepName.BETREUUNG);
            expect(wizardStepManager.getAllowedSteps()[4]).toBe(TSWizardStepName.ABWESENHEIT);
            expect(wizardStepManager.getAllowedSteps()[5]).toBe(TSWizardStepName.VERFUEGEN);
        });
        it('constructs the steps for JA', () => {
            spyOn(authServiceRS, 'getPrincipalRole').and.returnValue(TSRole.SACHBEARBEITER_BG);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_BG);
            const expectedSteps = 15;
            expect(wizardStepManager.getAllowedSteps().length).toBe(expectedSteps);
            expect(wizardStepManager.getAllowedSteps()[0]).toBe(TSWizardStepName.GESUCH_ERSTELLEN);
            expect(wizardStepManager.getAllowedSteps()[1]).toBe(TSWizardStepName.FAMILIENSITUATION);
            expect(wizardStepManager.getAllowedSteps()[2]).toBe(TSWizardStepName.GESUCHSTELLER);
            expect(wizardStepManager.getAllowedSteps()[3]).toBe(TSWizardStepName.UMZUG);
            expect(wizardStepManager.getAllowedSteps()[4]).toBe(TSWizardStepName.KINDER);
            expect(wizardStepManager.getAllowedSteps()[5]).toBe(TSWizardStepName.BETREUUNG);
            expect(wizardStepManager.getAllowedSteps()[6]).toBe(TSWizardStepName.ABWESENHEIT);
            expect(wizardStepManager.getAllowedSteps()[7]).toBe(TSWizardStepName.ERWERBSPENSUM);
            expect(wizardStepManager.getAllowedSteps()[8]).toBe(TSWizardStepName.FINANZIELLE_SITUATION);
            expect(wizardStepManager.getAllowedSteps()[9]).toBe(TSWizardStepName.FINANZIELLE_SITUATION_LUZERN);
            expect(wizardStepManager.getAllowedSteps()[10]).toBe(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
            expect(wizardStepManager.getAllowedSteps()[11]).toBe(TSWizardStepName.DOKUMENTE);
            expect(wizardStepManager.getAllowedSteps()[12]).toBe(TSWizardStepName.FREIGABE);
            // tslint:disable-next-line:no-magic-numbers
            expect(wizardStepManager.getAllowedSteps()[13]).toBe(TSWizardStepName.VERFUEGEN);
        });
    });
    describe('findStepsFromGesuch', () => {
        it('retrieves the steps from server', () => {
            const step = new TSWizardStep();
            step.bemerkungen = 'step1';
            const steps = [step];
            spyOn(wizardStepRS, 'findWizardStepsFromGesuch').and.returnValue($q.when(steps));

            wizardStepManager.findStepsFromGesuch('123');
            scope.$apply();

            // tslint:disable-next-line:no-unbound-method
            expect(wizardStepRS.findWizardStepsFromGesuch).toHaveBeenCalledWith('123');
            expect(wizardStepManager.getWizardSteps()).toBeDefined();
            expect(wizardStepManager.getWizardSteps().length).toBe(1);
            expect(wizardStepManager.getWizardSteps()[0]).toBe(step);
        });
        it('does not find any steps im Server -> minimale steps must be set', () => {
            const steps: TSWizardStep[] = [];
            spyOn(wizardStepRS, 'findWizardStepsFromGesuch').and.returnValue($q.when(steps));

            wizardStepManager.findStepsFromGesuch('123');
            scope.$apply();

            // tslint:disable-next-line:no-unbound-method
            expect(wizardStepRS.findWizardStepsFromGesuch).toHaveBeenCalledWith('123');
            expect(wizardStepManager.getWizardSteps()).toBeDefined();
            expect(wizardStepManager.getWizardSteps().length).toBe(2); // erste 2 states sind definiert
            expect(wizardStepManager.getWizardSteps()[0].wizardStepName)
                .toBe(TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN);
            expect(wizardStepManager.getWizardSteps()[0].wizardStepStatus).toBe(TSWizardStepStatus.IN_BEARBEITUNG);
            expect(wizardStepManager.getWizardSteps()[0].verfuegbar).toBe(true);
            expect(wizardStepManager.getCurrentStep()).toBe(wizardStepManager.getWizardSteps()[0]);
        });
    });
    describe('isNextStepBesucht', () => {
        it('next step is available because status != UNBESUCHT', () => {
            createAllSteps(TSWizardStepStatus.OK);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_BG);

            wizardStepManager.setCurrentStep(TSWizardStepName.GESUCH_ERSTELLEN);
            expect(wizardStepManager.isNextStepBesucht(gesuchAntrag)).toBe(true);
        });
    });
    describe('areAllStepsOK', () => {
        it('returns true when all steps are OK', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(true);
            expect(wizardStepManager.areAllStepsOK(gesuch)).toBe(true);
        });
        it('returns true when all steps are OK although Betreuung is still PLATZBESTAETIGUNG', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(true);
            wizardStepManager.setCurrentStep(TSWizardStepName.BETREUUNG);
            wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.PLATZBESTAETIGUNG);
            expect(wizardStepManager.areAllStepsOK(gesuch)).toBe(true);
        });
        it('returns true when step Betreuung is not OK but there is not any Betreuung', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(false);
            wizardStepManager.setCurrentStep(TSWizardStepName.BETREUUNG);
            wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.NOK);
            expect(wizardStepManager.areAllStepsOK(gesuch)).toBe(true);
        });
        it('returns false when not all steps are OK', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(true);
            wizardStepManager.setCurrentStep(TSWizardStepName.FINANZIELLE_SITUATION);
            wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.NOK);
            expect(wizardStepManager.areAllStepsOK(gesuch)).toBe(false);
        });
    });
    describe('hasStepGivenStatus', () => {
        it('returns true if the Step has the given status', () => {
            createAllSteps(TSWizardStepStatus.OK);
            expect(wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.OK)).toBe(true);
        });
        it('returns false if the Step does not have the given status', () => {
            createAllSteps(TSWizardStepStatus.OK);
            expect(wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.NOK))
                .toBe(false);
        });
        it('returns false if the Step does not exist', () => {
            wizardStepManager.getWizardSteps().splice(0, wizardStepManager.getWizardSteps().length);
            const step = new TSWizardStep();
            step.gesuchId = '';
            step.wizardStepName = TSWizardStepName.GESUCH_ERSTELLEN;
            step.wizardStepStatus = TSWizardStepStatus.OK;
            step.bemerkungen = '';
            step.verfuegbar = true;
            wizardStepManager.getWizardSteps().push(step);
            expect(wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.NOK))
                .toBe(false);
        });
    });
    describe('getNextStep', () => {
        it('returns ERWERBSPENSUM coming from BETREUUNG for SACHBEARBEITER_BG', () => {
            createAllSteps(TSWizardStepStatus.OK);
            wizardStepManager.setCurrentStep(TSWizardStepName.BETREUUNG);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_BG);
            expect(wizardStepManager.getNextStep(gesuchAntrag)).toBe(TSWizardStepName.ERWERBSPENSUM);
        });
        it('returns VERFUEGEN coming from BETREUUNG for SACHBEARBEITER_INSTITUTION IF VERFUEGT', () => {
            createAllSteps(TSWizardStepStatus.OK);
            gesuchAntrag.status = TSAntragStatus.VERFUEGT;
            wizardStepManager.setCurrentStep(TSWizardStepName.BETREUUNG);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_INSTITUTION);
            expect(wizardStepManager.getNextStep(gesuchAntrag)).toBe(TSWizardStepName.VERFUEGEN);
        });
        it('returns undefined coming from BETREUUNG for SACHBEARBEITER_INSTITUTION IF Not VERFUEGT', () => {
            createAllSteps(TSWizardStepStatus.OK);
            gesuchAntrag.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            wizardStepManager.setCurrentStep(TSWizardStepName.BETREUUNG);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_INSTITUTION);
            expect(wizardStepManager.getNextStep(gesuchAntrag)).toBe(undefined);
        });
    });
    describe('getPreviousStep', () => {
        it('returns BETREUUNG coming from ERWERBSPENSUM for SACHBEARBEITER_BG', () => {
            createAllSteps(TSWizardStepStatus.OK);
            wizardStepManager.setCurrentStep(TSWizardStepName.ERWERBSPENSUM);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_BG);
            expect(wizardStepManager.getPreviousStep(gesuchAntrag)).toBe(TSWizardStepName.BETREUUNG);
        });
        it('returns BETREUUNG coming from VERFUEGEN for SACHBEARBEITER_INSTITUTION', () => {
            createAllSteps(TSWizardStepStatus.OK);
            wizardStepManager.setCurrentStep(TSWizardStepName.VERFUEGEN);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_INSTITUTION);
            expect(wizardStepManager.getPreviousStep(gesuchAntrag)).toBe(TSWizardStepName.BETREUUNG);
        });
    });
    describe('isStepVisible', () => {
        it('returns true for all steps allowed for role and false if the step is hidden', () => {
            createAllSteps(TSWizardStepStatus.OK);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_BG);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.GESUCH_ERSTELLEN)).toBe(true);

            wizardStepManager.hideStep(TSWizardStepName.GESUCH_ERSTELLEN);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.GESUCH_ERSTELLEN)).toBe(false);

            wizardStepManager.unhideStep(TSWizardStepName.GESUCH_ERSTELLEN);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.GESUCH_ERSTELLEN)).toBe(true);
        });
        it('returns false for NOT allowed steps whether they are hidden or not', () => {
            createAllSteps(TSWizardStepStatus.OK);
            wizardStepManager.getAllowedSteps().splice(0);
            wizardStepManager.setAllowedStepsForRole(TSRole.SACHBEARBEITER_INSTITUTION);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.FINANZIELLE_SITUATION)).toBe(false);

            wizardStepManager.hideStep(TSWizardStepName.GESUCH_ERSTELLEN);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.FINANZIELLE_SITUATION)).toBe(false);

            wizardStepManager.unhideStep(TSWizardStepName.GESUCH_ERSTELLEN);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.FINANZIELLE_SITUATION)).toBe(false);
        });
    });

    describe('hideSteps', () => {
        it('should hide the steps ABWESENHEIT and UMZUG for ONLINE Erstgesuch without umzug', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            gesuch.eingangsart = TSEingangsart.ONLINE;
            gesuch.typ = TSAntragTyp.ERSTGESUCH;
            gesuch.dossier = new TSDossier();
            gesuch.dossier.fall = new TSFall();
            wizardStepManager.setHiddenSteps(gesuch);

            expect(wizardStepManager.isStepVisible(TSWizardStepName.FREIGABE)).toBe(true);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.ABWESENHEIT)).toBe(false);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.UMZUG)).toBe(false);
        });
        it('should hide the steps ABWESENHEIT and UMZUG and unhide FREIGABE for PAPIER Erstgesuch without umzug',
            () => {
                createAllSteps(TSWizardStepStatus.OK);
                const gesuch = new TSGesuch();
                gesuch.eingangsart = TSEingangsart.PAPIER;
                gesuch.typ = TSAntragTyp.ERSTGESUCH;
                gesuch.dossier = new TSDossier();
                gesuch.dossier.fall = new TSFall();
                wizardStepManager.setHiddenSteps(gesuch);

                expect(wizardStepManager.isStepVisible(TSWizardStepName.FREIGABE)).toBe(false);
                expect(wizardStepManager.isStepVisible(TSWizardStepName.ABWESENHEIT)).toBe(false);
                expect(wizardStepManager.isStepVisible(TSWizardStepName.UMZUG)).toBe(false);
            });
        it('should unhide the steps ABWESENHEIT and UMZUG for Mutation and hide FREIGABE for PAPIER Gesuch', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            gesuch.eingangsart = TSEingangsart.PAPIER;
            gesuch.typ = TSAntragTyp.MUTATION;
            gesuch.dossier = new TSDossier();
            gesuch.dossier.fall = new TSFall();
            wizardStepManager.setHiddenSteps(gesuch);

            expect(wizardStepManager.isStepVisible(TSWizardStepName.FREIGABE)).toBe(false);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.ABWESENHEIT)).toBe(true);
            expect(wizardStepManager.isStepVisible(TSWizardStepName.UMZUG)).toBe(true);
        });
        it('should unhide the step UMZUG for Erstgesuch with umzug and hide ABWESENHEIT', () => {
            createAllSteps(TSWizardStepStatus.OK);
            const gesuch = new TSGesuch();
            gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
            const umzugsAdresse = new TSAdresseContainer();
            umzugsAdresse.adresseJA = new TSAdresse();
            umzugsAdresse.adresseJA.adresseTyp = TSAdressetyp.WOHNADRESSE;
            umzugsAdresse.adresseJA.gueltigkeit =
                new TSDateRange(DateUtil.today().add(1, 'months'), DateUtil.today().add(7, 'months'));
            gesuch.gesuchsteller1.adressen = [umzugsAdresse, umzugsAdresse]; // for an umzugAdresse we just need more
                                                                             // than one Wohnadressen
            gesuch.eingangsart = TSEingangsart.ONLINE;
            gesuch.typ = TSAntragTyp.ERSTGESUCH;
            gesuch.dossier = new TSDossier();
            gesuch.dossier.fall = new TSFall();
            wizardStepManager.setHiddenSteps(gesuch);

            expect(wizardStepManager.isStepVisible(TSWizardStepName.UMZUG)).toBe(true);
        });
    });

    function createAllSteps(status: TSWizardStepStatus): void {
        wizardStepManager.getWizardSteps().splice(0, wizardStepManager.getWizardSteps().length);
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN, status, '', false));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.GESUCH_ERSTELLEN, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.FAMILIENSITUATION, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.GESUCHSTELLER, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.KINDER, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.BETREUUNG, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.ERWERBSPENSUM, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.FINANZIELLE_SITUATION, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.DOKUMENTE, status, '', true));
        wizardStepManager.getWizardSteps().push(
            wizardStepManager.createWizardStep('', TSWizardStepName.VERFUEGEN, status, '', true));
    }
});
