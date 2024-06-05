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

import {TSSozialdienstFallStatus} from '../../models/enums/TSSozialdienstFallStatus';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSSozialdienstFall} from '../../models/sozialdienst/TSSozialdienstFall';
import {TSFall} from '../../models/TSFall';
import {GesuchModelManager} from '../service/gesuchModelManager';
import {WizardStepManager} from '../service/wizardStepManager';

import {FreigabeService} from './freigabe.service';
import SpyObj = jasmine.SpyObj;

describe('FreigabeService', () => {
    let testee: FreigabeService;
    let wizardStepManager: SpyObj<WizardStepManager>;
    let gesuchModelManager: SpyObj<GesuchModelManager>;

    beforeEach(() => {
        wizardStepManager = jasmine.createSpyObj<WizardStepManager>(WizardStepManager.name,
            ['areAllStepsOK', 'hasStepGivenStatus', 'isStepStatusOk']);
        gesuchModelManager = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
            ['getGesuch', 'isGesuchStatus', 'isGesuchReadonly', 'getFall']);

        testee = new FreigabeService(gesuchModelManager, wizardStepManager);
    });

    describe('canBeFreigegeben', () => {
        it('should return false when not all steps are true', () => {
            wizardStepManager.areAllStepsOK.and.returnValue(false);
            wizardStepManager.isStepStatusOk.and.returnValue(true);
            expect(testee.canBeFreigegeben()).toBe(false);
        });
        it('should return false when all steps are true but not all Betreuungen are accepted', () => {
            wizardStepManager.areAllStepsOK.and.returnValue(true);
            wizardStepManager.isStepStatusOk.and.returnValue(false);

            expect(testee.canBeFreigegeben()).toBe(false);
            expect(wizardStepManager.isStepStatusOk)
                .toHaveBeenCalledWith(TSWizardStepName.BETREUUNG);
        });
        it('should return false when all steps are true and all Betreuungen are accepted and the Gesuch is ReadOnly',
            () => {
                wizardStepManager.areAllStepsOK.and.returnValue(true);
                wizardStepManager.isStepStatusOk.and.returnValue(true);
                gesuchModelManager.isGesuchReadonly.and.returnValue(true);
                expect(testee.canBeFreigegeben()).toBe(false);
            });
        it('should return true when all steps are true and all Betreuungen are accepted and the Gesuch is not ReadOnly',
            () => {
                wizardStepManager.areAllStepsOK.and.returnValue(true);
                wizardStepManager.isStepStatusOk.and.returnValue(true);
                gesuchModelManager.isGesuchReadonly.and.returnValue(false);
                gesuchModelManager.isGesuchStatus.and.returnValue(true);
                gesuchModelManager.getFall.and.returnValue(new TSFall());
                expect(testee.canBeFreigegeben()).toBe(true);
            });

        it('should return false if the Fall is a Sozialdienstfall',
            () => {
                wizardStepManager.areAllStepsOK.and.returnValue(true);
                wizardStepManager.isStepStatusOk.and.returnValue(true);
                gesuchModelManager.isGesuchReadonly.and.returnValue(false);
                gesuchModelManager.isGesuchStatus.and.returnValue(true);
                const tsFall = new TSFall();
                tsFall.sozialdienstFall = new TSSozialdienstFall();
                gesuchModelManager.getFall.and.returnValue(tsFall);
                expect(testee.canBeFreigegeben()).toBe(false);
            })     ;

        it('should return true if the Fall is an active Sozialdienstfall',
            () => {
                wizardStepManager.areAllStepsOK.and.returnValue(true);
                wizardStepManager.isStepStatusOk.and.returnValue(true);
                gesuchModelManager.isGesuchReadonly.and.returnValue(false);
                gesuchModelManager.isGesuchStatus.and.returnValue(true);
                const tsFall = new TSFall();
                tsFall.sozialdienstFall = new TSSozialdienstFall();
                tsFall.sozialdienstFall.status = TSSozialdienstFallStatus.AKTIV;
                gesuchModelManager.getFall.and.returnValue(tsFall);
                expect(testee.canBeFreigegeben()).toBe(true);
            });
    });
});
