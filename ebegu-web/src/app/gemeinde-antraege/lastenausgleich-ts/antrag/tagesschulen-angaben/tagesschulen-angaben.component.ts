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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import * as moment from 'moment';
import {BehaviorSubject, combineLatest, Subject, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeStatus,
} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {
    TSLastenausgleichTagesschuleAngabenInstitutionStatus,
} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenInstitutionStatus';
import {TSWizardStepXTyp} from '../../../../../models/enums/TSWizardStepXTyp';
import {TSAnzahlEingeschriebeneKinder} from '../../../../../models/gemeindeantrag/TSAnzahlEingeschriebeneKinder';
import {TSDurchschnittKinderProTag} from '../../../../../models/gemeindeantrag/TSDurchschnittKinderProTag';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeContainer,
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {
    TSLastenausgleichTagesschuleAngabenInstitution,
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {
    TSLastenausgleichTagesschuleAngabenInstitutionContainer,
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {TSOeffnungszeitenTagesschule} from '../../../../../models/gemeindeantrag/TSOeffnungszeitenTagesschule';
import {TSOeffnungszeitenTagesschuleTyp} from '../../../../../models/gemeindeantrag/TSOeffnungszeitenTagesschuleTyp';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSExceptionReport} from '../../../../../models/TSExceptionReport';
import {TSGesuchsperiode} from '../../../../../models/TSGesuchsperiode';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {
    DvNgConfirmDialogComponent,
} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {CONSTANTS} from '../../../../core/constants/CONSTANTS';
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
    encapsulation: ViewEncapsulation.None
})
export class TagesschulenAngabenComponent implements OnInit {

    @Input() public lastenausgleichID: string;
    @Input() public institutionContainerId: string;

    public form = this.fb.group({
        // A
        isLehrbetrieb: <null | boolean>null,
        // B
        anzahlEingeschriebeneKinder: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        anzahlEingeschriebeneKinderKindergarten: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        anzahlEingeschriebeneKinderBasisstufe: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        anzahlEingeschriebeneKinderSekundarstufe: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        anzahlEingeschriebeneKinderPrimarstufe: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        anzahlEingeschriebeneKinderVolksschulangebot: [
            <null | number>null,
            numberValidator(ValidationType.POSITIVE_INTEGER),
        ],
        durchschnittKinderProTagFruehbetreuung: [
            <null | number>null,
            Validators.compose([
                numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
            ]),
        ],
        durchschnittKinderProTagMittag: [
            <null | number>null,
            Validators.compose([
                numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
            ]),
        ],
        durchschnittKinderProTagNachmittag1: [
            <null | number>null,
            Validators.compose([
                numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
            ]),
        ],
        durchschnittKinderProTagNachmittag2: [
            <null | number>null,
            Validators.compose([
                numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS),
            ]),
        ],
        betreuungsstundenEinschliesslichBesondereBeduerfnisse:
            [
                <null | number>null,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
        // C
        schuleAufBasisOrganisatorischesKonzept: <null | boolean>null,
        schuleAufBasisPaedagogischesKonzept: <null | boolean>null,
        raeumlicheVoraussetzungenEingehalten: <null | boolean>null,
        betreuungsverhaeltnisEingehalten: <null | boolean>null,
        ernaehrungsGrundsaetzeEingehalten: <null | boolean>null,
        // Bemerkungen
        bemerkungen: <null | string>null
    });
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

