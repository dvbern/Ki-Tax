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
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../../../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFamilienstatus} from '../../../models/enums/TSFamilienstatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSUnterhaltsvereinbarungAnswer} from '../../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSErwerbspensumContainer} from '../../../models/TSErwerbspensumContainer';
import {TSFamiliensituation} from '../../../models/TSFamiliensituation';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');
const LOG = LogFactory.createLog('ErwerbspensumListViewComponent');

export class ErwerbspensumListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./erwerbspensumListView.html');
    public controller = ErwerbspensumListViewController;
    public controllerAs = 'vm';
}

export class ErwerbspensumListViewController
    extends AbstractGesuchViewController<any> implements IDVFocusableController {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        'BerechnungsManager',
        'DvDialog',
        'ErrorService',
        'WizardStepManager',
        '$scope',
        'AuthServiceRS',
        '$timeout',
        '$translate',
        'EinstellungRS',
        'EbeguRestUtil'
    ];

    public erwerbspensenGS1: Array<TSErwerbspensumContainer> = undefined;
    public erwerbspensenGS2: Array<TSErwerbspensumContainer>;
    public erwerbspensumRequired: boolean;      // ich muss es ausfuellen
    public erwerbspensumNotAllowed: boolean;    // ich darf es nicht ausfuellen
    public showInfoAusserordentlichenAnspruch: boolean;
    public gemeindeTelefon: string = '';
    public gemeindeEmail: string = '';
    private isGesuchBeendenFamSitActive = false;
    private anspruchBeschaeftigungAbhaengigkeit: TSAnspruchBeschaeftigungAbhaengigkeitTyp;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly dvDialog: DvDialog,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        private readonly authServiceRS: AuthServiceRS,
        $timeout: ITimeoutService,
        private readonly $translate: ITranslateService,
        private readonly einstellungRS: EinstellungRS,
        private readonly ebeguRestUtil: EbeguRestUtil,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.ERWERBSPENSUM,
            $timeout);
        this.initViewModel();
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id
        ).subscribe((response: TSEinstellung[]) => {
            response.filter(r => r.key === TSEinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2)
                .forEach(value => {
                    this.isGesuchBeendenFamSitActive = value.getValueAsBoolean();
                });
            const einstellung = response.find(r => r.key === TSEinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM);
            this.anspruchBeschaeftigungAbhaengigkeit =
                this.ebeguRestUtil.parseAnspruchBeschaeftigungAbhaengigkeitTyp(einstellung);
        }, error => LOG.error(error));
    }

    private initViewModel(): void {
        this.erwerbspensumNotAllowed = !(this.getGesuch() && this.getGesuch().hasAnyJugendamtAngebot());
        if (EbeguUtil.isNotNullOrUndefined(this.getGesuchId())) {
            this.gesuchModelManager.isErwerbspensumRequired(this.getGesuchId()).then((response: boolean) => {
                this.erwerbspensumRequired = response;
                if (this.isSaveDisabled() || this.isErwerbspensumGS2Required() && !this.showErwerbspensumGS2()) {
                    this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                        TSWizardStepName.ERWERBSPENSUM,
                        TSWizardStepStatus.IN_BEARBEITUNG);
                } else {
                    this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                        TSWizardStepName.ERWERBSPENSUM,
                        TSWizardStepStatus.OK);
                }
            });
        }
        this.setShowInfoAusserordentlichenAnspruchIfPossible();
        if (EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.gemeindeStammdaten)) {
            this.gemeindeTelefon = this.gesuchModelManager.gemeindeStammdaten.telefon;
            this.gemeindeEmail = this.gesuchModelManager.gemeindeStammdaten.mail;
        }
    }

    private setShowInfoAusserordentlichenAnspruchIfPossible(): void {
        if (this.getKonfigAnspruchUnabhaengigVomBeschaeftigungsPensumForGemeinde() !==
            TSAnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING) {
            this.showInfoAusserordentlichenAnspruch = false;
            return;
        }

        this.gesuchModelManager.showInfoAusserordentlichenAnspruch().then((resp: any) => {
            this.showInfoAusserordentlichenAnspruch = JSON.parse(resp);
            this.showInfoAusserordentlichenAnspruch =
                this.showInfoAusserordentlichenAnspruch
                && !this.gesuchModelManager.getGesuch().allKindHaveAusserordentlicherAnspruch()
                && !this.isSaveDisabled();
        });
    }

    public getErwerbspensenListGS1(): Array<TSErwerbspensumContainer> {
        if (this.erwerbspensenGS1 === undefined) {
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller1 &&
                this.gesuchModelManager.getGesuch().gesuchsteller1.erwerbspensenContainer) {
                const gesuchsteller1 = this.gesuchModelManager.getGesuch().gesuchsteller1;
                this.erwerbspensenGS1 = gesuchsteller1.erwerbspensenContainer;

            } else {
                this.erwerbspensenGS1 = [];
            }
        }
        return this.erwerbspensenGS1;
    }

    public getErwerbspensenListGS2(): Array<TSErwerbspensumContainer> {
        if (this.erwerbspensenGS2 === undefined) {
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller2 &&
                this.gesuchModelManager.getGesuch().gesuchsteller2.erwerbspensenContainer) {
                const gesuchsteller2 = this.gesuchModelManager.getGesuch().gesuchsteller2;
                this.erwerbspensenGS2 = gesuchsteller2.erwerbspensenContainer;

            } else {
                this.erwerbspensenGS2 = [];
            }
        }
        return this.erwerbspensenGS2;
    }

    public createErwerbspensum(gesuchstellerNumber: number): void {
        this.openErwerbspensumView(gesuchstellerNumber, undefined);
    }

    public removePensum(
        pensum: TSErwerbspensumContainer,
        gesuchstellerNumber: number,
        elementId: string,
        index: any
    ): void {
        // Spezielle Meldung, wenn es ein GS ist, der in einer Mutation loescht
        const principalRole = this.authServiceRS.getPrincipalRole();
        const gsInMutation = principalRole === TSRole.GESUCHSTELLER && pensum.vorgaengerId !== undefined;
        const pensumLaufendOderVergangen = pensum.erwerbspensumJA.gueltigkeit.gueltigAb.isBefore(moment(moment.now()));
        this.errorService.clearAll();
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: (gsInMutation && pensumLaufendOderVergangen) ? 'ERWERBSPENSUM_LOESCHEN_GS_MUTATION' : '',
            title: 'ERWERBSPENSUM_LOESCHEN',
            parentController: this,
            elementID: elementId + String(index)
        })
            .then(() => {   // User confirmed removal
                this.gesuchModelManager.setGesuchstellerNumber(gesuchstellerNumber);
                this.gesuchModelManager.removeErwerbspensum(pensum).then(() => {
                    this.setShowInfoAusserordentlichenAnspruchIfPossible();
                });
            });

    }

    public editPensum(pensum: any, gesuchstellerNumber: any): void {
        const index = this.gesuchModelManager.findIndexOfErwerbspensum(parseInt(gesuchstellerNumber, 10), pensum);
        this.openErwerbspensumView(gesuchstellerNumber, index);
    }

    private openErwerbspensumView(gesuchstellerNumber: number, erwerbspensumNum: number): void {
        this.$state.go('gesuch.erwerbsPensum', {
            gesuchstellerNumber,
            erwerbspensumNum,
            gesuchId: this.getGesuchId()
        });
    }

    /**
     * Gibt true zurueck wenn Erwerbspensen nicht notwendig sind oder wenn sie notwendig sind aber mindestens eines pro
     * Gesuchsteller eingegeben wurde.
     */
    public isSaveDisabled(): boolean {
        if (this.erwerbspensumRequired === false) {
            return false;
        }

        if (this.getErwerbspensenListGS1() && this.getErwerbspensenListGS1().length <= 0) {
            return true;
        }

        return this.isErwerbspensumRequiredForGS2();
    }

    private isErwerbspensumRequiredForGS2(): boolean {
        if (!this.gesuchModelManager.isGesuchsteller2Required()) {
            return false;
        }

        if (!this.showErwerbspensumGS2()) {
            return false;
        }

        return this.getErwerbspensenListGS2() && this.getErwerbspensenListGS2().length <= 0;
    }

    public setFocusBack(elementID: string): void {
        angular.element(`#${  elementID}`).first().focus();
    }

    public getErwerbspensumNotRequired(): string {
        const fiActive = this.gesuchModelManager.gemeindeKonfiguration
            && this.gesuchModelManager.gemeindeKonfiguration.isFerieninselanmeldungKonfiguriert();
        let undFerieninselnTxt = '';
        if (fiActive) {
            undFerieninselnTxt = this.$translate.instant('UND_FERIENINSELN');
        }
        return this.$translate.instant('ERWERBSPENSEN_NOT_REQUIRED', {
            undFerieninseln: undFerieninselnTxt
        });
    }

    public showErwerbspensumGS2(): boolean {
        if (EbeguUtil.isNullOrUndefined(this.gesuchModelManager.getGesuch())) {
            return false;
        }

        return this.isErwerbspensumGS2Required()
            && EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
    }

    public isErwerbspensumGS2Required(): boolean {
        const hasGS2 = this.gesuchModelManager.getFamiliensituation()
            .hasSecondGesuchsteller(this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis);
        if (!hasGS2) {
            return false;
        }
        let familiensituation = this.gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA;
        const partnerIdentischMitVorgesuch: boolean = this.getGesuch().extractFamiliensituation().partnerIdentischMitVorgesuch;
        if (EbeguUtil.isNotNullAndFalse(partnerIdentischMitVorgesuch)){
            familiensituation = this.getGesuch().extractFamiliensituationErstgesuch();
        }
        if (this.anspruchBeschaeftigungAbhaengigkeit === TSAnspruchBeschaeftigungAbhaengigkeitTyp.SCHWYZ) {
            return this.isErwerbspensumGS2RequiredSchwyz(familiensituation);
        }
        // Wenn zwei Gesuchsteller und keine Unterhatsvereinbarung abgeschlossen ist,
        // muss das Erwerbspensum von GS2 nicht angegeben werden
        const unterhaltsvereinbarung = familiensituation.unterhaltsvereinbarung;

        if (
            unterhaltsvereinbarung !== null
            && unterhaltsvereinbarung === TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG
            && (this.isPensumGS2InKonkubinatOmittable(familiensituation) || this.isAlleinerziehend(familiensituation))
        ) {
            return false;
        }
        if (this.getGesuch().isMutation()) {
            const famSitErstGesuch = this.getGesuch().extractFamiliensituationErstgesuch();
            if ((this.isShortKonkubinat(familiensituation) || this.isAlleinerziehend(familiensituation)) &&
                (this.isShortKonkubinat(famSitErstGesuch) || this.isAlleinerziehend(famSitErstGesuch))
                && famSitErstGesuch.unterhaltsvereinbarung === TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG
            ) {
                return false;
            }
        }

        return true;
    }

    private isErwerbspensumGS2RequiredSchwyz(familiensituation: TSFamiliensituation): boolean {
        const hasAnyKindWithUnterhalt = this.getGesuch()
            .kindContainers
            .map(kc => kc.kindJA)
            .reduce((curr, kind) => curr || kind.gemeinsamesGesuch, false);
        return familiensituation.hasSecondGesuchsteller(this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis)
            && hasAnyKindWithUnterhalt;
    }

    private isPensumGS2InKonkubinatOmittable(familiensituation: TSFamiliensituation): boolean {
        if (familiensituation.familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            return false;
        }
        return this.isGesuchBeendenFamSitActive ?
            familiensituation.konkuinatOhneKindBecomesXYearsDuringPeriode(this.gesuchModelManager.getGesuchsperiode()) :
            this.isShortKonkubinat(familiensituation);
    }

    private isShortKonkubinat(familiensituation: TSFamiliensituation): boolean {

        if (familiensituation.familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            return false;
        }

        return !familiensituation.konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(
            this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis);
    }

    private isAlleinerziehend(familienSituation: TSFamiliensituation): boolean {
        return familienSituation.familienstatus === TSFamilienstatus.ALLEINERZIEHEND;
    }

    private getKonfigAnspruchUnabhaengigVomBeschaeftigungsPensumForGemeinde(): TSAnspruchBeschaeftigungAbhaengigkeitTyp {
        return this.gesuchModelManager.gemeindeKonfiguration.anspruchUnabhaengingVonBeschaeftigungsPensum;
    }

    public anspruchUnabhaengingVomBeschaeftigungspensum(): boolean {
        return this.getKonfigAnspruchUnabhaengigVomBeschaeftigungsPensumForGemeinde()  ===
            TSAnspruchBeschaeftigungAbhaengigkeitTyp.UNABHAENGING;
    }

    public getGS2FullName(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }
}
