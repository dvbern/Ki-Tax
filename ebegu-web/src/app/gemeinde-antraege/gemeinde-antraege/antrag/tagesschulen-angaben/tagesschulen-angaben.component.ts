/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, ViewEncapsulation} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSLastenausgleichTagesschuleAngabenInstitutionStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenInstitutionStatus';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {TSGesuchsperiode} from '../../../../../models/TSGesuchsperiode';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {HTTP_ERROR_CODES} from '../../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../../lastenausgleich-ts/services/lastenausgleich-ts.service';
import {TagesschuleAngabenRS} from '../../../lastenausgleich-ts/services/tagesschule-angaben.service.rest';

@Component({
    selector: 'dv-tagesschulen-angaben',
    templateUrl: './tagesschulen-angaben.component.html',
    styleUrls: ['./tagesschulen-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
})
export class TagesschulenAngabenComponent {

    @Input() public lastenausgleichID: string;
    @Input() public institutionContainerId: string;

    public form: FormGroup;

    private subscription: Subscription;
    public latsAngabenInstitutionContainer: TSLastenausgleichTagesschuleAngabenInstitutionContainer;
    public angabenAusKibon: boolean;
    public gesuchsPeriode: TSGesuchsperiode;
    public formFreigebenTriggered: boolean = false;

    private gemeindeAntragContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly tagesschulenAngabenRS: TagesschuleAngabenRS,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly authService: AuthServiceRS,
        private readonly dialog: MatDialog,
        private readonly $state: StateService,
        private readonly routerGlobals: UIRouterGlobals,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer().subscribe(container => {
            this.gemeindeAntragContainer = container;
            this.latsAngabenInstitutionContainer =
                container.angabenInstitutionContainers?.find(institutionContainer => {
                    return institutionContainer.id === this.institutionContainerId;
                });
            this.gesuchsPeriode = container.gesuchsperiode;
            const angaben = this.latsAngabenInstitutionContainer?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN ?
                this.latsAngabenInstitutionContainer?.angabenDeklaration :
                this.latsAngabenInstitutionContainer?.angabenKorrektur;
            this.form = this.setupForm(angaben);
            if (container.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU || !this.canEditForm()) {
                this.form.disable();
            }
            this.setupCalculation(angaben);
            this.angabenAusKibon = container.alleAngabenInKibonErfasst;
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR'));
        });
    }

    private canEditForm(): boolean {
        return !this.authService.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles()) && (
            (this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) &&
                this.latsAngabenInstitutionContainer.status !== TSLastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getInstitutionRoles()) &&
                this.latsAngabenInstitutionContainer.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN)
        );
    }

    private setupForm(latsAngabenInstiution: TSLastenausgleichTagesschuleAngabenInstitution): FormGroup {
        const form = this.fb.group({
            // A
            isLehrbetrieb: latsAngabenInstiution?.isLehrbetrieb,
            // B
            anzahlEingeschriebeneKinder: latsAngabenInstiution?.anzahlEingeschriebeneKinder,
            anzahlEingeschriebeneKinderKindergarten: latsAngabenInstiution?.anzahlEingeschriebeneKinderKindergarten,
            anzahlEingeschriebeneKinderBasisstufe: latsAngabenInstiution?.anzahlEingeschriebeneKinderBasisstufe,
            anzahlEingeschriebeneKinderPrimarstufe: latsAngabenInstiution?.anzahlEingeschriebeneKinderPrimarstufe,
            anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen: latsAngabenInstiution?.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen,
            durchschnittKinderProTagFruehbetreuung: latsAngabenInstiution?.durchschnittKinderProTagFruehbetreuung,
            durchschnittKinderProTagMittag: latsAngabenInstiution?.durchschnittKinderProTagMittag,
            durchschnittKinderProTagNachmittag1: latsAngabenInstiution?.durchschnittKinderProTagNachmittag1,
            durchschnittKinderProTagNachmittag2: latsAngabenInstiution?.durchschnittKinderProTagNachmittag2,
            betreuungsstundenEinschliesslichBesondereBeduerfnisse:
            latsAngabenInstiution?.betreuungsstundenEinschliesslichBesondereBeduerfnisse,
            // C
            schuleAufBasisOrganisatorischesKonzept: latsAngabenInstiution?.schuleAufBasisOrganisatorischesKonzept,
            schuleAufBasisPaedagogischesKonzept: latsAngabenInstiution?.schuleAufBasisPaedagogischesKonzept,
            raeumlicheVoraussetzungenEingehalten: latsAngabenInstiution?.raeumlicheVoraussetzungenEingehalten,
            betreuungsverhaeltnisEingehalten: latsAngabenInstiution?.betreuungsverhaeltnisEingehalten,
            ernaehrungsGrundsaetzeEingehalten: latsAngabenInstiution?.ernaehrungsGrundsaetzeEingehalten,
            // Bemerkungen
            bemerkungen: latsAngabenInstiution?.bemerkungen,
            // Calculations
            anzahlEingeschriebeneKinderSekundarstufe: '',
        });
        form.get('anzahlEingeschriebeneKinderSekundarstufe').disable();

        return form;
    }

    private setupCalculation(angaben: TSLastenausgleichTagesschuleAngabenInstitution): void {
        combineLatest(
            [
                this.form.get('anzahlEingeschriebeneKinder')
                    .valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinder || 0)),
                this.form.get('anzahlEingeschriebeneKinderKindergarten')
                    .valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderKindergarten || 0)),
                this.form.get('anzahlEingeschriebeneKinderBasisstufe')
                    .valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderBasisstufe || 0)),
                this.form.get('anzahlEingeschriebeneKinderPrimarstufe')
                    .valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderPrimarstufe || 0)),
            ],
        ).subscribe(values => {
            this.form.get('anzahlEingeschriebeneKinderSekundarstufe')
                .setValue(values[0] - values[1] - values[2] - values[3]);
        }, () => {
            this.errorService.addMesageAsError('BAD_NUMBER_ERROR');
        });
    }

    public onFormSubmit(): void {
        if (this.latsAngabenInstitutionContainer.isAtLeastInBearbeitungGemeinde()) {
            this.latsAngabenInstitutionContainer.angabenKorrektur = this.form.value;
        } else {
            this.latsAngabenInstitutionContainer.angabenDeklaration = this.form.value;
        }

        this.tagesschulenAngabenRS.saveTagesschuleAngaben(this.latsAngabenInstitutionContainer).subscribe(result => {
            this.setupForm(result?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN ?
                result?.angabenDeklaration : result?.angabenKorrektur);
            this.errorService.addMesageAsInfo(this.translate.instant('SAVED'));
        }, error => {
            if (error.status === HTTP_ERROR_CODES.BAD_REQUEST) {
                this.errorService.addMesageAsError(this.translate.instant('ERROR_NUMBER'));
            }
        });
    }

    public async onFreigeben(): Promise<void> {
        this.formFreigebenTriggered = true;
        this.enableFormValidation();

        if (!this.form.valid) {
            this.errorService.addMesageAsError(
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
            );
            return;
        }
        if (!await this.confirmDialog('LATS_FRAGE_INSTITUTION_FORMULAR_FREIGEBEN')) {
            return;
        }
        this.latsAngabenInstitutionContainer.angabenDeklaration = this.form.value;

        this.tagesschulenAngabenRS.tagesschuleAngabenFreigeben(this.latsAngabenInstitutionContainer)
            .subscribe(freigegeben => {
                this.latsAngabenInstitutionContainer = freigegeben;
                this.lastenausgleichTSService.updateLATSAngabenGemeindeContainerStore(this.routerGlobals.params.id);
                if (!this.canEditForm()) {
                    this.form.disable();
                }
                this.cd.markForCheck();
            }, () => {
                this.errorService.addMesageAsError(this.translate.instant('ERROR_SAVE'));
            });
    }

    private confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant(frageKey),
        };
        return this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
    }

    public async onGeprueft(): Promise<void> {
        this.formFreigebenTriggered = true;
        this.enableFormValidation();

        if (!this.form.valid) {
            this.errorService.addMesageAsError(
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
            );
            return;
        }

        if (!await this.confirmDialog('LATS_FRAGE_INSTITUTION_FORMULAR_GEPRUEFT')) {
            return;
        }

        this.latsAngabenInstitutionContainer.angabenKorrektur = this.form.value;

        this.tagesschulenAngabenRS.tagesschuleAngabenGeprueft(this.latsAngabenInstitutionContainer)
            .subscribe(geprueft => {
                this.latsAngabenInstitutionContainer = geprueft;
                this.lastenausgleichTSService.updateLATSAngabenGemeindeContainerStore(this.routerGlobals.params.id);
                this.form.disable();
                this.cd.markForCheck();
                this.navigateBack();
            }, () => {
                this.errorService.addMesageAsError(this.translate.instant('ERROR_SAVE'));
            });
    }

    private enableFormValidation(): void {
        // A
        this.form.get('isLehrbetrieb').setValidators([Validators.required]);
        // B
        if (!this.angabenAusKibon) {
            this.form.get('anzahlEingeschriebeneKinder')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('anzahlEingeschriebeneKinderKindergarten')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('anzahlEingeschriebeneKinderBasisstufe')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('anzahlEingeschriebeneKinderPrimarstufe')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('durchschnittKinderProTagFruehbetreuung')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('durchschnittKinderProTagMittag')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('durchschnittKinderProTagNachmittag1')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('durchschnittKinderProTagNachmittag2')
                .setValidators([Validators.required, this.numberValidator()]);
            this.form.get('betreuungsstundenEinschliesslichBesondereBeduerfnisse')
                .setValidators([Validators.required, this.numberValidator()]);
        }
        this.form.get('anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen')
            .setValidators([Validators.required, this.numberValidator()]);
        // C
        this.form.get('schuleAufBasisOrganisatorischesKonzept').setValidators([Validators.required]);
        this.form.get('schuleAufBasisPaedagogischesKonzept').setValidators([Validators.required]);
        this.form.get('raeumlicheVoraussetzungenEingehalten').setValidators([Validators.required]);
        this.form.get('betreuungsverhaeltnisEingehalten').setValidators([Validators.required]);
        this.form.get('ernaehrungsGrundsaetzeEingehalten').setValidators([Validators.required]);

        this.triggerFormValidation();
    }

    private numberValidator(): ValidatorFn {
        // tslint:disable-next-line:no-unnecessary-type-annotation
        return (control: AbstractControl): {} | null => {
            return isNaN(control.value) ? {
                noNumberError: control.value,
            } : null;
        };
    }

    private triggerFormValidation(): void {
        for (const key in this.form.controls) {
            if (this.form.get(key) !== null) {
                this.form.get(key).markAsTouched();
                this.form.get(key).updateValueAndValidity();
            }
        }
        this.form.updateValueAndValidity();
    }

    public actionButtonsDisabled(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles()) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) &&
                this.latsAngabenInstitutionContainer.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) &&
                this.latsAngabenInstitutionContainer.status !== TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);
    }

    public canSeeFreigebenButton(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) && !this.latsAngabenInstitutionContainer.isAtLeastInBearbeitungGemeinde());
    }

    public canSeeGeprueftButton(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) && this.latsAngabenInstitutionContainer.isAtLeastInBearbeitungGemeinde();
    }

    public canSeeSaveButton(): boolean {
        return !this.authService.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles());
    }

    public onFalscheAngaben(): void {
        if (!this.gemeindeAntragContainer?.angabenDeklaration?.isInBearbeitung()) {
            this.errorService.addMesageAsError(this.translate.instant('LATS_FA_INSTI_NUR_WENN_GEMEINDE_OFFEN'));
            return;
        }
        this.tagesschulenAngabenRS.falscheAngaben(this.latsAngabenInstitutionContainer).subscribe(container => {
            this.latsAngabenInstitutionContainer = container;
            this.form = this.setupForm(container.angabenKorrektur);
            this.setupCalculation(container.angabenKorrektur);
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_SAVE'));
        });
    }

    public falscheAngabenVisible(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles().concat(TSRoleUtil.getInstitutionRoles())) &&
            this.gemeindeAntragContainer?.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE &&
            this.latsAngabenInstitutionContainer?.status ===
            TSLastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT;
    }

    public navigateBack($event?: MouseEvent): void {
        const parentState = 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN.LIST';
        if ($event && $event.ctrlKey) {
            const url = this.$state.href(parentState);
            window.open(url, '_blank');
        } else {
            this.$state.go(parentState);
        }
    }
}
