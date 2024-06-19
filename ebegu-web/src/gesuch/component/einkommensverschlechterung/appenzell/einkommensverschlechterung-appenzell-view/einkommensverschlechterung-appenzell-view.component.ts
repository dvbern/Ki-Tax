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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../../../../../models/TSFinSitZusatzangabenAppenzell';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {EinkommensverschlechterungContainerRS} from '../../../../service/einkommensverschlechterungContainerRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationAppenzellService} from '../../../finanzielleSituation/appenzell/finanzielle-situation-appenzell.service';

@Component({
    selector: 'dv-einkommensverschlechterung-appenzell-view',
    templateUrl: './einkommensverschlechterung-appenzell-view.component.html',
    styleUrls: ['./einkommensverschlechterung-appenzell-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EinkommensverschlechterungAppenzellViewComponent extends AbstractGesuchViewX<TSFinanzModel> {
    public readOnly: boolean = false;

    private readonly gesuchstellerNumber: number;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected berechnungsManager: BerechnungsManager,
        private readonly $transition$: Transition,
        protected ref: ChangeDetectorRef,
        private readonly finSitAppenzellService: FinanzielleSituationAppenzellService,
        private readonly ekvContainerRS: EinkommensverschlechterungContainerRS,
        private readonly translate: TranslateService
    ) {
        super(
            gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL
        );
        const parsedGesuchstelllerNum = parseInt(
            this.$transition$.params().gesuchstellerNumber,
            10
        );
        const parsedBasisJahrPlusNum = parseInt(
            this.$transition$.params().basisjahrPlus,
            10
        );
        this.gesuchstellerNumber = parsedGesuchstelllerNum;
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
        this.readOnly = this.gesuchModelManager.isGesuchReadonly();
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL,
            TSWizardStepStatus.IN_BEARBEITUNG
        );

        if (
            EbeguUtil.isNullOrUndefined(
                this.getEkvToWorkWith().finSitZusatzangabenAppenzell
            )
        ) {
            this.getEkvToWorkWith().finSitZusatzangabenAppenzell =
                new TSFinSitZusatzangabenAppenzell();
        }

        if (
            this.isSpezialFallAR() &&
            EbeguUtil.isNullOrUndefined(
                this.getEkvToWorkWith().finSitZusatzangabenAppenzell
                    .zusatzangabenPartner
            )
        ) {
            this.getEkvToWorkWith().finSitZusatzangabenAppenzell.zusatzangabenPartner =
                new TSFinSitZusatzangabenAppenzell();
        }
        this.calculateResults();
    }

    public getZusatzangabenAppenzellToWorkWith(): TSFinSitZusatzangabenAppenzell {
        const finSitZusatzangabenAppenzell =
            this.getEkvToWorkWith().finSitZusatzangabenAppenzell;
        if (this.isSpezialFallAR() && this.gesuchstellerNumber === 2) {
            return finSitZusatzangabenAppenzell.zusatzangabenPartner;
        }
        return finSitZusatzangabenAppenzell;
    }

    public getEkvToWorkWith(): TSEinkommensverschlechterung {
        if (this.isSpezialFallAR() && this.gesuchstellerNumber === 2) {
            return this.model.einkommensverschlechterungContainerGS1.getJA(
                this.model.getBasisJahrPlus()
            );
        }
        return this.model.getEkvToWorkWith();
    }

    public getZusatzangabenAppenzellGSToWorkWith(): TSFinSitZusatzangabenAppenzell {
        const einkommensverschlechterung = this.getEkvGSToWorkWith();
        if (EbeguUtil.isNullOrUndefined(einkommensverschlechterung)) {
            return null;
        }
        const finSitZusatzangabenAppenzell =
            einkommensverschlechterung.finSitZusatzangabenAppenzell;
        if (this.isSpezialFallAR() && this.gesuchstellerNumber === 2) {
            return finSitZusatzangabenAppenzell.zusatzangabenPartner;
        }
        return finSitZusatzangabenAppenzell;
    }

    private getEkvGSToWorkWith(): TSEinkommensverschlechterung {
        if (this.isSpezialFallAR() && this.gesuchstellerNumber === 2) {
            return this.model.einkommensverschlechterungContainerGS1.getGS(
                this.model.getBasisJahrPlus()
            );
        }
        return this.model.getEkvToWorkWith_GS();
    }

    public save(
        onResult: (arg: any) => void
    ): IPromise<TSEinkommensverschlechterungContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }

        if (!this.form.dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            onResult(this.model.getEkvContToWorkWith());
            return Promise.resolve(this.model.getEkvContToWorkWith());
        }
        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.ekvContainerRS
            .saveEinkommensverschlechterungContainer(
                this.getStammdatenToWorkWith()
                    .einkommensverschlechterungContainer,
                this.getStammdatenToWorkWith().id,
                this.getGesuch().id
            )
            .then(ekv => {
                onResult(ekv);
                return ekv;
            });
    }

    public getAntragstellerNumber(): number {
        return this.gesuchstellerNumber;
    }

    public isGemeinsam(): boolean {
        return this.model.familienSituation.gemeinsameSteuererklaerung;
    }

    public calculateResults(): void {
        this.finSitAppenzellService.calculateEinkommensverschlechterung(
            this.model,
            this.model.getBasisJahrPlus()
        );
    }

    public getGesuchstellerName() {
        if (this.isGemeinsam()) {
            return `${this.gesuchModelManager
                .getStammdatenToWorkWith()
                .extractFullName()} + ${this.extractFullNameGS2()}`;
        }
        return this.gesuchstellerNumber === 2
            ? this.extractFullNameGS2()
            : this.gesuchModelManager
                  .getStammdatenToWorkWith()
                  .extractFullName();
    }

    public extractFullNameGS2(): string {
        if (this.isSpezialFallAR()) {
            return this.translate.instant('GS2_VERHEIRATET');
        }
        return this.getGesuch() && this.getGesuch().gesuchsteller2
            ? this.getGesuch().gesuchsteller2.extractFullName()
            : '';
    }

    private getStammdatenToWorkWith(): TSGesuchstellerContainer {
        return this.gesuchstellerNumber === 2 && this.isSpezialFallAR()
            ? this.getGesuch().gesuchsteller1
            : this.gesuchModelManager.getStammdatenToWorkWith();
    }
}
