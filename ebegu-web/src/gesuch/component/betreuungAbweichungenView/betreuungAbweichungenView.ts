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
import {map} from 'rxjs/operators';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {MANDANTS} from '../../../app/core/constants/MANDANTS';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {BetreuungRS} from '../../../app/core/service/betreuungRS.rest';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSBetreuungspensumAbweichungStatus} from '../../../models/enums/betreuung/TSBetreuungspensumAbweichungStatus';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSPensumAnzeigeTyp} from '../../../models/enums/TSPensumAnzeigeTyp';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';
import {TSBetreuungspensumAbweichung} from '../../../models/TSBetreuungspensumAbweichung';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {OkHtmlDialogController} from '../../dialog/OkHtmlDialogController';
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

const LOG = LogFactory.createLog('BetreuungAbweichungenViewController');

export class BetreuungAbweichungenViewComponentConfig
    implements IComponentOptions
{
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
        'WizardStepManager',
        '$stateParams',
        'MitteilungRS',
        'BetreuungRS',
        '$log',
        'EinstellungRS',
        '$timeout',
        '$translate',
        'DvDialog',
        'MandantService',
        'EbeguRestUtil'
    ];

    public $translate: ITranslateService;

    public kindModel: TSKindContainer;
    public institution: string;
    public isSavingData: boolean; // Semaphore
    public dvDialog: DvDialog;
    public betreuungspensumAnzeigeTypEinstellung: TSPensumAnzeigeTyp;
    private existingMutationsmeldung: TSBetreuungsmitteilung;
    private isLuzern: boolean;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        public readonly CONSTANTS: any,
        $scope: IScope,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        private readonly $stateParams: IBetreuungStateParams,
        private readonly mitteilungRS: MitteilungRS,
        private readonly betreuungRS: BetreuungRS,
        private readonly $log: ILogService,
        private readonly einstellungRS: EinstellungRS,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
        dvDialog: DvDialog,
        private readonly mandantService: MandantService,
        private readonly ebeguRestUtil: EbeguRestUtil
    ) {
        super(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.BETREUUNG,
            $timeout
        );
        this.$translate = $translate;
        this.dvDialog = dvDialog;
    }

    public $onInit(): void {
        super.$onInit();

        this.mandantService.mandant$
            .pipe(map(mandant => mandant === MANDANTS.LUZERN))
            .subscribe(isLuzern => {
                this.isLuzern = isLuzern;
            });

        const kindNumber = parseInt(this.$stateParams.kindNumber, 10);
        const kindIndex =
            this.gesuchModelManager.convertKindNumberToKindIndex(kindNumber);
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            if (
                this.$stateParams.betreuungNumber &&
                this.$stateParams.betreuungNumber.length > 0
            ) {
                const betreuungNumber = parseInt(
                    this.$stateParams.betreuungNumber,
                    10
                );
                const betreuungIndex =
                    this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(
                        betreuungNumber
                    );
                this.model = angular.copy(
                    this.gesuchModelManager.getKindToWorkWith().betreuungen[
                        betreuungIndex
                    ]
                );
                this.institution =
                    this.model.institutionStammdaten.institution.name;
                this.gesuchModelManager.setBetreuungIndex(betreuungIndex);
            }

            // just to read!
            this.kindModel = this.gesuchModelManager.getKindToWorkWith();
        } else {
            this.$log.error(
                `There is no kind available with kind-number:${this.$stateParams.kindNumber}`
            );
        }
        this.model = angular.copy(
            this.gesuchModelManager.getBetreuungToWorkWith()
        );
        this.loadAbweichungen();

        this.mitteilungRS
            .getNewestBetreuungsmitteilung(this.model.id)
            .then((response: TSBetreuungsmitteilung) => {
                this.existingMutationsmeldung = response;
            });
        this.einstellungRS
            .getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode().id
            )
            .subscribe(
                einstellungen => {
                    this.loadEinstellungPensumAnzeigeTyp(einstellungen);
                },
                error => LOG.error(error)
            );
    }

    private loadEinstellungPensumAnzeigeTyp(
        einstellungen: TSEinstellung[]
    ): void {
        const einstellung = einstellungen.find(
            e => e.key === TSEinstellungKey.PENSUM_ANZEIGE_TYP
        );
        const einstellungPensumAnzeigeTyp =
            this.ebeguRestUtil.parsePensumAnzeigeTyp(einstellung);

        this.betreuungspensumAnzeigeTypEinstellung =
            EbeguUtil.isNotNullOrUndefined(einstellungPensumAnzeigeTyp)
                ? einstellungPensumAnzeigeTyp
                : TSPensumAnzeigeTyp.ZEITEINHEIT_UND_PROZENT;
    }

    public getKindModel(): TSKindContainer {
        return this.kindModel;
    }

    public loadAbweichungen(): void {
        this.betreuungRS.loadAbweichungen(this.model.id).then(data => {
            this.model.betreuungspensumAbweichungen = data;
        });
    }

    public getFormattedDate(abweichung: TSBetreuungspensumAbweichung): string {
        return `${abweichung.gueltigkeit.gueltigAb.month() + 1}.${abweichung.gueltigkeit.gueltigAb.year()}`;
    }

    public updateStatus(abweichung: TSBetreuungspensumAbweichung): void {
        abweichung.status = TSBetreuungspensumAbweichungStatus.NONE;

        if (
            abweichung.pensum !== null &&
            abweichung.pensum >= 0 &&
            abweichung.monatlicheBetreuungskosten !== null &&
            abweichung.monatlicheBetreuungskosten >= 0
        ) {
            abweichung.status =
                TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN;
        }
    }

    public updateStatusMittagstisch(
        abweichung: TSBetreuungspensumAbweichung
    ): void {
        abweichung.status =
            Number(abweichung.monatlicheHauptmahlzeiten) > 0 &&
            Number(abweichung.tarifProHauptmahlzeit) > 0
                ? TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN
                : TSBetreuungspensumAbweichungStatus.NONE;
    }

    public getIcon(abweichung: TSBetreuungspensumAbweichung): string {
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

    public getIconTooltip(abweichung: TSBetreuungspensumAbweichung): string {
        switch (abweichung.status) {
            case TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN:
                return this.$translate.instant(
                    `TSBetreuungspensumAbweichungStatus_${TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN}`
                );
            case TSBetreuungspensumAbweichungStatus.VERRECHNET:
                return this.$translate.instant(
                    `TSBetreuungspensumAbweichungStatus_${TSBetreuungspensumAbweichungStatus.VERRECHNET}`
                );
            case TSBetreuungspensumAbweichungStatus.UEBERNOMMEN:
                return this.$translate.instant(
                    `TSBetreuungspensumAbweichungStatus_${TSBetreuungspensumAbweichungStatus.UEBERNOMMEN}`
                );
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
        if (
            !this.isMahlzeitenverguenstigungEnabled() &&
            this.getBetreuungsangebotTyp() !==
                TSBetreuungsangebotTyp.MITTAGSTISCH
        ) {
            this.model.betreuungspensumAbweichungen.forEach(a => {
                a.monatlicheNebenmahlzeiten ??= 0;
                a.tarifProNebenmahlzeit ??= 0;
                a.monatlicheHauptmahlzeiten ??= 0;
                a.tarifProHauptmahlzeit ??= 0;
            });
        }

        this.betreuungRS.saveAbweichungen(this.model).then(result => {
            this.model.betreuungspensumAbweichungen = result;
            this.dvDialog.showDialog(
                okHtmlDialogTempl,
                OkHtmlDialogController,
                {
                    title: 'SPEICHERN_ERFOLGREICH'
                }
            );
        });
    }

    /**
     * Prüft, ob es eine Mutationsmeldung gibt, die noch nicht applied ist.
     * Prueft dass das Objekt existingMutationsMeldung existiert und dass es ein sentDatum hat. Das wird gebraucht,
     * um zu vermeiden, dass ein leeres Objekt als gueltiges Objekt erkannt wird.
     * Ausserdem muss die Meldung nicht applied sein und nicht den Status ERLEDIGT haben
     */
    public hasNotAppliedMutationsmeldung(): boolean {
        return (
            this.existingMutationsmeldung !== undefined &&
            this.existingMutationsmeldung !== null &&
            this.existingMutationsmeldung.sentDatum !== undefined &&
            this.existingMutationsmeldung.sentDatum !== null &&
            !this.existingMutationsmeldung.applied &&
            !this.existingMutationsmeldung.isErledigt()
        );
    }

    public preFreigeben(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        if (!this.hasNotAppliedMutationsmeldung()) {
            this.freigeben();
            return;
        }

        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                this.form,
                RemoveDialogController,
                {
                    title: 'MUTATIONSMELDUNG_OVERRIDE_EXISTING_TITLE',
                    deleteText: 'MUTATIONSMELDUNG_OVERRIDE_EXISTING_BODY',
                    parentController: undefined,
                    elementID: undefined
                }
            )
            .then(() => {
                // User confirmed removal
                this.freigeben();
            });
    }

    public freigeben(): void {
        this.mitteilungRS
            .abweichungenFreigeben(
                this.model,
                this.gesuchModelManager.getDossier()
            )
            .then(result => {
                this.model.betreuungspensumAbweichungen = result;
            });
    }

    public isDisabled(abweichung: TSBetreuungspensumAbweichung): boolean {
        return (
            abweichung.status ===
                TSBetreuungspensumAbweichungStatus.VERRECHNET ||
            EbeguUtil.isNullOrUndefined(abweichung.vertraglicheKosten)
        );
    }

    public isAbweichungAllowed(): boolean {
        return super.isMutationsmeldungAllowed(
            this.model,
            this.gesuchModelManager.isNeuestesGesuch()
        );
    }

    public isFreigabeAllowed(): boolean {
        return (
            this.isAbweichungAllowed() &&
            this.hasAbweichungInStatus(
                TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN
            ) &&
            !this.isDirty()
        );
    }

    public hasAbweichungInStatus(
        status: TSBetreuungspensumAbweichungStatus
    ): boolean {
        for (const a of this.model.betreuungspensumAbweichungen) {
            if (a.status === status) {
                return true;
            }
        }
        return false;
    }

    public isDirty(): boolean {
        for (const a of this.model.betreuungspensumAbweichungen) {
            if (
                a.isNew() &&
                a.status ===
                    TSBetreuungspensumAbweichungStatus.NICHT_FREIGEGEBEN
            ) {
                return true;
            }
        }
        return false;
    }

    public getHelpText(): string {
        return this.$translate.instant('ABWEICHUNGEN_HELP', {
            vorname: this.kindModel.kindJA.vorname,
            name: this.kindModel.kindJA.nachname,
            institution: this.institution
        });
    }

    public getBetreuungsangebotTyp(): TSBetreuungsangebotTyp {
        return this.model.getAngebotTyp();
    }

    public getInputFormatTitle(): string {
        return this.getBetreuungsangebotTyp() === TSBetreuungsangebotTyp.KITA &&
            this.betreuungspensumAnzeigeTypEinstellung !==
                TSPensumAnzeigeTyp.NUR_STUNDEN
            ? this.$translate.instant('DAYS')
            : this.$translate.instant('HOURS');
    }

    public cancel(): void {
        this.form.$setPristine();
        this.$state.go(GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
    }

    public isMahlzeitenverguenstigungEnabled(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration
            .konfigMahlzeitenverguenstigungEnabled;
    }

    public isRowRequired(abweichung: TSBetreuungspensumAbweichung): boolean {
        if (this.isMahlzeitenverguenstigungEnabled()) {
            return (
                EbeguUtil.isNotNullAndPositive(
                    abweichung.monatlicheHauptmahlzeiten
                ) ||
                EbeguUtil.isNotNullAndPositive(
                    abweichung.monatlicheNebenmahlzeiten
                ) ||
                EbeguUtil.isNotNullAndPositive(abweichung.pensum) ||
                EbeguUtil.isNotNullAndPositive(
                    abweichung.monatlicheBetreuungskosten
                )
            );
        }
        return (
            EbeguUtil.isNotNullAndPositive(abweichung.pensum) ||
            EbeguUtil.isNotNullAndPositive(
                abweichung.monatlicheBetreuungskosten
            )
        );
    }

    public getMonthlyMahlzeitenKosten(
        abweichung: TSBetreuungspensumAbweichung
    ): number {
        const hauptmahlzeiten = EbeguUtil.isNullOrUndefined(
            abweichung.monatlicheHauptmahlzeiten
        )
            ? abweichung.vertraglicheHauptmahlzeiten
            : abweichung.monatlicheHauptmahlzeiten;

        const nebenmahlzeiten = EbeguUtil.isNullOrUndefined(
            abweichung.monatlicheNebenmahlzeiten
        )
            ? abweichung.vertraglicheNebenmahlzeiten
            : abweichung.monatlicheNebenmahlzeiten;

        return (
            hauptmahlzeiten * abweichung.vertraglicherTarifHaupt +
            nebenmahlzeiten * abweichung.vertraglicherTarifNeben
        );
    }

    public getStepSize(): string {
        return this.isLuzern ? '0.01' : '0.25';
    }
}
