/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IPromise, IScope, ITimeoutService} from 'angular';
import * as moment from 'moment';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../../../app/core/constants/CONSTANTS';
import {DvDialog} from '../../../../app/core/directive/dv-dialog/dv-dialog';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../models/enums/TSEinstellungKey';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../abstractGesuchView';

const LOG = LogFactory.createLog('AbstractFinSitBernView');

const removeDialogTemplate = require('../../../dialog/removeDialogTemplate.html');

enum saveHints {
    LOADING = 'FINSIT_BERN_LOADING',
    SAVED = 'FINSIT_BERN_SAVED',
    ERROR = 'FINSIT_BERN_ERROR'
}

export abstract class AbstractFinSitBernView extends AbstractGesuchViewController<TSFinanzModel> {
    protected steuerSchnittstelleAktivForPeriode: boolean;
    public steuerSchnittstelleAktivAbStr: string;
    protected steuerSchnittstelleAkivAbInPast: boolean;
    protected zahlungsangabenRequired: boolean = false;
    protected finSitRequestState: string;
    protected finSitRequestRunning: boolean;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        $timeout: ITimeoutService,
        protected readonly authServiceRS: AuthServiceRS,
        protected readonly einstellungRS: EinstellungRS,
        protected readonly dvDialog: DvDialog,
        private readonly applicationPropertyRS: ApplicationPropertyRS
    ) {
        super(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout
        );

        this.loadEinstellungen();
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then(properties => {
                this.steuerSchnittstelleAkivAbInPast = moment().isAfter(
                    properties.steuerschnittstelleAktivAb
                );
                this.steuerSchnittstelleAktivAbStr =
                    properties.steuerschnittstelleAktivAb.format(
                        CONSTANTS.DATE_FORMAT
                    );
            });
    }

    private loadEinstellungen(): void {
        this.einstellungRS
            .getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode()?.id
            )
            .subscribe(
                einstellungen => {
                    const einstellungSteuerschnittstelle = einstellungen.find(
                        e =>
                            e.key ===
                            TSEinstellungKey.SCHNITTSTELLE_STEUERN_AKTIV
                    );
                    this.steuerSchnittstelleAktivForPeriode =
                        einstellungSteuerschnittstelle?.value === 'true';

                    const einstellungZahlungsangebenRequired =
                        einstellungen.find(
                            e =>
                                e.key ===
                                TSEinstellungKey.ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED
                        );
                    this.zahlungsangabenRequired =
                        einstellungZahlungsangebenRequired?.value === 'true';
                },
                error => LOG.error(error)
            );
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public steuerveranlagungClicked(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        // Wenn zusätzlich noch GemeinsameStek -> Dasselbe auch für GS2
        // Wenn Steuerveranlagung erhalten, muss auch STEK ausgefüllt worden sein
        if (this.getModel().finanzielleSituationJA.steuerveranlagungErhalten) {
            this.getModel().finanzielleSituationJA.steuererklaerungAusgefuellt =
                true;
            if (this.model.familienSituation.gemeinsameSteuererklaerung) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten =
                    true;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt =
                    true;
            }
        } else if (
            !this.getModel().finanzielleSituationJA.steuerveranlagungErhalten
        ) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.getModel().finanzielleSituationJA.steuererklaerungAusgefuellt =
                undefined;
            if (this.model.familienSituation.gemeinsameSteuererklaerung) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten =
                    false;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt =
                    undefined;
            }
        }
    }

    public isGesuchsteller(): boolean {
        return this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
    }

    public showSteuerdatenAbholenButton(): boolean {
        return (
            this.steuerSchnittstelleAktivForPeriode &&
            this.steuerSchnittstelleAkivAbInPast &&
            this.getModel().finanzielleSituationJA.steuerdatenZugriff &&
            this.isNotFinSitStartOrGS2Required() &&
            EbeguUtil.isNullOrUndefined(
                this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus
            )
        );
    }

    protected abstract isNotFinSitStartOrGS2Required(): boolean;

    public showWarningSteuerschnittstelleNotYetActive(): boolean {
        return (
            this.getModel().finanzielleSituationJA.steuerdatenZugriff &&
            !this.steuerSchnittstelleAkivAbInPast
        );
    }

    protected showResetDialog(): IPromise<void> {
        return this.dvDialog.showRemoveDialog(
            removeDialogTemplate,
            null,
            RemoveDialogController,
            {
                title: 'WOLLEN_SIE_FORTFAHREN',
                deleteText: 'RESET_KIBON_ABFRAGE_WARNING'
            }
        );
    }

    public resetKiBonAnfrageFinSitIfRequired(): void {
        if (
            EbeguUtil.isNullOrUndefined(
                this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus
            )
        ) {
            this.resetKiBonAnfrageFinSit();
            return;
        }
        this.showResetDialog().then(
            () => {
                this.resetKiBonAnfrageFinSit();
            },
            () =>
                (this.getModel().finanzielleSituationJA.steuerdatenZugriff =
                    true)
        );
    }

    public callKiBonAnfrageAndUpdateFinSit(): void {
        this.finSitRequestRunning = true;
        this.finSitRequestState = saveHints.LOADING;
        this.callKiBonAnfrage(
            EbeguUtil.isNotNullAndTrue(
                this.model.familienSituation.gemeinsameSteuererklaerung
            )
        )
            .then(() => {
                this.model.copyFinSitDataFromGesuch(
                    this.gesuchModelManager.getGesuch()
                );
                this.form.$setDirty();
                this.finSitRequestState = saveHints.SAVED;
            })
            .catch(() => {
                this.finSitRequestState = saveHints.ERROR;
            })
            .finally(() => {
                this.finSitRequestState = saveHints.SAVED;
            });
        setTimeout(() => {
            this.finSitRequestRunning = false;
        }, 5000);
    }

    protected abstract resetKiBonAnfrageFinSit(): void;

    protected abstract showAutomatischePruefungSteuerdatenFrage(): boolean;

    public resetAutomatischePruefungSteuerdaten(): void {
        this.getModel().finanzielleSituationJA.automatischePruefungErlaubt =
            undefined;
    }

    public einkommenInVereinfachtemVerfarenClicked(): void {
        this.getModel().finanzielleSituationJA.amountEinkommenInVereinfachtemVerfahrenAbgerechnet =
            null;
    }

    protected showZugriffAufSteuerdaten(): boolean {
        if (!this.steuerSchnittstelleAktivForPeriode) {
            return false;
        }

        if (this.gesuchModelManager.getFall().isSozialdienstFall()) {
            return false;
        }

        if (
            !this.gesuchModelManager.getGesuch().isOnlineGesuch() &&
            !this.showZugriffAufSteuerdatenForGemeinde()
        ) {
            return false;
        }

        return (
            this.authServiceRS.isOneOfRoles([
                TSRole.GESUCHSTELLER,
                TSRole.SUPER_ADMIN
            ]) ||
            EbeguUtil.isNotNullOrUndefined(
                this.model.getFiSiConToWorkWith().finanzielleSituationGS
            ) ||
            this.showZugriffAufSteuerdatenForGemeinde()
        );
    }

    protected showZugriffAufSteuerdatenForGemeinde(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(
                this.model.getFiSiConToWorkWith().finanzielleSituationJA
                    ?.steuerdatenAbfrageStatus
            ) &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGemeindeOrBGOrTSRoles().concat(TSRole.SUPER_ADMIN)
            )
        );
    }

    protected callKiBonAnfrage(
        isGemeinsam: boolean
    ): IPromise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.callKiBonAnfrageAndUpdateFinSit(
            isGemeinsam
        );
    }
}
