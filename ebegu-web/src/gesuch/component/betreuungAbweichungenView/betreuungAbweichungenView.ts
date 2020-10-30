/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {BetreuungRS} from '../../../app/core/service/betreuungRS.rest';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungspensumAbweichungStatus} from '../../../models/enums/TSBetreuungspensumAbweichungStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';
import {TSBetreuungspensumAbweichung} from '../../../models/TSBetreuungspensumAbweichung';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {OkHtmlDialogController} from '../../dialog/OkHtmlDialogController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');
const okHtmlDialogTempl = require('../../dialog/okHtmlDialogTemplate.html');
const GESUCH_BETREUUNGEN = 'gesuch.betreuungen';

export class BetreuungAbweichungenViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./betreuungAbweichungenView.html');
    public controller = BetreuungAbweichungenViewController;
    public controllerAs = 'vm';
}

export class BetreuungAbweichungenViewController extends AbstractGesuchViewController<TSBetreuung> {

    public static $inject = [
        '$state',
        'GesuchModelManager',
        'CONSTANTS',
        '$scope',
        'BerechnungsManager',
        'ErrorService',
        'AuthServiceRS',
        'WizardStepManager',
        '$stateParams',
        'MitteilungRS',
        'BetreuungRS',
        '$log',
        'EinstellungRS',
        '$timeout',
        '$translate',
        'DvDialog',
    ];

    public $translate: ITranslateService;

