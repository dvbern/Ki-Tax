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
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import * as moment from 'moment';
import {BehaviorSubject, combineLatest, Subject, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSLastenausgleichTagesschuleAngabenInstitutionStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenInstitutionStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSWizardStepXTyp} from '../../../../../models/enums/TSWizardStepXTyp';
import {TSAnzahlEingeschriebeneKinder} from '../../../../../models/gemeindeantrag/TSAnzahlEingeschriebeneKinder';
import {TSDurchschnittKinderProTag} from '../../../../../models/gemeindeantrag/TSDurchschnittKinderProTag';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSGesuchsperiode} from '../../../../../models/TSGesuchsperiode';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {CONSTANTS, HTTP_ERROR_CODES} from '../../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../../shared/validators/number-validator.directive';
import {UnsavedChangesService} from '../../../services/unsaved-changes.service';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';
import {TagesschuleAngabenRS} from '../../services/tagesschule-angaben.service.rest';

const LOG = LogFactory.createLog('TagesschulenAngabenComponent');

@Component({
    selector: 'dv-tagesschulen-angaben',
    templateUrl: './tagesschulen-angaben.component.html',
    styleUrls: ['./tagesschulen-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
})
export class TagesschulenAngabenComponent {

    @Input() public lastenausgleichID: string;
    @Input() public institutionContainerId: string;

    public form: FormGroup;

    private subscription: Subscription;
    // TODO: refactor this to store
    public latsAngabenInstitutionContainer: TSLastenausgleichTagesschuleAngabenInstitutionContainer;
    public angabenAusKibon: boolean;
    public gesuchsPeriode: TSGesuchsperiode;
    public formFreigebenTriggered: boolean = false;
    public anzahlEingeschriebeneKinder: TSAnzahlEingeschriebeneKinder;
    public durchschnittKinderProTag: TSDurchschnittKinderProTag;
    public abweichungenAnzahlKinder: number;
    public stichtag: Subject<string> = new Subject<string>();
    public gemeindeAntragContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;

    public autoFilled: boolean = false;
    public isInstiUser: boolean = false;

    public readonly canSeeSave: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public readonly canSeeAbschliessen: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public readonly canSeeFreigeben: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public readonly canSeeFalscheAngaben: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public readonly canSeeDurchKibonAusfuellen: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

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
        private readonly settings: EinstellungRS,
        private readonly wizardRS: WizardStepXRS,
        private readonly unsavedChangesService: UnsavedChangesService,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = combineLatest([
            this.lastenausgleichTSService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$,
        ]).subscribe(([container, principal]) => {
            this.gemeindeAntragContainer = container;
            this.latsAngabenInstitutionContainer =
                container.angabenInstitutionContainers?.find(institutionContainer => {
                    return institutionContainer.id === this.institutionContainerId;
                });
            this.gesuchsPeriode = container.gesuchsperiode;
            const angaben = this.latsAngabenInstitutionContainer?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN ?
                this.latsAngabenInstitutionContainer?.angabenDeklaration :
                this.latsAngabenInstitutionContainer?.angabenKorrektur;
            this.angabenAusKibon = container.alleAngabenInKibonErfasst;
            this.form = this.setupForm(angaben);
            if (container.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU || !this.canEditForm()) {
                this.form.disable();
            }
            this.getStichtag();
            this.setupCalculation(angaben);
            if (this.angabenAusKibon && !principal.hasOneOfRoles(TSRoleUtil.getMandantOnlyRoles())) {
                this.queryAnzahlEingeschriebeneKinder();
                this.queryDurchschnittKinderProTag();
            }
            this.setupRoleBasedPropertiesForPrincipal(this.gemeindeAntragContainer,
                this.latsAngabenInstitutionContainer,
                principal);
            this.isInstiUser = principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
            this.angabenAusKibon = container.alleAngabenInKibonErfasst;
            this.unsavedChangesService.registerForm(this.form);
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR'));
        });
    }

    // tslint:disable-next-line:cognitive-complexity
    private setupRoleBasedPropertiesForPrincipal(
        container: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
        angaben: TSLastenausgleichTagesschuleAngabenInstitutionContainer,
        principal: TSBenutzer,
    ): void {
        if (container.isAtLeastInBearbeitungKanton()) {
            this.canSeeDurchKibonAusfuellen.next(false);
            this.canSeeAbschliessen.next(false);
            this.canSeeFalscheAngaben.next(false);
            this.canSeeFreigeben.next(false);
            this.canSeeSave.next(false);
        } else {
            if (angaben.isInBearbeitungInstitution()) {
                this.canSeeDurchKibonAusfuellen.next(true);
                this.canSeeSave.next(true);
                this.canSeeAbschliessen.next(false);
                this.canSeeFreigeben.next(true);
                this.canSeeFalscheAngaben.next(false);
            }
            if (angaben.isInPruefungGemeinde()) {
                if (principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
                    this.canSeeDurchKibonAusfuellen.next(false);
                    this.canSeeSave.next(false);
                    this.canSeeAbschliessen.next(false);
                    this.canSeeFreigeben.next(false);
                    this.canSeeFalscheAngaben.next(true);
                }
                if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
                    this.canSeeDurchKibonAusfuellen.next(true);
                    this.canSeeSave.next(true);
                    this.canSeeAbschliessen.next(true);
                    this.canSeeFreigeben.next(false);
                    if (principal.hasRole(TSRole.SUPER_ADMIN)) {
                        this.canSeeFalscheAngaben.next(true);
                    } else {
                        this.canSeeFalscheAngaben.next(false);
                    }
                }
            }
            if (angaben.isGeprueftGemeinde()) {
                this.canSeeDurchKibonAusfuellen.next(false);
                if (principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
                    this.canSeeSave.next(false);
                    this.canSeeAbschliessen.next(false);
                    this.canSeeFreigeben.next(false);
                    this.canSeeFalscheAngaben.next(false);
                }
                if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
                    this.canSeeSave.next(false);
                    this.canSeeAbschliessen.next(false);
                    this.canSeeFreigeben.next(false);
                    this.canSeeFalscheAngaben.next(true);
                }
            }
        }
    }

    private canEditForm(): boolean {
        return !this.authService.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles()) && (
            (this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) &&
                this.latsAngabenInstitutionContainer?.status !== TSLastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) &&
                this.latsAngabenInstitutionContainer?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN)
        );
    }

    private setupForm(latsAngabenInstiution: TSLastenausgleichTagesschuleAngabenInstitution): FormGroup {
        const form = this.fb.group({
            // A
            isLehrbetrieb: latsAngabenInstiution?.isLehrbetrieb,
            // B
            anzahlEingeschriebeneKinder: [
                latsAngabenInstiution?.anzahlEingeschriebeneKinder,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
            anzahlEingeschriebeneKinderKindergarten: [
                latsAngabenInstiution?.anzahlEingeschriebeneKinderKindergarten,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
            anzahlEingeschriebeneKinderSekundarstufe: [
                latsAngabenInstiution?.anzahlEingeschriebeneKinderSekundarstufe,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
            anzahlEingeschriebeneKinderPrimarstufe: [
                latsAngabenInstiution?.anzahlEingeschriebeneKinderPrimarstufe,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
            anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen: [
                latsAngabenInstiution?.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
            durchschnittKinderProTagFruehbetreuung: [
                latsAngabenInstiution?.durchschnittKinderProTagFruehbetreuung,
                Validators.compose([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]),
            ],
            durchschnittKinderProTagMittag: [
                latsAngabenInstiution?.durchschnittKinderProTagMittag,
                Validators.compose([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]),
            ],
            durchschnittKinderProTagNachmittag1: [
                latsAngabenInstiution?.durchschnittKinderProTagNachmittag1,
                Validators.compose([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]),
            ],
            durchschnittKinderProTagNachmittag2: [
                latsAngabenInstiution?.durchschnittKinderProTagNachmittag2,
                Validators.compose([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]),
            ],
            betreuungsstundenEinschliesslichBesondereBeduerfnisse:
                [
                    latsAngabenInstiution?.betreuungsstundenEinschliesslichBesondereBeduerfnisse,
                    numberValidator(ValidationType.POSITIVE_INTEGER),
                ],
            // C
            schuleAufBasisOrganisatorischesKonzept: latsAngabenInstiution?.schuleAufBasisOrganisatorischesKonzept,
            schuleAufBasisPaedagogischesKonzept: latsAngabenInstiution?.schuleAufBasisPaedagogischesKonzept,
            raeumlicheVoraussetzungenEingehalten: latsAngabenInstiution?.raeumlicheVoraussetzungenEingehalten,
            betreuungsverhaeltnisEingehalten: latsAngabenInstiution?.betreuungsverhaeltnisEingehalten,
            ernaehrungsGrundsaetzeEingehalten: latsAngabenInstiution?.ernaehrungsGrundsaetzeEingehalten,
            // Bemerkungen
            bemerkungen: latsAngabenInstiution?.bemerkungen,
            // hidden fields
            version: latsAngabenInstiution?.version,
        });

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
                this.form.get('anzahlEingeschriebeneKinderPrimarstufe')
                    .valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderPrimarstufe || 0)),
                this.form.get('anzahlEingeschriebeneKinderSekundarstufe')
                    .valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderSekundarstufe || 0)),
            ],
        ).subscribe(values => {
            this.abweichungenAnzahlKinder = values[0] - values[1] - values[2] - values[3];
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError('BAD_NUMBER_ERROR');
        });
    }

    public onFormSubmit(): void {
        this.resetBasicValidation();
        if (!this.form.valid) {
            this.errorService.addMesageAsError(
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
            );
            return;
        }

        if (this.latsAngabenInstitutionContainer.isAtLeastInBearbeitungGemeinde()) {
            this.latsAngabenInstitutionContainer.angabenKorrektur = this.form.value;
        } else {
            this.latsAngabenInstitutionContainer.angabenDeklaration = this.form.value;
        }
        this.errorService.clearAll();
        this.tagesschulenAngabenRS.saveTagesschuleAngaben(this.latsAngabenInstitutionContainer).subscribe(result => {
            this.form = this.setupForm(result?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN ?
                result?.angabenDeklaration : result?.angabenKorrektur);
            this.errorService.addMesageAsInfo(this.translate.instant('SAVED'));
            this.form.markAsPristine();
            this.unsavedChangesService.registerForm(this.form);
        }, error => {
            this.manageSaveErrorCodes(error);
        });
    }

    public async onFreigeben(): Promise<void> {
        this.formFreigebenTriggered = true;
        this.enableFormValidation();
        this.errorService.clearAll();
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
                this.errorService.clearAll();
                this.cd.markForCheck();
                this.form.markAsPristine();
                this.unsavedChangesService.registerForm(this.form);
            }, error => {
                this.manageSaveErrorCodes(error);
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
                this.errorService.clearAll();
                this.cd.markForCheck();
                this.form.markAsPristine();
                this.unsavedChangesService.registerForm(this.form);
                this.navigateBack();
            }, error => {
                this.manageSaveErrorCodes(error);
            });
    }

    private enableFormValidation(): void {
        // A
        this.form.get('isLehrbetrieb').setValidators([Validators.required]);
        // B
        if (!this.angabenAusKibon) {
            this.form.get('anzahlEingeschriebeneKinder')
                .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('anzahlEingeschriebeneKinderKindergarten')
                .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('anzahlEingeschriebeneKinderSekundarstufe')
                .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('anzahlEingeschriebeneKinderPrimarstufe')
                .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('durchschnittKinderProTagFruehbetreuung')
                .setValidators([
                    Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('durchschnittKinderProTagMittag')
                .setValidators([
                    Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('durchschnittKinderProTagNachmittag1')
                .setValidators([
                    Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('durchschnittKinderProTagNachmittag2')
                .setValidators([
                    Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('betreuungsstundenEinschliesslichBesondereBeduerfnisse')
                .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        }
        this.form.get('anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        // C
        this.form.get('schuleAufBasisOrganisatorischesKonzept').setValidators([Validators.required]);
        this.form.get('schuleAufBasisPaedagogischesKonzept').setValidators([Validators.required]);
        this.form.get('raeumlicheVoraussetzungenEingehalten').setValidators([Validators.required]);
        this.form.get('betreuungsverhaeltnisEingehalten').setValidators([Validators.required]);
        this.form.get('ernaehrungsGrundsaetzeEingehalten').setValidators([Validators.required]);

        this.triggerFormValidation();
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
                this.latsAngabenInstitutionContainer?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) &&
                this.latsAngabenInstitutionContainer?.status !== TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);
    }

    public canSeeFreigebenButton(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) && !this.latsAngabenInstitutionContainer?.isAtLeastInBearbeitungGemeinde());
    }

    public canSeeGeprueftButton(): boolean {
        return this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) && this.latsAngabenInstitutionContainer?.isAtLeastInBearbeitungGemeinde();
    }

    public canSeeSaveButton(): boolean {
        return !this.authService.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles());
    }

    public async onFalscheAngaben(): Promise<void> {

        const gemeindeMustBeReopenedCheckRequired = !this.isInstiUser &&
            this.gemeindeAntragContainer?.isInBearbeitungGemeinde() &&
            !this.gemeindeAntragContainer.angabenDeklaration?.isInBearbeitung();

        if (gemeindeMustBeReopenedCheckRequired && !(await this.confirmDialog(this.translate.instant(
            'LATS_CONFIRM_OPEN_GEMEINDE_FORMULAR')))) {
            return;
        }
        const falscheAngabenObs$ = this.latsAngabenInstitutionContainer.isGeprueftGemeinde() ?
            this.tagesschulenAngabenRS.falscheAngabenGemeinde(this.latsAngabenInstitutionContainer) :
            this.tagesschulenAngabenRS.falscheAngabenTS(this.latsAngabenInstitutionContainer);

        falscheAngabenObs$.subscribe(() => {
            this.errorService.clearAll();
            this.wizardRS.updateSteps(TSWizardStepXTyp.LASTENAUSGLEICH_TAGESSCHULEN, this.gemeindeAntragContainer.id);
            this.lastenausgleichTSService.updateLATSAngabenGemeindeContainerStore(this.routerGlobals.params.id);
            this.cd.markForCheck();
        }, error => {
            if (error.error.includes(
                'LastenausgleichTagesschuleAngabenGemeindeContainer muss in Bearbeitung Gemeinde sein')) {
                this.errorService.addMesageAsError(this.translate.instant('LATS_FA_INSTI_NUR_WENN_GEMEINDE_OFFEN'));
            }
            this.manageSaveErrorCodes(error);
        });
    }

    public falscheAngabenVisible(): boolean {
        return this.gemeindeAntragContainer?.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE && (
                this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) &&
                this.latsAngabenInstitutionContainer?.status ===
                TSLastenausgleichTagesschuleAngabenInstitutionStatus.IN_PRUEFUNG_GEMEINDE ||
                this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) &&
                this.latsAngabenInstitutionContainer?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT);
    }

    public navigateBack($event?: MouseEvent): void {
        const parentState = 'LASTENAUSGLEICH_TAGESSCHULEN.ANGABEN_TAGESSCHULEN.LIST';
        if ($event && $event.ctrlKey) {
            const url = this.$state.href(parentState);
            window.open(url, '_blank');
        } else {
            this.$state.go(parentState);
        }
    }

    private resetBasicValidation(): void {
        if (!this.angabenAusKibon) {
            this.form.get('anzahlEingeschriebeneKinder')
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('anzahlEingeschriebeneKinderKindergarten')
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('anzahlEingeschriebeneKinderSekundarstufe')
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('anzahlEingeschriebeneKinderPrimarstufe')
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.get('durchschnittKinderProTagFruehbetreuung')
                .setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('durchschnittKinderProTagMittag')
                .setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('durchschnittKinderProTagNachmittag1')
                .setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('durchschnittKinderProTagNachmittag2')
                .setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
                ]);
            this.form.get('betreuungsstundenEinschliesslichBesondereBeduerfnisse')
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        }
        this.form.get('anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen')
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);

        this.form.get('schuleAufBasisOrganisatorischesKonzept').clearValidators();
        this.form.get('schuleAufBasisPaedagogischesKonzept').clearValidators();
        this.form.get('raeumlicheVoraussetzungenEingehalten').clearValidators();
        this.form.get('betreuungsverhaeltnisEingehalten').clearValidators();
        this.form.get('ernaehrungsGrundsaetzeEingehalten').clearValidators();

        this.triggerFormValidation();
    }

    public async fillOutCalculationsFromKiBon(): Promise<void> {
        if (!await this.confirmDialog('LATS_WARN_FILL_OUT_FROM_KIBON')) {
            return;
        }
        this.form.get('anzahlEingeschriebeneKinder')
            .setValue(this.anzahlEingeschriebeneKinder?.overall);
        this.form.get('anzahlEingeschriebeneKinderKindergarten')
            .setValue(this.anzahlEingeschriebeneKinder?.kindergarten);
        this.form.get('anzahlEingeschriebeneKinderPrimarstufe')
            .setValue(this.anzahlEingeschriebeneKinder?.primarstufe);
        this.form.get('anzahlEingeschriebeneKinderSekundarstufe')
            .setValue(this.anzahlEingeschriebeneKinder?.sekundarstufe);
        this.form.get('durchschnittKinderProTagFruehbetreuung')
            .setValue(this.durchschnittKinderProTag?.fruehbetreuung);
        this.form.get('durchschnittKinderProTagMittag')
            .setValue(this.durchschnittKinderProTag?.mittagsbetreuung);
        this.form.get('durchschnittKinderProTagNachmittag1')
            .setValue(this.durchschnittKinderProTag?.nachmittagsbetreuung1);
        this.form.get('durchschnittKinderProTagNachmittag2')
            .setValue(this.durchschnittKinderProTag?.nachmittagsbetreuung2);

        this.autoFilled = true;
    }

    private queryAnzahlEingeschriebeneKinder(): void {
        this.tagesschulenAngabenRS.getAnzahlEingeschriebeneKinder(this.latsAngabenInstitutionContainer)
            .subscribe(anzahlEingeschriebeneKinder => {
                    this.anzahlEingeschriebeneKinder = anzahlEingeschriebeneKinder;
                },
                error => {
                    LOG.error(error);
                    this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR'));
                });
    }

    private queryDurchschnittKinderProTag(): void {
        this.tagesschulenAngabenRS.getDurchschnittKinderProTag(this.latsAngabenInstitutionContainer)
            .subscribe(durchschnittKinderProTag => {
                    this.durchschnittKinderProTag = durchschnittKinderProTag;
                },
                error => {
                    LOG.error(error);
                    this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR'));
                });
    }

    private getStichtag(): void {
        this.settings.findEinstellung(TSEinstellungKey.LATS_STICHTAG,
            this.gemeindeAntragContainer.gemeinde?.id,
            this.gemeindeAntragContainer.gesuchsperiode?.id)
            .then(setting => {
                const date = moment(setting.value).format(CONSTANTS.DATE_FORMAT);
                this.stichtag.next(date);
            });
    }

    public manageSaveErrorCodes(error: any): void {
        if (error.status === HTTP_ERROR_CODES.CONFLICT) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_DATA_CHANGED'));
        } else if (error.status === HTTP_ERROR_CODES.BAD_REQUEST) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_NUMBER'));
        } else {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_UNEXPECTED'));
        }
    }

    public allAnzahlFieldsFilledOut(): boolean {
        return this.form?.get('anzahlEingeschriebeneKinder').value?.toString().length > 0 &&
            this.form?.get('anzahlEingeschriebeneKinderKindergarten').value?.toString().length > 0 &&
            this.form?.get('anzahlEingeschriebeneKinderPrimarstufe').value?.toString().length > 0 &&
            this.form?.get('anzahlEingeschriebeneKinderSekundarstufe').value?.toString().length > 0;
    }
}
