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

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {isStatusVerfuegenVerfuegt, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSBetreuung from '../../../models/TSBetreuung';
import TSKindContainer from '../../../models/TSKindContainer';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class BetreuungListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./betreuungListView.html');
    public controller = BetreuungListViewController;
    public controllerAs = 'vm';
}

/**
 * View fuer die Liste der Betreeungen der eingegebenen Kinder
 */
export class BetreuungListViewController extends AbstractGesuchViewController<any> implements IDVFocusableController {

    public static $inject: string[] = ['$state', 'GesuchModelManager', '$translate', 'DvDialog', 'EbeguUtil', 'BerechnungsManager',
        'ErrorService', 'WizardStepManager', 'AuthServiceRS', '$scope', '$log', '$timeout'];

    public TSRoleUtil = TSRoleUtil;

    public constructor(private readonly $state: StateService, gesuchModelManager: GesuchModelManager,
                       private readonly $translate: ITranslateService,
                       private readonly DvDialog: DvDialog, private readonly ebeguUtil: EbeguUtil, berechnungsManager: BerechnungsManager,
                       private readonly errorService: ErrorService, wizardStepManager: WizardStepManager,
                       private readonly authServiceRS: AuthServiceRS, $scope: IScope, private readonly $log: ILogService, $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);

    }

    public editBetreuung(kind: TSKindContainer, betreuung: any): void {
        if (kind && betreuung) {
            betreuung.isSelected = false; // damit die row in der Tabelle nicht mehr als "selected" markiert ist
            this.openBetreuungView(betreuung.betreuungNummer, kind.kindNummer);
        }
    }

    public isNotAllowedToRemove(betreuung: TSBetreuung): boolean {
        if (betreuung.betreuungsstatus === TSBetreuungsstatus.ABGEWIESEN && this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getAdministratorOrAmtRole())) {
            return false;
        } else {
            return this.isKorrekturModusJugendamt();
        }
    }

    public getKinderWithBetreuungList(): Array<TSKindContainer> {
        return this.gesuchModelManager.getKinderWithBetreuungList();
    }

    public hasBetreuungInStatusWarten(): boolean {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().hasBetreuungInStatusWarten();
        }
        return false;
    }

    public createBetreuung(kind: TSKindContainer): void {
        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kind.kindNummer);
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            this.resetActiveInstitutionenList();
            this.openBetreuungView(undefined, kind.kindNummer);
        } else {
            this.$log.error('kind nicht gefunden ', kind);
        }
    }

    private resetActiveInstitutionenList() {
        // Beim Navigieren auf die BetreuungView muss eventuell die Liste der Institutionen neu geladen werden.
        // Diese wird im GMM gecached und enthält eventuell nicht die neuesten Daten, insbesondere beim Hinzufügen von Betreuungen.
        if (this.authServiceRS.isRole(TSRole.GESUCHSTELLER)) {
            this.gesuchModelManager.resetActiveInstitutionenList();
        }
    }

    public createAnmeldungFerieninsel(kind: TSKindContainer): void {
        this.createAnmeldungSchulamt(TSBetreuungsangebotTyp.FERIENINSEL, kind);
    }

    public createAnmeldungTagesschule(kind: TSKindContainer): void {
        this.createAnmeldungSchulamt(TSBetreuungsangebotTyp.TAGESSCHULE, kind);
    }

    private createAnmeldungSchulamt(betreuungstyp: TSBetreuungsangebotTyp, kind: TSKindContainer): void {
        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kind.kindNummer);
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            this.resetActiveInstitutionenList();
            this.openAnmeldungView(kind.kindNummer, betreuungstyp);
        } else {
            this.$log.error('kind nicht gefunden ', kind);
        }
    }

    public removeBetreuung(kind: TSKindContainer, betreuung: TSBetreuung, index: any): void {
        this.gesuchModelManager.findKind(kind);     // kind index setzen
        const remTitleText: any = this.$translate.instant('BETREUUNG_LOESCHEN', {
            kindname: this.gesuchModelManager.getKindToWorkWith().kindJA.getFullName(),
            betreuungsangebottyp: this.ebeguUtil.translateString(TSBetreuungsangebotTyp[betreuung.institutionStammdaten.betreuungsangebotTyp])
        });
        this.DvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: remTitleText,
            deleteText: 'BETREUUNG_LOESCHEN_BESCHREIBUNG',
            parentController: this,
            elementID: 'removeBetreuungButton' + kind.kindNummer + '_' + index
        }).then(() => {   // User confirmed removal
            this.errorService.clearAll();
            const betreuungIndex = this.gesuchModelManager.findBetreuung(betreuung);
            if (betreuungIndex >= 0) {
                this.gesuchModelManager.setBetreuungIndex(betreuungIndex);
                this.gesuchModelManager.removeBetreuung();
            } else {
                this.$log.error('betreuung nicht gefunden ', betreuung);
            }
        });
    }

    private openBetreuungView(betreuungNumber: number, kindNumber: number): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber,
            kindNumber,
            gesuchId: this.getGesuchId()
        });
    }

    private openAnmeldungView(kindNumber: number, betreuungsangebotTyp: TSBetreuungsangebotTyp): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber: undefined,
            kindNumber,
            gesuchId: this.getGesuchId(),
            betreuungsangebotTyp: betreuungsangebotTyp.toString()
        });
    }

    /**
     * Gibt den Betreuungsangebottyp der Institution, die mit der gegebenen Betreuung verknuepft ist zurueck.
     * By default wird ein Leerzeichen zurueckgeliefert.
     * @param betreuung
     * @returns {string}
     */
    public getBetreuungsangebotTyp(betreuung: TSBetreuung): string {
        if (betreuung && betreuung.institutionStammdaten) {
            return TSBetreuungsangebotTyp[betreuung.institutionStammdaten.betreuungsangebotTyp];
        }
        return '';
    }

    public getBetreuungDetails(betreuung: TSBetreuung): string {
        let detail = betreuung.institutionStammdaten.institution.name;
        if (betreuung.isAngebotFerieninsel()) {
            const ferien = this.$translate.instant(betreuung.belegungFerieninsel.ferienname.toLocaleString());
            detail = detail + ' (' + ferien + ')';
        }
        return detail;
    }

    public canRemoveBetreuung(betreuung: TSBetreuung): boolean {
        return !this.isGesuchReadonly() && !betreuung.vorgaengerId && !betreuung.isSchulamtangebotAusgeloest();
    }

    private showMitteilung(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
    }

    private gotoMitteilung(betreuung: TSBetreuung) {
        this.$state.go('gesuch.mitteilung', {
            dossierId: this.gesuchModelManager.getDossier().id,
            gesuchId: this.gesuchModelManager.getGesuch().id,
            betreuungId: betreuung.id,
            mitteilungId: undefined
        });
    }

    public setFocusBack(elementID: string): void {
        angular.element('#' + elementID).first().focus();
    }

    public showButtonAnmeldungSchulamt(): boolean {
        // Anmeldung Schulamt: Solange das Gesuch noch "normal" editiert werden kann, soll der Weg ueber "Betreuung hinzufuegen" verwendet werden
        // Nachdem readonly: nur fuer Jugendamt, Schulamt und Gesuchsteller verfuegbar sein. Nur fuer GP.hasTagesschulenAnmeldung().
        const isStatus = isStatusVerfuegenVerfuegt(this.gesuchModelManager.getGesuch().status)
            || this.gesuchModelManager.isGesuchReadonlyForRole()
            || this.gesuchModelManager.isKorrekturModusJugendamt()
            || this.gesuchModelManager.getGesuch().gesperrtWegenBeschwerde;
        const isRole = this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtSchulamtGesuchstellerRoles());
        const isGesuchsperiode = this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung();
        const istNotStatusFreigabequittung = this.gesuchModelManager.getGesuch().status !== TSAntragStatus.FREIGABEQUITTUNG;
        const isSchulamtAngeboteEnabled = EbeguUtil.isTagesschulangebotEnabled();
        return isSchulamtAngeboteEnabled && isStatus && isRole && isGesuchsperiode && istNotStatusFreigabequittung && this.gesuchModelManager.isNeuestesGesuch();
    }

    /**
     * Betreuungen und auch anmeldungen duerfen in Status FREIGABEQUITTUNG nicht hinzugefuegt werden
     */
    public isBetreuungenHinzufuegenDisabled(): boolean {
        return this.gesuchModelManager.getGesuch().gesuchsperiode.hasTagesschulenAnmeldung() &&
            this.gesuchModelManager.getGesuch().status === TSAntragStatus.FREIGABEQUITTUNG;
    }

    public hasOnlyFerieninsel() {
        const gesuch = this.gesuchModelManager.getGesuch();
        return !!gesuch && gesuch.areThereOnlyFerieninsel();
    }
}