    public kindModel: TSKindContainer;
    public institution: string;
    public isSavingData: boolean; // Semaphore
    public dvDialog: DvDialog;
    private existingMutationsmeldung: TSBetreuungsmitteilung;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        private readonly CONSTANTS: any,
        $scope: IScope,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $stateParams: IBetreuungStateParams,
        private readonly mitteilungRS: MitteilungRS,
        private readonly betreuungRS: BetreuungRS,
        private readonly $log: ILogService,
        private readonly einstellungRS: EinstellungRS,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
        dvDialog: DvDialog,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
        this.$translate = $translate;
        this.dvDialog = dvDialog;
    }

    // tslint:disable-next-line:cognitive-complexity
    public $onInit(): void {
        super.$onInit();

        const kindNumber = parseInt(this.$stateParams.kindNumber, 10);
        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kindNumber);
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            if (this.$stateParams.betreuungNumber && this.$stateParams.betreuungNumber.length > 0) {
                const betreuungNumber = parseInt(this.$stateParams.betreuungNumber, 10);
                const betreuungIndex = this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(betreuungNumber);
                this.model = angular.copy(this.gesuchModelManager.getKindToWorkWith().betreuungen[betreuungIndex]);
                this.institution = this.model.institutionStammdaten.institution.name;
                this.gesuchModelManager.setBetreuungIndex(betreuungIndex);
            }

            // just to read!
            this.kindModel = this.gesuchModelManager.getKindToWorkWith();
        } else {
            this.$log.error('There is no kind available with kind-number:' + this.$stateParams.kindNumber);
        }
        this.model = angular.copy(this.gesuchModelManager.getBetreuungToWorkWith());
        this.loadAbweichungen();

        this.mitteilungRS.getNewestBetreuungsmitteilung(this.model.id)
            .then((response: TSBetreuungsmitteilung) => {
                this.existingMutationsmeldung = response;
            });
    }

    public getKindModel(): TSKindContainer {
        return this.kindModel;
    }

    public getAbweichung(index: number): TSBetreuungspensumAbweichung {
        if (this.model.betreuungspensumAbweichungen && index >= 0
            && index < this.model.betreuungspensumAbweichungen.length) {
            return this.model.betreuungspensumAbweichungen[index];
        }

        return undefined;
    }

    public loadAbweichungen(): void {
        this.betreuungRS.loadAbweichungen(this.model.id).then(data => {
            this.model.betreuungspensumAbweichungen = data;
        });
    }

    public getFormattedDate(abweichung: TSBetreuungspensumAbweichung): string {
        return `${abweichung.gueltigkeit.gueltigAb.month() + 1}.${abweichung.gueltigkeit.gueltigAb.year()}`;
    }

    public updateStatus(index: number): void {
        const abweichung = this.getAbweichung(index);
        abweichung.status = TSBetreuungspensumAbweichungStatus.NONE;

        if (abweichung.pensum !== null && abweichung.pensum >= 0
            && abweichung.monatlicheBetreuungskosten !== null
            && abweichung.monatlicheBetreuungskosten >= 0) {
            abweichung.status = TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN;
        }
    }

    public getIcon(index: number): string {
        const abweichung = this.getAbweichung(index);
        switch (abweichung.status) {
            case TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN:
                return 'fa-pencil black';
            case TSBetreuungspensumAbweichungStatus.VERRECHNET:
                return 'fa-hourglass orange';
            case TSBetreuungspensumAbweichungStatus.UEBERNOMMEN:
                return 'fa-check green';
            default:
                return '';
        }
    }

    public getIconTooltip(index: number): string {
        const abweichung = this.getAbweichung(index);
        switch (abweichung.status) {
            case TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN:
                return this.$translate.instant('TSBetreuungspensumAbweichungStatus_'
                    + TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN);
            case TSBetreuungspensumAbweichungStatus.VERRECHNET:
                return this.$translate.instant('TSBetreuungspensumAbweichungStatus_'
                    + TSBetreuungspensumAbweichungStatus.VERRECHNET);
            case TSBetreuungspensumAbweichungStatus.UEBERNOMMEN:
                return this.$translate.instant('TSBetreuungspensumAbweichungStatus_'
                    + TSBetreuungspensumAbweichungStatus.UEBERNOMMEN);
            default:
                return '';
        }
    }

    public save(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        // die felder sind not null und müssen auf 0 gesetzt werden, damit die validierung nicht fehlschlägt falls
        // die gemeinde die vergünstigung deaktiviert hat
        if (!this.isMahlzeitenverguenstigungEnabled()) {
            this.model.betreuungspensumAbweichungen.forEach(a => {
                a.monatlicheNebenmahlzeiten = 0;
                a.monatlicheHauptmahlzeiten = 0;
            });
        }

        this.betreuungRS.saveAbweichungen(this.model).then(result => {
            this.model.betreuungspensumAbweichungen = result;
            this.dvDialog.showDialog(okHtmlDialogTempl, OkHtmlDialogController, {
                title: 'SPEICHERN_ERFOLGREICH',
            });
        });
    }

    /**
     * Prueft dass das Objekt existingMutationsMeldung existiert und dass es ein sentDatum hat. Das wird gebraucht,
     * um zu vermeiden, dass ein leeres Objekt als gueltiges Objekt erkannt wird.
     * Ausserdem muss die Meldung nicht applied sein und nicht den Status ERLEDIGT haben
     */
    public hasNotAppliedMutationsmeldung(): boolean {
        return this.existingMutationsmeldung !== undefined && this.existingMutationsmeldung !== null
            && this.existingMutationsmeldung.sentDatum !== undefined && this.existingMutationsmeldung.sentDatum !== null
            && !this.existingMutationsmeldung.applied && !this.existingMutationsmeldung.isErledigt();
    }

    public preFreigeben(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        if (!this.hasNotAppliedMutationsmeldung()) {
            this.freigeben();
        }

        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'MUTATIONSMELDUNG_OVERRIDE_EXISTING_TITLE',
            deleteText: 'MUTATIONSMELDUNG_OVERRIDE_EXISTING_BODY',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.freigeben();
        });
    }

    public freigeben(): void {
        this.mitteilungRS.abweichungenFreigeben(this.model, this.gesuchModelManager.getDossier())
            .then(result => {
                this.model.betreuungspensumAbweichungen = result;
            });
    }

    public isDisabled(index: number): boolean {
        return this.getAbweichung(index).status === TSBetreuungspensumAbweichungStatus.VERRECHNET;
    }

    public isAbweichungAllowed(): boolean {
        return super.isMutationsmeldungAllowed(this.model, this.gesuchModelManager.isNeuestesGesuch());
    }

    public isFreigabeAllowed(): boolean {
        return this.isAbweichungAllowed()
            && this.hasAbweichungInStatus(TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN)
            && !this.isDirty();
    }

    public hasAbweichungInStatus(status: TSBetreuungspensumAbweichungStatus): boolean {
        for (const a of this.model.betreuungspensumAbweichungen) {
            if (a.status === status) {
                return true;
            }
        }
        return false;
    }

    public isDirty(): boolean {
        for (const a of this.model.betreuungspensumAbweichungen) {
            if (a.isNew() && a.status === TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN) {
                return true;
            }
        }
        return false;
    }

    public getHelpText(): string {
        return this.$translate.instant('ABWEICHUNGEN_HELP',
            {
                vorname: this.kindModel.kindJA.vorname,
                name: this.kindModel.kindJA.nachname,
                institution: this.institution
            });
    }

    public getInputFormatTitle(): string {
        return this.model.getAngebotTyp() === TSBetreuungsangebotTyp.KITA
            ? this.$translate.instant('DAYS')
            : this.$translate.instant('HOURS');
    }

    public cancel(): void {
        this.form.$setPristine();
        this.$state.go(GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
    }

    public isMahlzeitenverguenstigungEnabled(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration.konfigMahlzeitenverguenstigungEnabled;
    }

    public isRowRequired(index: number): boolean {
        const abweichung = this.getAbweichung(index);

        if (this.isMahlzeitenverguenstigungEnabled()) {
            return EbeguUtil.isNotNullAndPositive(abweichung.monatlicheHauptmahlzeiten)
                || EbeguUtil.isNotNullAndPositive(abweichung.monatlicheNebenmahlzeiten)
                || EbeguUtil.isNotNullAndPositive(abweichung.pensum)
                || EbeguUtil.isNotNullAndPositive(abweichung.monatlicheBetreuungskosten);
        }
        return EbeguUtil.isNotNullAndPositive(abweichung.pensum)
            || EbeguUtil.isNotNullAndPositive(abweichung.monatlicheBetreuungskosten);
    }

    public getMonthlyMahlzeitenKosten(index: number): number {
        const abweichung = this.getAbweichung(index);

        const hauptmahlzeiten = EbeguUtil.isNullOrUndefined(abweichung.monatlicheHauptmahlzeiten)
            ? abweichung.vertraglicheHauptmahlzeiten : abweichung.monatlicheHauptmahlzeiten;

        const nebenmahlzeiten = EbeguUtil.isNullOrUndefined(abweichung.monatlicheNebenmahlzeiten)
            ? abweichung.vertraglicheNebenmahlzeiten : abweichung.monatlicheNebenmahlzeiten;

        return (hauptmahlzeiten * abweichung.vertraglicherTarifHaupt) + (nebenmahlzeiten * abweichung.vertraglicherTarifNeben);

    }
}
