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

import {TranslateService} from '@ngx-translate/core';
import {IComponentOptions, IPromise} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {isAtLeastFreigegeben, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSSozialdienstFallStatus} from '../../../models/enums/TSSozialdienstFallStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {FreigabeDialogController} from '../../dialog/FreigabeDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

const dialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class FreigabeViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./freigabeView.html');
    public controller = FreigabeViewController;
    public controllerAs = 'vm';
}

export class FreigabeViewController extends AbstractGesuchViewController<any> {

    public static $inject = [
        'GesuchModelManager',
        'BerechnungsManager',
        'WizardStepManager',
        'DvDialog',
        'DownloadRS',
        '$scope',
        'ApplicationPropertyRS',
        'AuthServiceRS',
        '$timeout',
        '$translate',
    ];

    public isFreigebenClicked: boolean = false;
    public showGesuchFreigebenSimulationButton: boolean = false;
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        private readonly downloadRS: DownloadRS,
        $scope: IScope,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly authServiceRS: AuthServiceRS,
        $timeout: ITimeoutService,
        private readonly $translate: TranslateService,
    ) {

        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.FREIGABE, $timeout);
        this.initViewModel();
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FREIGABE,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.initDevModeParameter();
    }

    public gesuchEinreichen(): IPromise<void> {
        this.isFreigebenClicked = true;
        if (this.isGesuchValid()) {
            this.form.$setPristine();
            return this.dvDialog.showDialog(dialogTemplate, FreigabeDialogController, {
                parentController: this,
            });
        }
        return undefined;
    }

    public confirmationCallback(): void {
        if (this.gesuchModelManager.isGesuch()) {
            this.openFreigabequittungPDF(true);
        } else {
            this.gesuchFreigeben(); // wenn keine freigabequittung noetig direkt freigeben
        }
    }

    public gesuchFreigeben(): void {
        const gesuchID = this.gesuchModelManager.getGesuch().id;
        this.gesuchModelManager.antragFreigeben(gesuchID, null, null);
    }

    public freigabeZurueckziehen(): void {
        const gesuchID = this.gesuchModelManager.getGesuch().id;
        this.gesuchModelManager.antragZurueckziehen(gesuchID);
    }

    private initDevModeParameter(): void {
        this.applicationPropertyRS.isDevMode().then((response: boolean) => {
            // Simulation nur fuer SuperAdmin freischalten
            const isSuperadmin = this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
            // Die Simulation ist nur im Dev-Mode moeglich und nur, wenn das Gesuch im Status FREIGABEQUITTUNG ist
            this.showGesuchFreigebenSimulationButton =
                (response && this.isGesuchInStatus(TSAntragStatus.FREIGABEQUITTUNG) && isSuperadmin);
        });
    }

    public isGesuchFreigegeben(): boolean {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().status) {
            return isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status)
                || (this.gesuchModelManager.getGesuch().status === TSAntragStatus.FREIGABEQUITTUNG);
        }
        return false;
    }

    public isFreigabequittungAusstehend(): boolean {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().status) {
            return this.gesuchModelManager.getGesuch().status === TSAntragStatus.FREIGABEQUITTUNG;
        }
        return false;
    }

    public openFreigabequittungPDF(forceCreation: boolean): IPromise<void> {
        const win = this.downloadRS.prepareDownloadWindow();
        const gesuchId = this.gesuchModelManager.getGesuch().id;
        return this.downloadRS.getFreigabequittungAccessTokenGeneratedDokument(gesuchId, forceCreation)
            .then((downloadFile: TSDownloadFile) => {
                // wir laden das Gesuch neu, da die Erstellung des Dokumentes auch Aenderungen im Gesuch verursacht
                this.gesuchModelManager.openGesuch(gesuchId)
                    .then(() => {
                        this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
                    })
                    .catch(ex => EbeguUtil.handleDownloadError(win, ex));
            });
    }

    public isThereAnySchulamtAngebot(): boolean {
        return this.gesuchModelManager.isThereAnySchulamtAngebot();
    }

    public getFreigabeDatum(): string {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().freigabeDatum) {
            return DateUtil.momentToLocalDateFormat(this.gesuchModelManager.getGesuch().freigabeDatum, 'DD.MM.YYYY');
        }
        return '';
    }

    public hasBerechenbareBetreuungen(): boolean {
        const gesuch = this.gesuchModelManager.getGesuch();
        return (gesuch && gesuch.hasBerechenbareBetreuungen());
    }

    public getTextForFreigebenNotAllowed(): string {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch && gesuch.gesperrtWegenBeschwerde) {
            return 'FREIGABEQUITTUNG_NOT_ALLOWED_BESCHWERDE_TEXT';
        }
        if (this.gesuchModelManager.isGesuchsperiodeReadonly()) {
            return 'FREIGABEQUITTUNG_NOT_ALLOWED_GESUCHSPERIODE_TEXT';
        }
        if (gesuch && gesuch.hasProvisorischeBetreuungen()) {
            return 'FREIGABEQUITTUNG_NOT_ALLOWED_PROVISORISCHE_BETREUUNG_TEXT';
        }

        return 'FREIGABEQUITTUNG_NOT_ALLOWED_TEXT';
    }

    /**
     * Die Methodes wizardStepManager.areAllStepsOK() erlaubt dass die Betreuungen in Status PLATZBESTAETIGUNG sind
     * aber in diesem Fall duerfen diese nur OK sein, deswegen die Frage extra. Ausserdem darf es nur freigegebn werden
     * wenn es nicht in ReadOnly modus ist
     */
    public canBeFreigegeben(): boolean {
        return this.wizardStepManager.areAllStepsOK(this.gesuchModelManager.getGesuch()) &&
            this.wizardStepManager.isStepStatusOk(TSWizardStepName.BETREUUNG)
            && !this.isGesuchReadonly()
            && (this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_GS)
                || this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST))
            && (!this.gesuchModelManager.getFall().isSozialdienstFall()
                || (this.gesuchModelManager.getFall().isSozialdienstFall()
                    && this.gesuchModelManager.getFall().sozialdienstFall.status === TSSozialdienstFallStatus.AKTIV));
    }

    public isNotFreigegeben(): boolean {
        return this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_GS)
            || this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
    }

    public isThereAnyAbgewieseneBetreuung(): boolean {
        return this.gesuchModelManager.isThereAnyAbgewieseneBetreuung();
    }

    /**
     * Wir koennen auf jeden Fall sicher sein, dass alle Erstgesuche eine Freigabequittung haben.
     * Ausserdem nur die Mutationen bei denen alle JA-Angebote neu sind, werden eine Freigabequittung haben
     */
    public isThereFreigabequittung(): boolean {
        return this.gesuchModelManager.isGesuch();
    }

    public $postLink(): void {
        // tslint:disable-next-line:no-magic-numbers
        this.doPostLinkActions(500);
    }

    public getButtonLabel(): string {
        if (this.isMutation()) {
            return this.$translate.instant('MUTATION_FREIGEBEN');
        }
        return this.$translate.instant('ANTRAG_EINREICHEN');
    }
}
