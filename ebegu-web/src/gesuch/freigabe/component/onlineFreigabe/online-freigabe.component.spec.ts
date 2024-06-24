/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

import {TSAntragStatus} from '../../../../models/enums/TSAntragStatus';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFreigabe} from '../../../../models/TSFreigabe';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSWizardStep} from '../../../../models/TSWizardStep';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {FreigabeService} from '../../freigabe.service';
import {OnlineFreigabeComponent} from './online-freigabe.component';
import SpyObj = jasmine.SpyObj;

describe('OnlineFreigabeComponent', () => {
    let gesuchModelManager: SpyObj<GesuchModelManager>;
    let wizardStepManager: SpyObj<WizardStepManager>;
    let freigabeService: SpyObj<FreigabeService>;
    let testee: OnlineFreigabeComponent;

    beforeEach(() => {
        gesuchModelManager = jasmine.createSpyObj<GesuchModelManager>(
            GesuchModelManager.name,
            ['antragFreigeben', 'getGesuch']
        );
        wizardStepManager = jasmine.createSpyObj<WizardStepManager>(
            WizardStepManager.name,
            [
                'setCurrentStep',
                'updateCurrentWizardStepStatusSafe',
                'getStepByName'
            ]
        );
        freigabeService = jasmine.createSpyObj<FreigabeService>(
            FreigabeService.name,
            ['canBeFreigegeben']
        );
    });

    describe('constructor', () => {
        it('must update step status to IN BEARBEITUNG if it was UNBESUCHT, alreadyFreigeben must be false', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            step.wizardStepStatus = TSWizardStepStatus.UNBESUCHT;
            wizardStepManager.getStepByName.and.returnValue(step);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );

            // then
            expect(testee.alreadyFreigegeben).toBeFalse();
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).toHaveBeenCalledWith(
                TSWizardStepName.FREIGABE,
                TSWizardStepStatus.IN_BEARBEITUNG
            );
        });

        it('must update step status to OK if alreadyFreigeben is true', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );

            // then
            expect(testee.alreadyFreigegeben).toBeTrue();
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).toHaveBeenCalledWith(
                TSWizardStepName.FREIGABE,
                TSWizardStepStatus.OK
            );
        });
    });

    describe('freigebenButtonDisabled', () => {
        it('must return true if FreigabeService says no', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(false);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );
            testee.model.userConfirmedCorrectness = true;

            // then
            expect(testee.freigebenButtonDisabled()).toBeTrue();
            expect(freigabeService.canBeFreigegeben).toHaveBeenCalled();
        });

        it('must return false if true has not confirmed correctness', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(true);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );
            testee.model.userConfirmedCorrectness = false;

            // then
            expect(testee.freigebenButtonDisabled()).toBeTrue();
        });

        it('must return true gesuch is already freigegeben', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(true);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );
            testee.model.userConfirmedCorrectness = true;

            // then
            expect(testee.freigebenButtonDisabled()).toBeTrue();
        });
    });
    describe('checkboxDisabled', () => {
        it('must return true if FreigabeService says no', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(false);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );

            // then
            expect(testee.checkboxDisabled()).toBeTrue();
            expect(freigabeService.canBeFreigegeben).toHaveBeenCalled();
        });

        it('must return true gesuch is already freigegeben', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(true);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );

            // then
            expect(testee.checkboxDisabled()).toBeTrue();
        });
    });

    describe('showReason', () => {
        it('must return true if FreigabeService says no and gesuch is not yet freigegeben', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(false);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );

            // then
            expect(testee.showReason()).toBeTrue();
            expect(freigabeService.canBeFreigegeben).toHaveBeenCalled();
        });

        it('must return false if gesuch is already freigegeben', () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            freigabeService.canBeFreigegeben.and.returnValue(false);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );

            // then
            expect(testee.showReason()).toBeFalse();
            expect(freigabeService.canBeFreigegeben).toHaveBeenCalled();
        });
    });

    describe('freigeben', () => {
        it('must not do anything if user has not confirmed correctness', async () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );
            testee.model.userConfirmedCorrectness = false;
            await testee.freigeben();

            // then
            expect(gesuchModelManager.antragFreigeben).not.toHaveBeenCalled();
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).not.toHaveBeenCalledWith(
                TSWizardStepName.FAMILIENSITUATION,
                TSWizardStepStatus.OK
            );
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).not.toHaveBeenCalledWith(
                TSWizardStepName.FAMILIENSITUATION,
                TSWizardStepStatus.NOK
            );
        });

        it('must call GesuchModelManager if user has confirmed correctness', async () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.id = 'id';
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            gesuchModelManager.antragFreigeben.and.returnValue(
                Promise.resolve(gesuch)
            );

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );
            testee.model.userConfirmedCorrectness = true;
            await testee.freigeben();

            // then
            expect(gesuchModelManager.antragFreigeben).toHaveBeenCalledWith(
                gesuch.id,
                new TSFreigabe(null, null, true)
            );
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).toHaveBeenCalledWith(
                TSWizardStepName.FREIGABE,
                TSWizardStepStatus.OK
            );
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).not.toHaveBeenCalledWith(
                TSWizardStepName.FREIGABE,
                TSWizardStepStatus.NOK
            );
        });

        it('must call mark step NOK if antragFreigeben fails', async () => {
            // given
            const gesuch = new TSGesuch();
            gesuch.id = 'id';
            gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            gesuchModelManager.getGesuch.and.returnValue(gesuch);
            const step = new TSWizardStep();
            wizardStepManager.getStepByName.and.returnValue(step);
            gesuchModelManager.antragFreigeben.and.throwError(
                new Error('freigabe failed')
            );

            // when
            testee = new OnlineFreigabeComponent(
                gesuchModelManager,
                wizardStepManager,
                freigabeService
            );
            testee.model.userConfirmedCorrectness = true;
            await testee.freigeben();

            // then
            expect(gesuchModelManager.antragFreigeben).toHaveBeenCalledWith(
                gesuch.id,
                new TSFreigabe(null, null, true)
            );
            expect(
                wizardStepManager.updateCurrentWizardStepStatusSafe
            ).toHaveBeenCalledWith(
                TSWizardStepName.FREIGABE,
                TSWizardStepStatus.NOK
            );
        });
    });
});