    // Oeffnungszeiten:
    public fruehbetreuungOeffnungszeiten: TSOeffnungszeitenTagesschule;
    public mittagsbetreuungOeffnungszeiten: TSOeffnungszeitenTagesschule;
    public nachmittagsbetreuung1Oeffnungszeiten: TSOeffnungszeitenTagesschule;
    public nachmittagsbetreuung2Oeffnungszeiten: TSOeffnungszeitenTagesschule;

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
        private readonly unsavedChangesService: UnsavedChangesService
    ) {
    }

    public ngOnInit(): void {
        this.subscription = combineLatest([
            this.lastenausgleichTSService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$
        ]).subscribe(([container, principal]) => {
            this.gemeindeAntragContainer = container;
            this.latsAngabenInstitutionContainer =
                container.angabenInstitutionContainers?.find(institutionContainer => institutionContainer.id === this.institutionContainerId);
            this.gesuchsPeriode = container.gesuchsperiode;
            const angaben = this.latsAngabenInstitutionContainer?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN ?
                this.latsAngabenInstitutionContainer?.angabenDeklaration :
                this.latsAngabenInstitutionContainer?.angabenKorrektur;
            this.angabenAusKibon = container.alleAngabenInKibonErfasst;
            this.form.patchValue(angaben);
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
            this.fruehbetreuungOeffnungszeiten =
                angaben.oeffnungszeiten.find(oeffnungszeit => oeffnungszeit.type === TSOeffnungszeitenTagesschuleTyp.FRUEHBETREUUNG);
            this.mittagsbetreuungOeffnungszeiten =
                angaben.oeffnungszeiten.find(oeffnungszeit => oeffnungszeit.type === TSOeffnungszeitenTagesschuleTyp.MITTAGSBETREUUNG);
            this.nachmittagsbetreuung1Oeffnungszeiten =
                angaben.oeffnungszeiten.find(oeffnungszeit => oeffnungszeit.type === TSOeffnungszeitenTagesschuleTyp.NACHMITTAGSBETREUUNG_1);
            this.nachmittagsbetreuung2Oeffnungszeiten =
                angaben.oeffnungszeiten.find(oeffnungszeit => oeffnungszeit.type === TSOeffnungszeitenTagesschuleTyp.NACHMITTAGSBETREUUNG_2);
            this.initOeffnungszeiten();
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError(this.translate.instant('DATA_RETRIEVAL_ERROR'));
        });
    }

    private initOeffnungszeiten(): void {
        if (EbeguUtil.isNullOrUndefined(this.fruehbetreuungOeffnungszeiten)) {
            this.fruehbetreuungOeffnungszeiten = new TSOeffnungszeitenTagesschule();
            this.fruehbetreuungOeffnungszeiten.type = TSOeffnungszeitenTagesschuleTyp.FRUEHBETREUUNG;
        }
        if (EbeguUtil.isNullOrUndefined(this.mittagsbetreuungOeffnungszeiten)) {
            this.mittagsbetreuungOeffnungszeiten = new TSOeffnungszeitenTagesschule();
            this.mittagsbetreuungOeffnungszeiten.type = TSOeffnungszeitenTagesschuleTyp.MITTAGSBETREUUNG;
        }
        if (EbeguUtil.isNullOrUndefined(this.nachmittagsbetreuung1Oeffnungszeiten)) {
            this.nachmittagsbetreuung1Oeffnungszeiten = new TSOeffnungszeitenTagesschule();
            this.nachmittagsbetreuung1Oeffnungszeiten.type = TSOeffnungszeitenTagesschuleTyp.NACHMITTAGSBETREUUNG_1;
        }
        if (EbeguUtil.isNullOrUndefined(this.nachmittagsbetreuung2Oeffnungszeiten)) {
            this.nachmittagsbetreuung2Oeffnungszeiten = new TSOeffnungszeitenTagesschule();
            this.nachmittagsbetreuung2Oeffnungszeiten.type = TSOeffnungszeitenTagesschuleTyp.NACHMITTAGSBETREUUNG_2;
        }
    }

    // eslint-disable-next-line
    private setupRoleBasedPropertiesForPrincipal(
        container: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
        angaben: TSLastenausgleichTagesschuleAngabenInstitutionContainer,
        principal: TSBenutzer
    ): void {
        let canSeeDurchKibonAusfuellen = false;
        let canSeeGeprueft = false;
        let canSeeFalscheAngaben = false;
        let canSeeFreigeben = false;
        let canSeeSave = false;

        if (container.isInBearbeitungGemeinde()) {
            if (angaben.isInBearbeitungInstitution()) {
                canSeeFreigeben = principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles()) ||
                    principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
                canSeeSave = principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles()) ||
                    principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
                canSeeDurchKibonAusfuellen = principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles()) ||
                    principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
            }
            if (angaben.isInPruefungGemeinde()) {
                canSeeFalscheAngaben = principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
                canSeeSave = principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
                canSeeGeprueft = principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
            }
            if (angaben.isGeprueftGemeinde()) {
                canSeeFalscheAngaben = principal.hasOneOfRoles(TSRoleUtil.getGemeindeRoles());
            }
        }

        if (container.isinPruefungKanton()) {
            if (angaben.isInPruefungGemeinde()) {
                canSeeSave = principal.hasOneOfRoles(TSRoleUtil.getMandantRoles());
                canSeeGeprueft = principal.hasOneOfRoles(TSRoleUtil.getMandantRoles());
            }
            if (angaben.isGeprueftGemeinde()) {
                canSeeFalscheAngaben = principal.hasOneOfRoles(TSRoleUtil.getMandantRoles());
            }
        }

        this.canSeeDurchKibonAusfuellen.next(canSeeDurchKibonAusfuellen);
        this.canSeeSave.next(canSeeSave);
        this.canSeeFreigeben.next(canSeeFreigeben);
        this.canSeeAbschliessen.next(canSeeGeprueft);
        this.canSeeFalscheAngaben.next(canSeeFalscheAngaben);
    }

    public canEditForm(): boolean {
        const angaben = this.latsAngabenInstitutionContainer;
        if (EbeguUtil.isNullOrUndefined(angaben)) {
            return false;
        }
        return (this.authService.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) &&
                angaben.isInBearbeitungInstitution()) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getGemeindeRoles()) &&
                !this.gemeindeAntragContainer.isinPruefungKanton() &&
                (angaben.isInBearbeitungInstitution() || angaben.isInPruefungGemeinde())) ||
            (this.authService.isOneOfRoles(TSRoleUtil.getMandantRoles()) &&
                this.gemeindeAntragContainer.isinPruefungKanton() &&
                angaben.isInPruefungGemeinde());
    }

    private setupCalculation(angaben: TSLastenausgleichTagesschuleAngabenInstitution): void {
        combineLatest(
            [
                this.form.controls.anzahlEingeschriebeneKinder.valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinder || 0)),
                this.form.controls.anzahlEingeschriebeneKinderKindergarten.valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderKindergarten || 0)),
                this.form.controls.anzahlEingeschriebeneKinderPrimarstufe.valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderPrimarstufe || 0)),
                this.form.controls.anzahlEingeschriebeneKinderSekundarstufe.valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderSekundarstufe || 0)),
                this.form.controls.anzahlEingeschriebeneKinderBasisstufe.valueChanges
                    .pipe(startWith(angaben?.anzahlEingeschriebeneKinderBasisstufe || 0))
            ]
        ).subscribe(values => {
            this.abweichungenAnzahlKinder = values[0] - values[1] - values[2] - values[3] - values[4];
            this.cd.markForCheck();
        }, () => {
            this.errorService.addMesageAsError('BAD_NUMBER_ERROR');
        });
    }

    public onFormSubmit(): void {
        this.resetBasicValidation();
        if (!this.form.valid) {
            this.errorService.addMesageAsError(
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN')
            );
            return;
        }
        this.setFormValuesToAngaben();
        this.errorService.clearAll();
        this.tagesschulenAngabenRS.saveTagesschuleAngaben(this.latsAngabenInstitutionContainer).subscribe(result => {
            this.latsAngabenInstitutionContainer = result;
            this.form.patchValue(
                result?.status === TSLastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN ?
                    result?.angabenDeklaration :
                    result?.angabenKorrektur);
            this.errorService.addMesageAsInfo(this.translate.instant('SAVED'));
            this.form.markAsPristine();
            this.unsavedChangesService.registerForm(this.form);
        }, error => {
            this.manageSaveErrorCodes(error);
        });
    }

    private setFormValuesToAngaben(): void {
        if (this.latsAngabenInstitutionContainer.isAtLeastInBearbeitungGemeinde()) {
            this.setFormValuesToAngabenKorrektur();
        } else {
           this.setFormValuesToAngabenDeklaration();
        }
    }

    private getOeffnungszeitenArray(): TSOeffnungszeitenTagesschule[] {
        return [
            this.fruehbetreuungOeffnungszeiten,
            this.mittagsbetreuungOeffnungszeiten,
            this.nachmittagsbetreuung1Oeffnungszeiten,
            this.nachmittagsbetreuung2Oeffnungszeiten
        ];
    }

    private setFormValuesToAngabenKorrektur(): void {
        this.writeBackForm(this.latsAngabenInstitutionContainer.angabenKorrektur);
        this.latsAngabenInstitutionContainer.angabenKorrektur.oeffnungszeiten = this.getOeffnungszeitenArray();
    }

    private setFormValuesToAngabenDeklaration(): void {
        this.writeBackForm(this.latsAngabenInstitutionContainer.angabenDeklaration);
        this.latsAngabenInstitutionContainer.angabenDeklaration.oeffnungszeiten = this.getOeffnungszeitenArray();
    }

    public async onFreigeben(): Promise<void> {
        this.formFreigebenTriggered = true;
        this.enableFormValidation();
        this.errorService.clearAll();
        if (!this.form.valid) {
            this.errorService.addMesageAsError(
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN')
            );
            return;
        }
        if (!await this.confirmDialog('LATS_FRAGE_INSTITUTION_FORMULAR_FREIGEBEN')) {
            return;
        }
        this.setFormValuesToAngabenDeklaration();

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
            frage: this.translate.instant(frageKey)
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
                this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN')
            );
            return;
        }

        if (!await this.confirmDialog('LATS_FRAGE_INSTITUTION_FORMULAR_GEPRUEFT')) {
            return;
        }

        this.setFormValuesToAngabenKorrektur();

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
        this.form.controls.isLehrbetrieb.setValidators([Validators.required]);
        // B
        this.form.controls.anzahlEingeschriebeneKinder
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderKindergarten
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderBasisstufe
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderSekundarstufe
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderPrimarstufe
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.durchschnittKinderProTagFruehbetreuung.setValidators([
                Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.form.controls.durchschnittKinderProTagMittag.setValidators([
                Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.form.controls.durchschnittKinderProTagNachmittag1.setValidators([
                Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.form.controls.durchschnittKinderProTagNachmittag2.setValidators([
                Validators.required, numberValidator(ValidationType.ANY_NUMBER),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.form.controls.betreuungsstundenEinschliesslichBesondereBeduerfnisse
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderVolksschulangebot
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // C
        this.form.controls.schuleAufBasisOrganisatorischesKonzept.setValidators([Validators.required]);
        this.form.controls.schuleAufBasisPaedagogischesKonzept.setValidators([Validators.required]);
        this.form.controls.raeumlicheVoraussetzungenEingehalten.setValidators([Validators.required]);
        this.form.controls.betreuungsverhaeltnisEingehalten.setValidators([Validators.required]);
        this.form.controls.ernaehrungsGrundsaetzeEingehalten.setValidators([Validators.required]);

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
            this.form.controls.anzahlEingeschriebeneKinder
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.controls.anzahlEingeschriebeneKinderKindergarten
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.controls.anzahlEingeschriebeneKinderSekundarstufe
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.controls.anzahlEingeschriebeneKinderPrimarstufe
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
            this.form.controls.durchschnittKinderProTagFruehbetreuung.setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ]);
            this.form.controls.durchschnittKinderProTagMittag.setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ]);
            this.form.controls.durchschnittKinderProTagNachmittag1.setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ]);
            this.form.controls.durchschnittKinderProTagNachmittag2.setValidators([
                    numberValidator(ValidationType.ANY_NUMBER),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ]);
            this.form.controls.betreuungsstundenEinschliesslichBesondereBeduerfnisse
                .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        }
        this.form.controls.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.form.controls.anzahlEingeschriebeneKinderVolksschulangebot
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
        this.form.controls.anzahlEingeschriebeneKinder.setValue(this.anzahlEingeschriebeneKinder?.overall);
        this.form.controls.anzahlEingeschriebeneKinderKindergarten
            .setValue(this.anzahlEingeschriebeneKinder?.kindergarten);
        this.form.controls.anzahlEingeschriebeneKinderPrimarstufe
            .setValue(this.anzahlEingeschriebeneKinder?.primarstufe);
        this.form.controls.anzahlEingeschriebeneKinderSekundarstufe
            .setValue(this.anzahlEingeschriebeneKinder?.sekundarstufe);
        this.form.controls.durchschnittKinderProTagFruehbetreuung
            .setValue(this.durchschnittKinderProTag?.fruehbetreuung);
        this.form.controls.durchschnittKinderProTagMittag.setValue(this.durchschnittKinderProTag?.mittagsbetreuung);
        this.form.controls.durchschnittKinderProTagNachmittag1
            .setValue(this.durchschnittKinderProTag?.nachmittagsbetreuung1);
        this.form.controls.durchschnittKinderProTagNachmittag2
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
            .subscribe(setting => {
                const date = moment(setting.value).format(CONSTANTS.DATE_FORMAT);
                this.stichtag.next(date);
            }, error => LOG.error(error));
    }

    public manageSaveErrorCodes(errors: TSExceptionReport[]): void {
        LOG.error(errors.map(error => error.customMessage).join('; '));
    }

    public allAnzahlFieldsFilledOut(): boolean {
        return this.form.value.anzahlEingeschriebeneKinderBasisstufe?.toString().length > 0 &&
            this.form.value.anzahlEingeschriebeneKinder?.toString().length > 0 &&
            this.form.value.anzahlEingeschriebeneKinderKindergarten?.toString().length > 0 &&
            this.form.value.anzahlEingeschriebeneKinderPrimarstufe?.toString().length > 0 &&
            this.form.value.anzahlEingeschriebeneKinderSekundarstufe?.toString().length > 0;
    }

    private writeBackForm(angabenKorrektur: TSLastenausgleichTagesschuleAngabenInstitution): void {
        const values = this.form.getRawValue();
        angabenKorrektur.isLehrbetrieb = values.isLehrbetrieb;
        // B
        angabenKorrektur.anzahlEingeschriebeneKinder = values.anzahlEingeschriebeneKinder;
        angabenKorrektur.anzahlEingeschriebeneKinderKindergarten = values.anzahlEingeschriebeneKinderKindergarten;
        angabenKorrektur.anzahlEingeschriebeneKinderBasisstufe = values.anzahlEingeschriebeneKinderBasisstufe;
        angabenKorrektur.anzahlEingeschriebeneKinderSekundarstufe = values.anzahlEingeschriebeneKinderSekundarstufe;
        angabenKorrektur.anzahlEingeschriebeneKinderPrimarstufe = values.anzahlEingeschriebeneKinderPrimarstufe;
        angabenKorrektur.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen =
            values.anzahlEingeschriebeneKinderMitBesonderenBeduerfnissen;
        angabenKorrektur.anzahlEingeschriebeneKinderVolksschulangebot =
            values.anzahlEingeschriebeneKinderVolksschulangebot;
        angabenKorrektur.durchschnittKinderProTagFruehbetreuung = values.durchschnittKinderProTagFruehbetreuung;
        angabenKorrektur.durchschnittKinderProTagMittag = values.durchschnittKinderProTagMittag;
        angabenKorrektur.durchschnittKinderProTagNachmittag1 = values.durchschnittKinderProTagNachmittag1;
        angabenKorrektur.durchschnittKinderProTagNachmittag2 = values.durchschnittKinderProTagNachmittag2;
        angabenKorrektur.betreuungsstundenEinschliesslichBesondereBeduerfnisse =
            values.betreuungsstundenEinschliesslichBesondereBeduerfnisse;
        // C
        angabenKorrektur.schuleAufBasisOrganisatorischesKonzept = values.schuleAufBasisOrganisatorischesKonzept;
        angabenKorrektur.schuleAufBasisPaedagogischesKonzept = values.schuleAufBasisPaedagogischesKonzept;
        angabenKorrektur.raeumlicheVoraussetzungenEingehalten = values.raeumlicheVoraussetzungenEingehalten;
        angabenKorrektur.ernaehrungsGrundsaetzeEingehalten = values.ernaehrungsGrundsaetzeEingehalten;
        angabenKorrektur.betreuungsverhaeltnisEingehalten = values.ernaehrungsGrundsaetzeEingehalten;
        // Bemerkungen
        angabenKorrektur.bemerkungen = values.bemerkungen;
    }
}
