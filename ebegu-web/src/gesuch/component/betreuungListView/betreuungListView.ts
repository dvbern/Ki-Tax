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
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {isStatusVerfuegenVerfuegt, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
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

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        '$translate',
        'DvDialog',
        'EbeguUtil',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        'AuthServiceRS',
        '$scope',
        '$log',
        '$timeout',
    ];

    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService,
        private readonly dvDialog: DvDialog,
        private readonly ebeguUtil: EbeguUtil,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly authServiceRS: AuthServiceRS,
        $scope: IScope,
        private readonly $log: ILogService,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.BETREUUNG,
            TSWizardStepStatus.IN_BEARBEITUNG);

    }

    public editBetreuung(kind: TSKindContainer, betreuung: any): void {
        if (kind && betreuung) {
            betreuung.isSelected = false; // damit die row in der Tabelle nicht mehr als "selected" markiert ist
            this.openBetreuungView(betreuung.betreuungNummer, kind.kindNummer);
        }
    }

    public isNotAllowedToRemove(betreuung: TSBetreuung): boolean {
        if (betreuung.betreuungsstatus === TSBetreuungsstatus.ABGEWIESEN
            && this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getAdministratorOrAmtRole())) {
            return false;
        }

        return this.isKorrekturModusJugendamt();
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

    public hasProvisorischeBetreuungen(): boolean {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().hasProvisorischeBetreuungen();
        }
        return false;
    }

    public createBetreuung(kind: TSKindContainer): void {
        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kind.kindNummer);
        if (kindIndex < 0) {
            this.$log.error('kind nicht gefunden ', kind);
            return;
        }
        this.gesuchModelManager.setKindIndex(kindIndex);
        this.resetActiveInstitutionenForGemeindeList();
        this.openBetreuungView(undefined, kind.kindNummer);
    }

    private resetActiveInstitutionenForGemeindeList(): void {
        // Beim Navigieren auf die BetreuungView muss eventuell die Liste der Institutionen neu geladen werden.
        // Diese wird im GMM gecached und enthält eventuell nicht die neuesten Daten, insbesondere beim Hinzufügen von
        // Betreuungen.
        if (this.authServiceRS.isRole(TSRole.GESUCHSTELLER)) {
            this.gesuchModelManager.resetActiveInstitutionenForGemeindeList();
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
        if (kindIndex < 0) {
            this.$log.error('kind nicht gefunden ', kind);
            return;
        }
        this.gesuchModelManager.setKindIndex(kindIndex);
        this.resetActiveInstitutionenForGemeindeList();
        this.openAnmeldungView(kind.kindNummer, betreuungstyp);
    }

    public removeBetreuung(kind: TSKindContainer, betreuung: TSBetreuung, index: any): void {
        this.gesuchModelManager.findKind(kind);     // kind index setzen
        const typ = TSBetreuungsangebotTyp[betreuung.institutionStammdaten.betreuungsangebotTyp];
        const remTitleText: any = this.$translate.instant('BETREUUNG_LOESCHEN', {
            kindname: this.gesuchModelManager.getKindToWorkWith().kindJA.getFullName(),
            betreuungsangebottyp: this.ebeguUtil.translateString(typ),
        });
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: remTitleText,
            deleteText: 'BETREUUNG_LOESCHEN_BESCHREIBUNG',
            parentController: this,
            elementID: `removeBetreuungButton${kind.kindNummer}_${index}`,
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
            gesuchId: this.getGesuchId(),
        });
    }

    private openAnmeldungView(kindNumber: number, betreuungsangebotTyp: TSBetreuungsangebotTyp): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber: undefined,
            kindNumber,
            gesuchId: this.getGesuchId(),
            betreuungsangebotTyp: betreuungsangebotTyp.toString(),
        });
    }

    /**
     * Gibt den Betreuungsangebottyp der Institution, die mit der gegebenen Betreuung verknuepft ist zurueck.
     * By default wird ein Leerzeichen zurueckgeliefert.
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
            detail = `${detail} (${ferien})`;
        }
        return detail;
    }

    public canRemoveBetreuung(betreuung: TSBetreuung): boolean {
        return !this.isGesuchReadonly() && !betreuung.vorgaengerId && !betreuung.isSchulamtangebotAusgeloest();
    }

    public showMitteilung(): boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
    }

    public gotoMitteilung(betreuung: TSBetreuung): void {
        this.$state.go('gesuch.mitteilung', {
            dossierId: this.gesuchModelManager.getDossier().id,
            gesuchId: this.gesuchModelManager.getGesuch().id,
            betreuungId: betreuung.id,
            mitteilungId: undefined,
        });
    }

    public setFocusBack(elementID: string): void {
        angular.element(`#${elementID}`).first().focus();
    }

    public showButtonAnmeldungTagesschule(): boolean {
        return this.isAnmeldungTagesschuleEnabledForMandantAndGemeinde()
            && this.isAnmeldungenTagesschuleEnabledForGemeindeAndGesuchsperiode()
            && this.isAnmeldungenHinzufuegenMoeglich();
    }

    public showButtonAnmeldungFerieninsel(): boolean {
        return this.isAnmeldungFerieninselEnabledForMandantAndGemeinde()
            && this.isAnmeldungenFerieninselEnabledForGemeindeAndGesuchsperiode()
            && this.isAnmeldungenHinzufuegenMoeglich();
    }

    /**
     * Entscheidet, ob Tagesschulen sowohl für den Mandanten wie auch für die Gemeinde eingeschaltet sind
     */
    private isAnmeldungTagesschuleEnabledForMandantAndGemeinde(): boolean {
        if (!this.gesuchModelManager.isTagesschulangebotEnabled()) {
            // Tagesschulen sind grundsätzlich auf dem Mandant nicht eingeschaltet
            return false;
        }
        const gemeinde = this.gesuchModelManager.getGemeinde();
        const gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        return gemeinde
            && gemeinde.angebotTS
            && gesuchsperiode
            && gesuchsperiode.gueltigkeit.gueltigBis.isAfter(gemeinde.tagesschulanmeldungenStartdatum);
    }

    /**
     * Entscheidet, ob Ferieninseln sowohl für den Mandanten wie auch für die Gemeinde eingeschaltet sind
     */
    private isAnmeldungFerieninselEnabledForMandantAndGemeinde(): boolean {
        if (!this.gesuchModelManager.isFerieninselangebotEnabled()) {
            // Ferieninsel sind grundsätzlich auf dem Mandant nicht eingeschaltet
            return false;
        }
        const gemeinde = this.gesuchModelManager.getGemeinde();
        const gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        return gemeinde
            && gemeinde.angebotFI
            && gesuchsperiode
            && gesuchsperiode.gueltigkeit.gueltigBis.isAfter(gemeinde.ferieninselanmeldungenStartdatum);
    }

    /**
     * Entscheidet, ob für die aktuelle Gesuchsperiode und Gemeinde die Anmeldung für Tagesschulen
     * (aufgrund des Datums) möglich ist.
     */
    private isAnmeldungenTagesschuleEnabledForGemeindeAndGesuchsperiode(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration
            && this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung();
    }

    /**
     * Entscheidet, ob für die aktuelle Gesuchsperiode und Gemeinde die Anmeldung für Ferieninseln (aufgrund des
     * Datums) möglich ist.
     */
    private isAnmeldungenFerieninselEnabledForGemeindeAndGesuchsperiode(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration
            && this.gesuchModelManager.gemeindeKonfiguration.hasFerieninseAnmeldung();
    }

    /**
     * Entscheidet aufgrund des Gesuchstatus und der Rolle des Benutzers ob in diesem Zustand grundsätzlich
     * (nachträgliche) Anmeldungen (für Tagesschulen oder Ferieninseln) möglich sind. Es muss separat geprüft werden,
     * ob die spezifische Anmeldung überhaupt für den Mandanten oder die Gemeinde eingeschaltet ist.
     */
    private isAnmeldungenHinzufuegenMoeglich(): boolean {
        const isStatus = isStatusVerfuegenVerfuegt(this.gesuchModelManager.getGesuch().status)
            || this.gesuchModelManager.isGesuchReadonlyForRole()
            || this.gesuchModelManager.isKorrekturModusJugendamt()
            || this.gesuchModelManager.getGesuch().gesperrtWegenBeschwerde;
        const allowedRoles = TSRoleUtil.getAdministratorJugendamtSchulamtSozialdienstGesuchstellerRoles();
        const isRole = this.authServiceRS.isOneOfRoles(allowedRoles);
        const istNotStatusFreigabequittung = this.gesuchModelManager.getGesuch().status !== TSAntragStatus.FREIGABEQUITTUNG;
        return isStatus && isRole && istNotStatusFreigabequittung && this.gesuchModelManager.isNeuestesGesuch();
    }

    /**
     * Die Information, dass im Moment keine Anmeldungen möglich sind weil die Quittung noch nicht eingetroffen ist,
     * soll in folgenden Fällen angezeigt werden:
     * - Status = FREIGABEQUITTUNG
     * - Anmeldungen entweder für Tagesschule ODER für Ferieninsel wären grundsätzlich möglich
     */
    public showAnmeldungenImMomentNichtMoeglichMessage(): boolean {
        if (this.gesuchModelManager.getGesuch().status !== TSAntragStatus.FREIGABEQUITTUNG) {
            return false;
        }
        const tagesschuleGrundsaetzlichErlaubt = this.isAnmeldungTagesschuleEnabledForMandantAndGemeinde()
            && this.isAnmeldungenTagesschuleEnabledForGemeindeAndGesuchsperiode();
        const ferieninselGrundsaetzlichErlaubt = this.isAnmeldungFerieninselEnabledForMandantAndGemeinde()
            && this.isAnmeldungenFerieninselEnabledForGemeindeAndGesuchsperiode();
        return tagesschuleGrundsaetzlichErlaubt || ferieninselGrundsaetzlichErlaubt;
    }

    public isNeueBetreuungErlaubtForFI(): boolean {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!!gesuch && !gesuch.areThereOnlyFerieninsel()) {
            return true;
        }
        return !!gesuch && (gesuch.status === TSAntragStatus.IN_BEARBEITUNG_GS
            || gesuch.status === TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST
            || (EbeguUtil.isNullOrUndefined(gesuch.freigabeDatum)
                && gesuch.status === TSAntragStatus.IN_BEARBEITUNG_JA));
    }
}
