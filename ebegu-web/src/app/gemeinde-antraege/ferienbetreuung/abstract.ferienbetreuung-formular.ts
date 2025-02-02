/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {ChangeDetectorRef} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {Moment} from 'moment';
import {BehaviorSubject} from 'rxjs';
import {TSWizardStepXTyp} from '../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAbstractAngaben} from '../../../models/gemeindeantrag/TSFerienbetreuungAbstractAngaben';
import {TSFerienbetreuungAngabenContainer} from '../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {WizardStepXRS} from '../../core/service/wizardStepXRS.rest';

export abstract class AbstractFerienbetreuungFormular {
    public form: FormGroup;
    public formValidationTriggered = false;
    public formAbschliessenTriggered = false;

    public container: TSFerienbetreuungAngabenContainer;

    private readonly WIZARD_TYPE = TSWizardStepXTyp.FERIENBETREUUNG;

    public readonly canSeeSave: BehaviorSubject<boolean> =
        new BehaviorSubject<boolean>(false);
    public readonly canSeeAbschliessen: BehaviorSubject<boolean> =
        new BehaviorSubject<boolean>(false);
    public readonly canSeeFalscheAngaben: BehaviorSubject<boolean> =
        new BehaviorSubject<boolean>(false);

    protected constructor(
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        protected readonly cd: ChangeDetectorRef,
        protected readonly wizardRS: WizardStepXRS,
        protected readonly uiRouterGlobals: UIRouterGlobals
    ) {}

    protected abstract enableFormValidation(): void;

    protected abstract setupForm(
        angabe: TSFerienbetreuungAbstractAngaben
    ): void;

    protected abstract setBasicValidation(): void;

    public addOneMonthToMoment(moment: Moment): string {
        const momentClone = moment?.clone();
        return momentClone?.add(1, 'M').format('DD.MM.YYYY');
    }

    protected removeAllValidators(): void {
        for (const key in this.form.controls) {
            if (this.form.get(key) === null) {
                continue;
            }
            this.form.controls[key].clearValidators();
        }
        this.triggerFormValidation();
    }

    protected enableAndTriggerFormValidation(): void {
        this.enableFormValidation();
        this.triggerFormValidation();
    }

    protected triggerFormValidation(): void {
        this.formValidationTriggered = true;
        for (const key in this.form.controls) {
            if (this.form.get(key) !== null) {
                this.form.get(key).markAsTouched();
                this.form.get(key).updateValueAndValidity();
            }
        }
        this.form.markAsTouched();
        this.form.updateValueAndValidity();
    }

    protected showValidierungFehlgeschlagenErrorMessage(): void {
        this.errorService.clearAll();
        this.errorService.addMesageAsError(
            this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN')
        );
    }

    protected showUnerwarteteErrorMessage(): void {
        this.errorService.clearAll();
        this.errorService.addMesageAsError(
            this.translate.instant('ERROR_UNEXPECTED')
        );
    }

    protected confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant(frageKey)
        };
        return this.dialog
            .open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
    }

    protected setupRoleBasedPropertiesForPrincipal(
        container: TSFerienbetreuungAngabenContainer,
        angaben: TSFerienbetreuungAbstractAngaben,
        principal: TSBenutzer
    ): void {
        if (container.isInPruefungKanton()) {
            if (
                principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()) &&
                angaben.isInBearbeitung()
            ) {
                this.canSeeSave.next(true);
                this.canSeeAbschliessen.next(true);
                this.canSeeFalscheAngaben.next(false);
            } else if (
                principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()) &&
                angaben.isAbgeschlossen()
            ) {
                this.canSeeSave.next(false);
                this.canSeeAbschliessen.next(false);
                this.canSeeFalscheAngaben.next(true);
            } else {
                this.setCanSeeNoActions();
            }
        } else if (
            container.isInBearbeitungGemeinde() &&
            !principal.hasOneOfRoles(TSRoleUtil.getMandantOnlyRoles())
        ) {
            if (angaben.isAbgeschlossen()) {
                this.canSeeAbschliessen.next(false);
                this.canSeeSave.next(false);
                this.canSeeFalscheAngaben.next(true);
            } else {
                this.canSeeAbschliessen.next(true);
                this.canSeeSave.next(true);
                this.canSeeFalscheAngaben.next(false);
            }
        } else {
            this.setCanSeeNoActions();
        }
    }

    private setCanSeeNoActions(): void {
        this.canSeeAbschliessen.next(false);
        this.canSeeSave.next(false);
        this.canSeeFalscheAngaben.next(false);
    }

    protected disableFormBasedOnStateAndPrincipal(
        principal: TSBenutzer,
        container: TSFerienbetreuungAngabenContainer,
        angaben: TSFerienbetreuungAbstractAngaben
    ): void {
        if (
            angaben.isAbgeschlossen() ||
            container?.isGeprueft() ||
            (container?.isInBearbeitungGemeinde() &&
                principal.hasOneOfRoles(TSRoleUtil.getMandantOnlyRoles())) ||
            (container?.isInPruefungKanton() &&
                principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrFBOnlyRoles()))
        ) {
            this.form.disable();
        } else {
            this.form.enable();
        }
    }

    protected setupFormAndPermissions(
        container: TSFerienbetreuungAngabenContainer,
        angaben: TSFerienbetreuungAbstractAngaben,
        principal: TSBenutzer
    ): void {
        this.setupForm(angaben);
        this.cd.detectChanges();

        this.disableFormBasedOnStateAndPrincipal(principal, container, angaben);
        this.setupRoleBasedPropertiesForPrincipal(
            container,
            angaben,
            principal
        );

        this.cd.markForCheck();
    }

    protected handleSaveSuccess(): void {
        this.formAbschliessenTriggered = false;
        this.form.markAsPristine();
        this.errorService.clearAll();
        this.wizardRS.updateSteps(
            this.WIZARD_TYPE,
            this.uiRouterGlobals.params.id
        );
    }

    protected handleSaveErrors(errors: TSExceptionReport[]): void {
        this.errorService.clearAll();
        if (
            errors.find(
                error =>
                    EbeguUtil.isNotNullOrUndefined(error.customMessage) &&
                    error.customMessage.includes(
                        'Not all required properties are set'
                    )
            )
        ) {
            this.enableAndTriggerFormValidation();
            this.showValidierungFehlgeschlagenErrorMessage();
        } else {
            this.showUnerwarteteErrorMessage();
        }
    }

    protected async checkReadyForAbschliessen(): Promise<boolean> {
        this.formAbschliessenTriggered = true;
        this.enableAndTriggerFormValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return false;
        }
        return this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN');
    }
}
