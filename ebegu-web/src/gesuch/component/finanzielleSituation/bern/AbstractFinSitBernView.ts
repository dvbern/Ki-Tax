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

import {IPromise, IScope, ITimeoutService} from 'angular';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../../app/core/directive/dv-dialog/dv-dialog';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../models/enums/TSEinstellungKey';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../abstractGesuchView';

const removeDialogTemplate = require('../../../dialog/removeDialogTemplate.html');

export abstract class AbstractFinSitBernView extends AbstractGesuchViewController<TSFinanzModel> {

    public steuerSchnittstelleAktiv: boolean;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        $timeout: ITimeoutService,
        protected readonly authServiceRS: AuthServiceRS,
        private readonly einstellungRS: EinstellungRS,
        protected readonly dvDialog: DvDialog
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout);

        this.einstellungRS.findEinstellung(TSEinstellungKey.SCHNITTSTELLE_STEUERN_AKTIV,
            this.gesuchModelManager.getGemeinde()?.id,
            this.gesuchModelManager.getGesuchsperiode()?.id)
            .then(setting => {
                this.steuerSchnittstelleAktiv = (setting.value === 'true');
            });
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

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

    public isGesuchsteller(): boolean {
        return this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
    }

    public showSteuerdatenAbholenButton(): boolean {
        return this.steuerSchnittstelleAktiv
            && this.getModel().finanzielleSituationJA.steuerdatenZugriff
            && EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus);
    }

    protected showResetDialog(): IPromise<void> {
        return this.dvDialog.showRemoveDialog(removeDialogTemplate, null, RemoveDialogController, {
            title: 'WOLLEN_SIE_FORTFAHREN',
            deleteText: 'RESET_KIBON_ABFRAGE_WARNING',
        });
    }

    public resetKiBonAnfrageFinSitIfRequired(): void {
        if (EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus)) {
            this.resetKiBonAnfrageFinSit();
            return;
        }
        this.showResetDialog().then(() => {
            this.resetKiBonAnfrageFinSit();
        }, () => this.getModel().finanzielleSituationJA.steuerdatenZugriff = true);
    }

    protected abstract resetKiBonAnfrageFinSit(): void;

    protected abstract showAutomatischePruefungSteuerdatenFrage(): boolean;
}
