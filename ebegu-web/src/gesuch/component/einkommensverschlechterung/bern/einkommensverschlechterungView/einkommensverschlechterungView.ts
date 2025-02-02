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

import {IComponentOptions, ILogService, IPromise, IQService} from 'angular';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {IEinkommensverschlechterungStateParams} from '../../../../gesuch.route';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class EinkommensverschlechterungViewComponentConfig
    implements IComponentOptions
{
    public transclude = false;
    public template = require('./einkommensverschlechterungView.html');
    public controller = EinkommensverschlechterungViewController;
    public controllerAs = 'vm';
}

export class EinkommensverschlechterungViewController extends AbstractGesuchViewController<TSFinanzModel> {
    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        '$log',
        'WizardStepManager',
        '$q',
        '$scope',
        '$translate',
        '$timeout',
        'EinstellungRS'
    ];

    public showSelbstaendig: boolean;
    public showSelbstaendigGS: boolean;
    public geschaeftsgewinnBasisjahrMinus1: number;
    public geschaeftsgewinnBasisjahrMinus2: number;
    public geschaeftsgewinnBasisjahrMinus1GS: number;
    public ersatzeinkommenSelbststaendigkeitActivated: boolean;
    public ersatzeinkommenSelbststaendigkeitBasisjahrMinus1: number;
    public ersatzeinkommenSelbststaendigkeitBasisjahrMinus2: number;
    public ersatzeinkommenSelbststaendigkeitBasisjahrMinus1GS: number;
    public showErsatzeinkommenSelbststaendigkeit: boolean;
    public showErsatzeinkommenSelbststaendigkeitGS: boolean;
    public allowedRoles: ReadonlyArray<TSRole>;
    public initialModel: TSFinanzModel;

    public constructor(
        $stateParams: IEinkommensverschlechterungStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly $log: ILogService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        $scope: IScope,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService,
        private readonly einstellungRS: EinstellungRS
    ) {
        super(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            $timeout
        );
        const parsedGesuchstelllerNum = parseInt(
            $stateParams.gesuchstellerNumber,
            10
        );
        const parsedBasisJahrPlusNum = parseInt($stateParams.basisjahrPlus, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedGesuchstelllerNum);
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.model = new TSFinanzModel(
            this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parsedGesuchstelllerNum,
            parsedBasisJahrPlusNum
        );
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(
            this.gesuchModelManager.getGesuch()
        );
        this.model.initEinkommensverschlechterungContainer(
            parsedBasisJahrPlusNum,
            parsedGesuchstelllerNum
        );
        this.initialModel = angular.copy(this.model);
        this.allowedRoles =
            this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.initViewModel();
        this.calculate();
    }

    private initViewModel(): void {
        this.initGeschaeftsgewinnUndErsatzeinkommensFromFS();
        this.initEinstellungen();
        const fiSiConToWorkWith = this.model.getFiSiConToWorkWith();

        this.showSelbstaendig =
            fiSiConToWorkWith.finanzielleSituationJA.isSelbstaendig() ||
            EbeguUtil.isNotNullOrUndefined(
                this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr
            );

        this.showErsatzeinkommenSelbststaendigkeit =
            fiSiConToWorkWith.finanzielleSituationJA.hasErsatzeinkommenSelbststaendigkeit() ||
            EbeguUtil.isNotNullOrUndefined(
                this.model.getEkvToWorkWith()
                    .ersatzeinkommenSelbststaendigkeitBasisjahr
            );

        if (
            !fiSiConToWorkWith.finanzielleSituationGS ||
            !this.model.getEkvToWorkWith_GS()
        ) {
            return;
        }

        this.showSelbstaendigGS =
            fiSiConToWorkWith.finanzielleSituationGS.isSelbstaendig() ||
            EbeguUtil.isNotNullOrUndefined(
                this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr
            );

        this.showErsatzeinkommenSelbststaendigkeitGS =
            fiSiConToWorkWith.finanzielleSituationGS.isSelbstaendig() ||
            EbeguUtil.isNotNullOrUndefined(
                this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr
            );
    }

    private initEinstellungen(): Promise<void> {
        return this.einstellungRS
            .getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode().id
            )
            .toPromise()
            .then(einstellungen => {
                const showErsatzeinkommen = einstellungen.find(
                    einstellung =>
                        einstellung.key ===
                        TSEinstellungKey.ZUSATZLICHE_FELDER_ERSATZEINKOMMEN
                );
                if (showErsatzeinkommen === undefined) {
                    this.ersatzeinkommenSelbststaendigkeitActivated = false;
                    return;
                }
                this.ersatzeinkommenSelbststaendigkeitActivated =
                    showErsatzeinkommen.getValueAsBoolean();
            });
    }

    public showSelbstaendigClicked(): void {
        if (!this.showSelbstaendig) {
            this.resetSelbstaendigFields();
        }
    }

    private resetSelbstaendigFields(): void {
        if (this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr) {
            this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr = undefined;
            this.calculate();
        }
    }

    public showErsatzeinkommenSelbststaendigkeitClicked(): void {
        if (!this.showErsatzeinkommenSelbststaendigkeit) {
            this.resetErsatzeinkommenSelbststaendigkeitFields();
        } else if (this.model.getEkvToWorkWith().ersatzeinkommen === 0) {
            this.model.getEkvToWorkWith().ersatzeinkommenSelbststaendigkeitBasisjahr = 0;
        }
    }

    private resetErsatzeinkommenSelbststaendigkeitFields(): void {
        this.model.getEkvToWorkWith().ersatzeinkommenSelbststaendigkeitBasisjahr =
            undefined;
        if (
            !this.model.einkommensverschlechterungInfoContainer
                .einkommensverschlechterungInfoJA.ekvFuerBasisJahrPlus1 &&
            this.model.getBasisJahrPlus() === 2
        ) {
            this.model.getEkvToWorkWith().ersatzeinkommenSelbststaendigkeitBasisjahr =
                undefined;
        }
        this.calculate();
    }

    public hasGeschaeftsgewinn(): boolean {
        return EbeguUtil.isNotNullOrUndefined(
            this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr
        );
    }

    public save(): IPromise<TSEinkommensverschlechterungContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }
        if (
            !this.isAtLeastOneErsatzeinkommenSelbststaendigkeitProvided() ||
            !this.isErsatzeinkommenValid()
        ) {
            this.scrollToErsatzeinkommenSelbststaendigkeit();
            return undefined;
        }

        if (!this.form.$dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.model.getEkvContToWorkWith());
        }
        this.errorService.clearAll();
        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveEinkommensverschlechterungContainer();
    }

    public calculate(): void {
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(
            this.model,
            this.model.getBasisJahrPlus()
        );
    }

    public getEinkommensverschlechterung(): TSEinkommensverschlechterung {
        return this.model.getEkvToWorkWith();
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.getEinkommensverschlechterungResultate(
            this.model.getBasisJahrPlus()
        );
    }

    public initGeschaeftsgewinnUndErsatzeinkommensFromFS(): void {
        if (
            !this.model.getFiSiConToWorkWith() ||
            !this.model.getFiSiConToWorkWith().finanzielleSituationJA
        ) {
            this.$log.error('Fehler: FinSit muss existieren');
            return;
        }

        const fs = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        const fsGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS;
        if (this.model.getBasisJahrPlus() === 2) {
            // basisjahr Plus 2
            const infoContainer =
                this.model.einkommensverschlechterungInfoContainer;
            if (
                infoContainer.einkommensverschlechterungInfoJA
                    .ekvFuerBasisJahrPlus1
            ) {
                const einkommensverschlJABasisjahrPlus1 =
                    this.model.getEkvContToWorkWith().ekvJABasisJahrPlus1;
                this.geschaeftsgewinnBasisjahrMinus1 =
                    einkommensverschlJABasisjahrPlus1?.geschaeftsgewinnBasisjahr;
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 =
                    einkommensverschlJABasisjahrPlus1?.ersatzeinkommenSelbststaendigkeitBasisjahr;
                const einkommensverschlGSBasisjahrPlus1 =
                    this.model.getEkvContToWorkWith().ekvGSBasisJahrPlus1;
                this.geschaeftsgewinnBasisjahrMinus1GS =
                    einkommensverschlGSBasisjahrPlus1?.geschaeftsgewinnBasisjahr;
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1GS =
                    einkommensverschlGSBasisjahrPlus1?.ersatzeinkommenSelbststaendigkeitBasisjahr;
            } else {
                const einkommensverschlGS = this.model.getEkvToWorkWith_GS();
                this.geschaeftsgewinnBasisjahrMinus1GS =
                    einkommensverschlGS?.geschaeftsgewinnBasisjahrMinus1;
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1GS =
                    einkommensverschlGS?.ersatzeinkommenSelbststaendigkeitBasisjahr;
            }

            this.geschaeftsgewinnBasisjahrMinus2 = fs.geschaeftsgewinnBasisjahr;
            this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2 =
                fs.ersatzeinkommenSelbststaendigkeitBasisjahr;
        } else {
            this.geschaeftsgewinnBasisjahrMinus1 = fs.geschaeftsgewinnBasisjahr;
            this.geschaeftsgewinnBasisjahrMinus2 =
                fs.geschaeftsgewinnBasisjahrMinus1;
            this.geschaeftsgewinnBasisjahrMinus1GS =
                fsGS?.geschaeftsgewinnBasisjahr;

            this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 =
                fs.ersatzeinkommenSelbststaendigkeitBasisjahr;
            this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2 =
                fs.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1;
            this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1GS =
                fsGS?.ersatzeinkommenSelbststaendigkeitBasisjahr;
        }
    }

    public enableBasisjahrMinus1(): boolean {
        const info =
            this.model.einkommensverschlechterungInfoContainer
                .einkommensverschlechterungInfoJA;

        return (
            this.model.getBasisJahrPlus() === 2 && !info.ekvFuerBasisJahrPlus1
        );
    }

    public getTextSelbstaendigKorrektur(): any {
        if (this.showSelbstaendigGS && this.model.getEkvToWorkWith_GS()) {
            const gew1 =
                this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr;
            if (gew1) {
                const basisjahr = this.gesuchModelManager.getBasisjahrPlus(
                    this.model.getBasisJahrPlus()
                );
                return this.$translate.instant(
                    'JA_KORREKTUR_SELBSTAENDIG_EKV',
                    {basisjahr, gewinn1: gew1}
                );
            }
        }
        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }

    public einkommenInVereinfachtemVerfarenClicked(): void {
        this.model.getEkvToWorkWith().amountEinkommenInVereinfachtemVerfahrenAbgerechnet =
            null;
    }

    public ersatzeinkommenChanged(): void {
        if (
            this.model.getEkvToWorkWith().ersatzeinkommen === 0 &&
            EbeguUtil.isNotNullOrUndefined(
                this.model.getEkvToWorkWith()
                    .ersatzeinkommenSelbststaendigkeitBasisjahr
            )
        ) {
            this.model.getEkvToWorkWith().ersatzeinkommenSelbststaendigkeitBasisjahr = 0;
        }
    }

    public isAtLeastOneErsatzeinkommenSelbststaendigkeitProvided(): boolean {
        const ekv = this.model.getEkvToWorkWith();
        return (
            !this.showErsatzeinkommenSelbststaendigkeit ||
            (EbeguUtil.isNotNullOrUndefined(
                ekv.ersatzeinkommenSelbststaendigkeitBasisjahr
            ) &&
                ekv.ersatzeinkommenSelbststaendigkeitBasisjahr > 0) ||
            (EbeguUtil.isNotNullOrUndefined(
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1
            ) &&
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 > 0) ||
            (EbeguUtil.isNotNullOrUndefined(
                ekv.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1
            ) &&
                ekv.ersatzeinkommenSelbststaendigkeitBasisjahrMinus1 > 0) ||
            (EbeguUtil.isNotNullOrUndefined(
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2
            ) &&
                this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2 > 0)
        );
    }

    public isErsatzeinkommenValid(): boolean {
        const ekv: TSEinkommensverschlechterung = this.model.getEkvToWorkWith();
        return (
            (EbeguUtil.isNullOrUndefined(ekv.ersatzeinkommen) &&
                EbeguUtil.isNullOrUndefined(
                    ekv.ersatzeinkommenSelbststaendigkeitBasisjahr
                )) ||
            (EbeguUtil.isNotNullOrUndefined(ekv.ersatzeinkommen) &&
                EbeguUtil.isNullOrUndefined(
                    ekv.ersatzeinkommenSelbststaendigkeitBasisjahr
                )) ||
            ekv.ersatzeinkommen -
                ekv.ersatzeinkommenSelbststaendigkeitBasisjahr >=
                0
        );
    }

    private scrollToErsatzeinkommenSelbststaendigkeit(): void {
        const tmp = document.getElementById(
            'ersatzeinkommen-selbststaendigkeit-container'
        );
        if (tmp) {
            tmp.scrollIntoView();
        }
    }
}
