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

import {IScope, ITimeoutService} from 'angular';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../abstractGesuchView';

export abstract class AbstractFinSitBernView extends AbstractGesuchViewController<TSFinanzModel> {

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout);
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public showZugriffErfolgreich(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus) &&
            this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus !== 'FAILED'
            && this.getModel().finanzielleSituationJA.steuerdatenZugriff;
    }

    public showZugriffFailed(): boolean {
        return this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus === 'FAILED'
            && this.getModel().finanzielleSituationJA.steuerdatenZugriff;
    }

    // hier neu init
    public steuerveranlagungClicked(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        // Wenn zusätzlich noch GemeinsameStek -> Dasselbe auch für GS2
        // Wenn Steuerveranlagung erhalten, muss auch STEK ausgefüllt worden sein
        if (this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerveranlagungErhalten) {
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt = true;
            if (this.model.gemeinsameSteuererklaerung) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten = true;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt = true;
            }
        } else if (!this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerveranlagungErhalten) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt = undefined;
            if (this.model.gemeinsameSteuererklaerung) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten = false;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt =
                    undefined;
            }
        }
    }
}
