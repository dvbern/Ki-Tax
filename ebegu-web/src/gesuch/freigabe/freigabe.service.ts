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

import {Injectable} from '@angular/core';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSSozialdienstFallStatus} from '../../models/enums/TSSozialdienstFallStatus';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {GesuchModelManager} from '../service/gesuchModelManager';
import {WizardStepManager} from '../service/wizardStepManager';

@Injectable({
    providedIn: 'root'
})
export class FreigabeService {
    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly wizardStepManager: WizardStepManager
    ) {}

    /**
     * Die Methodes wizardStepManager.areAllStepsOK() erlaubt dass die Betreuungen in Status PLATZBESTAETIGUNG sind
     * aber in diesem Fall duerfen diese nur OK sein, deswegen die Frage extra. Ausserdem darf es nur freigegebn werden
     * wenn es nicht in ReadOnly modus ist
     */
    public canBeFreigegeben(): boolean {
        return (
            this.wizardStepManager.areAllStepsOK(
                this.gesuchModelManager.getGesuch()
            ) &&
            this.wizardStepManager.isStepStatusOk(TSWizardStepName.BETREUUNG) &&
            !this.gesuchModelManager.isGesuchReadonly() &&
            (this.gesuchModelManager.isGesuchStatus(
                TSAntragStatus.IN_BEARBEITUNG_GS
            ) ||
                this.gesuchModelManager.isGesuchStatus(
                    TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST
                )) &&
            (!this.gesuchModelManager.getFall().isSozialdienstFall() ||
                (this.gesuchModelManager.getFall().isSozialdienstFall() &&
                    this.gesuchModelManager.getFall().sozialdienstFall
                        .status === TSSozialdienstFallStatus.AKTIV))
        );
    }

    public getTextForFreigebenNotAllowed(): string {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch?.gesperrtWegenBeschwerde) {
            return 'FREIGABEQUITTUNG_NOT_ALLOWED_BESCHWERDE_TEXT';
        }
        if (this.gesuchModelManager.isGesuchsperiodeReadonly()) {
            return 'FREIGABEQUITTUNG_NOT_ALLOWED_GESUCHSPERIODE_TEXT';
        }
        if (gesuch?.hasProvisorischeBetreuungen()) {
            return 'FREIGABEQUITTUNG_NOT_ALLOWED_PROVISORISCHE_BETREUUNG_TEXT';
        }

        return 'FREIGABEQUITTUNG_NOT_ALLOWED_TEXT';
    }
}
