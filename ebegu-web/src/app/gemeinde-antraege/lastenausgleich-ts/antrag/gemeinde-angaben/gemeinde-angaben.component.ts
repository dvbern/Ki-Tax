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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatRadioChange} from '@angular/material/radio';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {BehaviorSubject, combineLatest, ReplaySubject, Subject, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus,
} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeStatus,
} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSWizardStepXTyp} from '../../../../../models/enums/TSWizardStepXTyp';
import {
    TSLastenausgleichTagesschuleAngabenGemeinde,
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeContainer,
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSEinstellung} from '../../../../../models/TSEinstellung';
import {TSExceptionReport} from '../../../../../models/TSExceptionReport';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {MathUtil} from '../../../../../utils/MathUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgOkDialogComponent} from '../../../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {CONSTANTS} from '../../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../../shared/validators/number-validator.directive';
import {GemeindeAntragService} from '../../../services/gemeinde-antrag.service';
import {UnsavedChangesService} from '../../../services/unsaved-changes.service';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';
import {TSControllingCalculator} from './TSControllingCalculator';

const LOG = LogFactory.createLog('GemeindeAngabenComponent');

@Component({
    selector: 'dv-gemeinde-angaben',
    templateUrl: './gemeinde-angaben.component.html',
    styleUrls: ['./gemeinde-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
})
export class GemeindeAngabenComponent implements OnInit, OnDestroy {

    @Input() public lastenausgleichID: string;
    @Input() public triggerValidationOnInit = false;

    public angabenForm = this.fb.group({
        status: [<null|string>null],
        version: [<null|number>null],
        // A
        alleFaelleInKibon: [<null|boolean>null],
        angebotVerfuegbarFuerAlleSchulstufen: [<null|boolean>null],
        begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen: [],
        bedarfBeiElternAbgeklaert: [<null|boolean>null],
        angebotFuerFerienbetreuungVorhanden: [<null|boolean>null],
        // B
        geleisteteBetreuungsstundenOhneBesondereBeduerfnisse:
            [<null|number>null, numberValidator(ValidationType.POSITIVE_INTEGER)],
        geleisteteBetreuungsstundenBesondereBeduerfnisse: [<null|number>null,  numberValidator(ValidationType.POSITIVE_INTEGER)],
        geleisteteBetreuungsstundenBesondereVolksschulangebot:
            [<null|number>null, numberValidator(ValidationType.POSITIVE_INTEGER)],
        davonStundenZuNormlohnMehrAls50ProzentAusgebildete: [<null|number>null, numberValidator(ValidationType.POSITIVE_INTEGER)],
        davonStundenZuNormlohnWenigerAls50ProzentAusgebildete:
            [<null|number>null, numberValidator(ValidationType.POSITIVE_INTEGER)],
        einnahmenElterngebuehren:
            [<null|number>null, [this.numberValidator(), Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)]],
        einnahmenElterngebuehrenVolksschulangebot:
            [<null|number>null,[this.numberValidator(), Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)]],
        ersteRateAusbezahlt: [<null|number>null, numberValidator(ValidationType.POSITIVE_INTEGER)],
        tagesschuleTeilweiseGeschlossen: [<null|boolean>null],
        rueckerstattungenElterngebuehrenSchliessung:
            [<null|number>null,[this.numberValidator(), Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)]],
        // C
        gesamtKostenTagesschule:
            [<null|number>null, [this.numberValidator(), Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)]],
        einnnahmenVerpflegung: [<null|number>null, [this.numberValidator(), Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)]],
        einnahmenSubventionenDritter:
            [<null|number>null, [this.numberValidator(), Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)]],
        ueberschussErzielt: [<null|boolean>null],
        ueberschussVerwendung: [<null|string>null],
        // D
        bemerkungenWeitereKostenUndErtraege: [<null|string>null],
        // E
        betreuungsstundenDokumentiertUndUeberprueft: [<null|boolean>null],
        betreuungsstundenDokumentiertUndUeberprueftBemerkung: [<null|string>null],
        elterngebuehrenGemaessVerordnungBerechnet: [<null|boolean>null],
        elterngebuehrenGemaessVerordnungBerechnetBemerkung: [<null|string>null],
        einkommenElternBelegt: [<null|boolean>null],
        einkommenElternBelegtBemerkung: [<null|string>null],
        maximalTarif: [<null|boolean>null],
        maximalTarifBemerkung: [<null|string>null],
        mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal: [<null|boolean>null],
        mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung: [<null|string>null],
        ausbildungenMitarbeitendeBelegt: [<null|boolean>null],
        ausbildungenMitarbeitendeBelegtBemerkung: [<null|string>null],
        // Bemerkungen
        bemerkungen: [<null|string>null],
        bemerkungStarkeVeraenderung: [<null|string>null],
        // calculated values
        lastenausgleichberechtigteBetreuungsstunden: [this.fb.control({value: <null|number>null, disabled: true})],
        davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet: [{value: <null|number>null, disabled: true}],
        davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet: [{value: <null|number>null, disabled: true}],
        normlohnkostenBetreuungBerechnet: [{value: <null|number>null, disabled: true}],
        lastenausgleichsberechtigerBetrag: [{value: <null|number>null, disabled: true}],
        lastenausgleichsberechtigerBetragRO: [{value: <null|number>null, disabled: true}],
        einnahmenElterngebuehrenRO: [{value: <null|number>null, disabled: true}],
        kostenbeitragGemeinde: [{value: <null|number>null, disabled: true}],
        kostenueberschussGemeinde: [{value: <null|number>null, disabled: true}],
        erwarteterKostenbeitragGemeinde: [{value: <null|number>null, disabled: true}],
        schlusszahlung: [{value: <null|number>null, disabled: true}]
    });
    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public formularInitForm: FormGroup<{alleAngabenInKibonErfasst: FormControl<boolean>}>;
    private subscription: Subscription;
    public abschliessenValidationActive = false;
    public lohnnormkostenSettingMoreThanFifty$: Subject<TSEinstellung> = new ReplaySubject<TSEinstellung>(1);
    public lohnnormkostenSettingLessThanFifty$: Subject<TSEinstellung> = new ReplaySubject<TSEinstellung>(1);

    public saveVisible: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public abschliessenVisible: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public falscheAngabenVisible: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    public controllingCalculator: TSControllingCalculator;
    public previousAntrag: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public erwarteteBetreuungsstunden: number;

    private readonly kostenbeitragGemeinde = 0.2;
    private readonly WIZARD_TYPE: TSWizardStepXTyp = TSWizardStepXTyp.LASTENAUSGLEICH_TAGESSCHULEN;
    public hasStarkeVeraenderung: boolean = false;

    public constructor(
        private readonly fb: FormBuilder,
        private readonly gemeindeAntraegeService: GemeindeAntragService,
        private readonly cd: ChangeDetectorRef,
        private readonly authServiceRS: AuthServiceRS,
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly errorService: ErrorService,
        private readonly translateService: TranslateService,
        private readonly settings: EinstellungRS,
        private readonly wizardRS: WizardStepXRS,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly dialog: MatDialog,
        private readonly unsavedChangesService: UnsavedChangesService,
        private readonly $state: StateService
    ) {
    }

    public ngOnInit(): void {
        this.subscription = combineLatest([
            this.lastenausgleichTSService.getLATSAngabenGemeindeContainer(),
            this.authServiceRS.principal$
        ]).subscribe(([container, principal]) => {
            this.lATSAngabenGemeindeContainer = container;
            if (this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst !== null) {
                const gemeindeAngaben = container.getAngabenToWorkWith();
                this.setupForm(gemeindeAngaben);
                this.setupCalculcations(gemeindeAngaben);
            }
            this.initLATSGemeindeInitializationForm(container, principal);
            this.setupPermissions(container, principal);
            this.settings.findEinstellung(TSEinstellungKey.LATS_LOHNNORMKOSTEN,
                this.lATSAngabenGemeindeContainer.gemeinde?.id,
                this.lATSAngabenGemeindeContainer.gesuchsperiode?.id)
                .subscribe(setting => this.lohnnormkostenSettingMoreThanFifty$.next(setting),
                        error => LOG.error(error));
            this.settings.findEinstellung(TSEinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50,
                this.lATSAngabenGemeindeContainer.gemeinde?.id,
                this.lATSAngabenGemeindeContainer.gesuchsperiode?.id)
                .subscribe(setting => this.lohnnormkostenSettingLessThanFifty$.next(setting),
                    error => LOG.error(error));
            this.unsavedChangesService.registerForm(this.angabenForm);
            this.initControlling();
            this.cd.markForCheck();

        }, () => this.errorService.addMesageAsError(this.translateService.instant('DATA_RETRIEVAL_ERROR')));

    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public onInitFormSubmit(): void {
        if (this.formularInitForm.valid) {
            this.lATSAngabenGemeindeFuerInstitutionenFreigeben();
        }
    }

    private initLATSGemeindeInitializationForm(
        container: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
        principal: TSBenutzer
    ): void {
        if (this.formularInitForm) {
            this.formularInitForm.patchValue({
                alleAngabenInKibonErfasst: container.alleAngabenInKibonErfasst
            });
        } else {
            this.formularInitForm = this.fb.group({
                alleAngabenInKibonErfasst: [
                    container.alleAngabenInKibonErfasst,
                    Validators.required
                ]
            });
        }
        if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles().concat(TSRole.SUPER_ADMIN))
            && container.isInBearbeitungGemeinde() && container.angabenDeklaration.isInBearbeitung()) {
            this.formularInitForm.enable();
        } else {
            this.formularInitForm.disable();
        }
    }

    private setupForm(initialGemeindeAngaben: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        this.angabenForm.patchValue(initialGemeindeAngaben);

        if (this.lATSAngabenGemeindeContainer.isGemeindeFormularInBearbeitungForRole(this.authServiceRS.getPrincipalRole())) {
            this.angabenForm.enable();
        } else {
            this.angabenForm.disable();
        }
    }

    private enableFormValidation(): void {
        // A
        this.angabenForm.controls.angebotVerfuegbarFuerAlleSchulstufen.setValidators([Validators.required]);
        this.angabenForm.controls.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen
            .setValidators([Validators.required]);
        this.angabenForm.controls.bedarfBeiElternAbgeklaert.setValidators([Validators.required]);
        this.angabenForm.controls.angebotFuerFerienbetreuungVorhanden.setValidators([Validators.required]);

        this.angabenForm.controls.angebotVerfuegbarFuerAlleSchulstufen.valueChanges.subscribe(value => {
            this.setValidatorRequiredIfFalse('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen', value);
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // B
        this.angabenForm.controls.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.controls.geleisteteBetreuungsstundenBesondereBeduerfnisse
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.controls.geleisteteBetreuungsstundenBesondereVolksschulangebot
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildete
            .setValidators([
                Validators.required,
                numberValidator(ValidationType.POSITIVE_INTEGER),
                this.plausibilisierungAddition()
            ]);
        this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete
            .setValidators([
                Validators.required,
                numberValidator(ValidationType.POSITIVE_INTEGER),
                this.plausibilisierungAddition()
            ]);
        this.angabenForm.controls.einnahmenElterngebuehren
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        if (this.showCornonaFrage()) {
            this.angabenForm.controls.tagesschuleTeilweiseGeschlossen
                .setValidators([Validators.required]);
        }
        this.angabenForm.controls.rueckerstattungenElterngebuehrenSchliessung
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.lastenausgleichberechtigteBetreuungsstunden
            .setValidators([
                this.plausibilisierungTageschulenStunden(),
                this.allInstitutionsGeprueft()
            ]);
        this.angabenForm.controls.ersteRateAusbezahlt
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // eslint-disable-next-line
        this.angabenForm.controls.tagesschuleTeilweiseGeschlossen.valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.controls.rueckerstattungenElterngebuehrenSchliessung
                    .setValidators([Validators.required, this.numberValidator()]);
            } else {
                this.angabenForm.controls.rueckerstattungenElterngebuehrenSchliessung
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // C
        this.angabenForm.controls.gesamtKostenTagesschule
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.einnnahmenVerpflegung
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.einnahmenSubventionenDritter
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.ueberschussErzielt
            .setValidators([Validators.required]);
        // eslint-disable-next-line
        this.angabenForm.controls.ueberschussErzielt.valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.controls.ueberschussVerwendung
                    .setValidators([Validators.required]);
            } else {
                this.angabenForm.controls.ueberschussVerwendung
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // E
        this.angabenForm.controls.betreuungsstundenDokumentiertUndUeberprueft.setValidators([Validators.required]);
        this.angabenForm.controls.betreuungsstundenDokumentiertUndUeberprueft.valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('betreuungsstundenDokumentiertUndUeberprueftBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'betreuungsstundenDokumentiertUndUeberprueftBemerkung Subscribe Error')));

        this.angabenForm.controls.elterngebuehrenGemaessVerordnungBerechnet.setValidators([Validators.required]);
        this.angabenForm.controls.elterngebuehrenGemaessVerordnungBerechnet.valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('elterngebuehrenGemaessVerordnungBerechnetBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'elterngebuehrenGemaessVerordnungBerechnetBemerkung ValueChanges Error')));

        this.angabenForm.controls.einkommenElternBelegt.setValidators([Validators.required]);
        this.angabenForm.controls.einkommenElternBelegt.valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('einkommenElternBelegtBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'einkommenElternBelegtBemerkung ValueChanges error')));

        this.angabenForm.controls.maximalTarif.valueChanges.subscribe(value => {
            this.setValidatorRequiredIfFalse('maximalTarifBemerkung', value);
        }, () => this.errorService.addMesageAsError(this.translateService.instant('Maximal Tarif ValueChanges error')));

        this.angabenForm.controls.ausbildungenMitarbeitendeBelegt.setValidators([Validators.required]);
        this.angabenForm.controls.ausbildungenMitarbeitendeBelegt.valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('ausbildungenMitarbeitendeBelegtBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'AusbildungMitarbeitendeBelegt ValueChanges error')));
    }

    private numberValidator(): ValidatorFn {
        // eslint-disable-next-line
        return (control: AbstractControl): {} | null => {
            return isNaN(control.value) ? {
                noNumberError: control.value
            } : null;
        };
    }

    private plausibilisierungAddition(): ValidatorFn {
        return control => this.angabenForm.value.lastenausgleichberechtigteBetreuungsstunden ===
            MathUtil.addFloatPrecisionSafe(
                this.angabenForm.value.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete,
                this.angabenForm.value.davonStundenZuNormlohnMehrAls50ProzentAusgebildete
            ) ? null : {
                plausibilisierungAdditionError: control.value
            };
    }

    private plausibilisierungTageschulenStunden(): ValidatorFn {
        return control => {
            const tagesschulenSum = this.lATSAngabenGemeindeContainer.angabenInstitutionContainers.reduce((
                accumulator,
                next
                ) => accumulator + (next.isInBearbeitungInstitution() ?
                next.angabenDeklaration.betreuungsstundenEinschliesslichBesondereBeduerfnisse :
                next.angabenKorrektur.betreuungsstundenEinschliesslichBesondereBeduerfnisse),
                0);

            return this.angabenForm.value.lastenausgleichberechtigteBetreuungsstunden === tagesschulenSum ?
                null :
                {
                    plausibilisierungTagesschulenStundenError: control.value
                };
        };
    }

    /**
     * Sets up form obervers that calculate intermediate results of the form that are presented to the user each
     * time the inputs change
     *
     * @param gemeindeAngabenFromServer existing data, used for initiating some calculations
     */
    private setupCalculcations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        this.setupLastenausgleichberechtigteBetreuungsstundenCalculations(gemeindeAngabenFromServer);
        this.setupStundenWeniger50ProzentCalculations(gemeindeAngabenFromServer);
        this.setupStundenNormlohnMehr50ProzentCalculations(gemeindeAngabenFromServer);
        this.setupNormlohnkostenBetreuungBerechnetCalculations();
        this.setupKostenGemeindeCalculations(gemeindeAngabenFromServer);
        this.setupLastenausgleichsberechtigterBetragCalculations(gemeindeAngabenFromServer);
        this.setupSchlusszahlungenCalculations(gemeindeAngabenFromServer);

    }

    // eslint-disable-next-line max-len
    private setupLastenausgleichberechtigteBetreuungsstundenCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest(
            [
                this.angabenForm.controls.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse.valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse)
                ),
                this.angabenForm.controls.geleisteteBetreuungsstundenBesondereBeduerfnisse.valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenBesondereBeduerfnisse)
                ),
                this.angabenForm.controls.geleisteteBetreuungsstundenBesondereVolksschulangebot.valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenBesondereVolksschulangebot)
                )
            ]
        ).subscribe(formValues => {
            this.angabenForm.controls.lastenausgleichberechtigteBetreuungsstunden.setValue(
                    MathUtil.addArrayFloatPrecisionSafe(formValues[0] || 0,
                        [formValues[1] || 0, formValues[2] || 0]),
                );
            this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete.updateValueAndValidity();
            this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildete.updateValueAndValidity();
            this.angabenForm.controls.lastenausgleichberechtigteBetreuungsstunden
                .updateValueAndValidity({emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // eslint-disable-next-line max-len
    private setupStundenNormlohnMehr50ProzentCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildete.valueChanges.pipe(
                startWith(gemeindeAngabenFromServer?.davonStundenZuNormlohnMehrAls50ProzentAusgebildete)
            ),
            this.lohnnormkostenSettingMoreThanFifty$
        ]).subscribe(valueAndParameter => {
            const value = valueAndParameter[0];
            const lohnkostenParam = parseFloat(valueAndParameter[1].value);
            const roundedValue = (value && lohnkostenParam) ? parseFloat((value * lohnkostenParam).toFixed(2)) : 0;
            this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet.setValue(roundedValue);
            this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete
                .updateValueAndValidity({onlySelf: true, emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    private setValidatorRequiredIfFalse(fieldname: string, value: undefined | boolean): void {
        if (EbeguUtil.isNotNullAndFalse(value)) {
            this.angabenForm.get(fieldname).setValidators([Validators.required]);
        } else {
            this.angabenForm.get(fieldname).setValidators(null);
        }
    }

    // eslint-disable-next-line max-len
    private setupStundenWeniger50ProzentCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete.valueChanges.pipe(
                startWith(gemeindeAngabenFromServer?.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete)
            ),
            this.lohnnormkostenSettingLessThanFifty$
        ]).subscribe(valueAndParamter => {
            const value = valueAndParamter[0];
            const lohnkostenParam = parseFloat(valueAndParamter[1].value);
            const roundedValue = (value && lohnkostenParam) ? parseFloat((value * lohnkostenParam).toFixed(2)) : 0;
            this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet.setValue(roundedValue);
            this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildete
                .updateValueAndValidity({onlySelf: true, emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // eslint-disable-next-line max-len
    private setupLastenausgleichsberechtigterBetragCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.controls.normlohnkostenBetreuungBerechnet.valueChanges.pipe(
                    startWith(0),
                ),
            this.angabenForm.controls.einnahmenElterngebuehren.valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.einnahmenElterngebuehren || 0),
                ),
        ]).subscribe(values => {
                const result = MathUtil.subtractFloatPrecisionSafe(values[0], values[1]);
                // round to next Franken
                const roundedResult = parseFloat(Math.ceil(result).toFixed(2));
                this.angabenForm.controls.lastenausgleichsberechtigerBetrag.setValue(roundedResult);
                this.angabenForm.controls.lastenausgleichsberechtigerBetragRO.setValue(
                    // round to next Franken
                    roundedResult,
                );
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR'))
        );
    }

    // eslint-disable-next-line max-len
    private setupKostenGemeindeCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.controls.gesamtKostenTagesschule.valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.gesamtKostenTagesschule || 0)),
            this.angabenForm.controls.lastenausgleichsberechtigerBetrag.valueChanges
                .pipe(startWith(0)),
            this.angabenForm.controls.einnahmenElterngebuehren.valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnahmenElterngebuehren || 0)),
            this.angabenForm.controls.einnnahmenVerpflegung.valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnnahmenVerpflegung || 0)),
            this.angabenForm.controls.einnahmenSubventionenDritter.valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnahmenSubventionenDritter || 0))
        ]).subscribe(values => {
                const gemeindeBeitragOderUeberschuss = MathUtil.subtractArrayFloatPrecisionSafe(values[0],
                    values.slice(1, 4));
                if (+gemeindeBeitragOderUeberschuss < 0) {
                    this.angabenForm.controls.kostenueberschussGemeinde.setValue(gemeindeBeitragOderUeberschuss);
                    this.angabenForm.controls.kostenbeitragGemeinde.setValue(null);
                } else {
                    this.angabenForm.controls.kostenbeitragGemeinde.setValue(gemeindeBeitragOderUeberschuss);
                    this.angabenForm.controls.kostenueberschussGemeinde.setValue(null);
                }

                this.angabenForm.controls.erwarteterKostenbeitragGemeinde.setValue(
                    parseFloat((values[0] * this.kostenbeitragGemeinde).toFixed(2)));
                this.angabenForm.controls.einnahmenElterngebuehrenRO.setValue(
                    parseFloat(values[2].toFixed(2)));
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR'))
        );
    }

    private setupNormlohnkostenBetreuungBerechnetCalculations(): void {
        combineLatest(
            [
                this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet.valueChanges
                    .pipe(startWith(0)),
                this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet.valueChanges
                    .pipe(startWith(0))
            ]
        ).subscribe(value => {
                const normlohnkostenExact = MathUtil.addFloatPrecisionSafe(value[0], value[1]);
                const normlohnkostenRounded = EbeguUtil.ceilToFiveRappen(normlohnkostenExact);
                this.angabenForm.controls.normlohnkostenBetreuungBerechnet.setValue(
                    parseFloat(normlohnkostenRounded.toFixed(2))
                );
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR'))
        );
    }

    // eslint-disable-next-line max-len
    private setupSchlusszahlungenCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.controls.lastenausgleichsberechtigerBetrag.valueChanges.pipe(
                    startWith(0)
                ),
            this.angabenForm.controls.ersteRateAusbezahlt.valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.ersteRateAusbezahlt || 0)
                )
            // eslint-disable-next-line
        ]).subscribe(values => {
            this.angabenForm.controls.schlusszahlung.setValue(
                parseFloat((values[0] - values[1]).toFixed(2))
            );
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    private parseFloatSafe(formValue: string): number {
        const unsafeParsed = parseFloat(formValue);
        return isNaN(unsafeParsed) ? 0 : unsafeParsed;
    }

    private lATSAngabenGemeindeFuerInstitutionenFreigeben(): void {
        this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst =
            this.formularInitForm.value.alleAngabenInKibonErfasst;
        this.lastenausgleichTSService.lATSAngabenGemeindeFuerInstitutionenFreigeben(this.lATSAngabenGemeindeContainer);
    }

    public showAntragErstellen(): boolean {
        return this.lATSAngabenGemeindeContainer?.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU;
    }

    public inMandantRoles(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public onAngabenFormSubmit(): void {
        this.resetBasicValidation();
        if (!this.angabenForm.valid) {
            this.errorService.addMesageAsError(
                this.translateService.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN')
            );
            return;
        }
        // eslint-disable-next-line max-len
        if (this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKantonOrZurueckgegeben()) {
            this.lATSAngabenGemeindeContainer.angabenKorrektur = new TSLastenausgleichTagesschuleAngabenGemeinde();
            Object.assign(this.lATSAngabenGemeindeContainer.angabenKorrektur, this.angabenForm.getRawValue());
        } else {
            this.lATSAngabenGemeindeContainer.angabenDeklaration = new TSLastenausgleichTagesschuleAngabenGemeinde();
            Object.assign(this.lATSAngabenGemeindeContainer.angabenDeklaration, this.angabenForm.getRawValue());
        }
        this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst =
            this.formularInitForm.value.alleAngabenInKibonErfasst;
        this.lastenausgleichTSService.saveLATSAngabenGemeindeContainer(this.lATSAngabenGemeindeContainer);
        this.angabenForm.markAsPristine();
        this.unsavedChangesService.registerForm(this.angabenForm);
    }

    public async onAbschliessen(): Promise<void> {
        this.enableAndTriggerFormValidation();

        if (!this.angabenForm.valid) {
            this.errorService.addMesageAsError(
                this.translateService.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN')
            );
            return;
        }
        if (!await this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN')) {
            return;
        }

        // eslint-disable-next-line max-len
        if (this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKantonOrZurueckgegeben()) {
            this.lATSAngabenGemeindeContainer.angabenKorrektur = new TSLastenausgleichTagesschuleAngabenGemeinde();
            Object.assign(this.lATSAngabenGemeindeContainer.angabenKorrektur, this.angabenForm.getRawValue());
        } else {
            this.lATSAngabenGemeindeContainer.angabenDeklaration = new TSLastenausgleichTagesschuleAngabenGemeinde();
            Object.assign(this.lATSAngabenGemeindeContainer.angabenDeklaration, this.angabenForm.getRawValue());
        }
        this.errorService.clearAll();
        this.lastenausgleichTSService.latsAngabenGemeindeFormularAbschliessen(this.lATSAngabenGemeindeContainer)
            .subscribe(container => this.handleSaveSuccess(container),
                err => this.handleSaveError(err));
    }

    private confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translateService.instant(frageKey)
        };
        return this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
    }

    private handleSaveSuccess(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        if (container.isInBearbeitungGemeinde() && container.angabenDeklaration.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.VALIDIERUNG_FEHLGESCHLAGEN ||
            container.isAtLeastInBearbeitungKanton() && container.angabenKorrektur.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.VALIDIERUNG_FEHLGESCHLAGEN) {
            this.enableAndTriggerFormValidation();
            this.errorService.addMesageAsError(this.translateService.instant(
                'LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'));
            this.angabenForm.markAsPristine();
            this.unsavedChangesService.registerForm(this.angabenForm);
            return;
        }

        this.errorService.clearAll();

        this.dialog.open(DvNgOkDialogComponent, {
            data: {
                title: this.translateService.instant(container.isInBearbeitungGemeinde() ?
                    'LATS_FREIGABE_REMINDER' :
                    'LATS_FREIGABE_REMINDER_KANTON')
            }
        }).afterClosed().subscribe(confirmation => {
            if (confirmation) {
                this.unsavedChangesService.unregisterForm();
                this.$state.go('LASTENAUSGLEICH_TAGESSCHULEN.FREIGABE');
            }
        }, () => {
            this.errorService.addMesageAsInfo(this.translateService.instant('ERROR_UNEXPECTED'));
        });
        this.cd.markForCheck();
        this.wizardRS.updateSteps(this.WIZARD_TYPE, this.uiRouterGlobals.params.id);
    }

    private handleSaveError(errors: TSExceptionReport[]): void {
        errors.forEach(error => {
            if (error.customMessage.includes('institution')) {
                this.errorService.addMesageAsError(this.translateService.instant(
                    'LATS_NICHT_ALLE_INSTITUTIONEN_ABGESCHLOSSEN'));
            } else if (error.customMessage.includes('incomplete')) {
                this.errorService.addMesageAsError(this.translateService.instant(
                    'LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'));
            }
        });
    }

    public enableAndTriggerFormValidation(): void {
        this.abschliessenValidationActive = true;
        this.enableFormValidation();
        this.triggerFormValidation();
    }

    private triggerFormValidation(): void {
        for (const key in this.angabenForm.controls) {
            if (this.angabenForm.get(key) !== null) {
                this.angabenForm.get(key).markAsTouched();
                this.angabenForm.get(key).updateValueAndValidity();
            }
        }
        this.angabenForm.updateValueAndValidity();
    }

    /**
     * Begruendung value should be deleted when hidden
     */
    public deleteBegruendung($event: MatRadioChange): void {
        if ($event.value === true) {
            this.angabenForm.controls.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen.setValue(null);
        }
    }

    public inKantonPruefung(): boolean {
        return this.lATSAngabenGemeindeContainer.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON &&
            this.inMandantRoles();
    }

    public formularNotEditable(): boolean {
        return this.lATSAngabenGemeindeContainer.isInBearbeitungGemeinde() && // eslint-disable-next-line max-len
            this.isAbgeschlossen(this.lATSAngabenGemeindeContainer.angabenDeklaration.status)
            || this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOnlyRoles())
            && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton()
            || this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())
            && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton()
            && this.isAbgeschlossen(this.lATSAngabenGemeindeContainer.angabenKorrektur.status);
    }

    private isAbgeschlossen(status: TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus): boolean {
        return status === TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
    }

    public onFalscheAngaben(): void {
        this.lastenausgleichTSService.falscheAngaben(this.lATSAngabenGemeindeContainer);
    }

    public getLastYear(): number {
        return this.lATSAngabenGemeindeContainer?.gesuchsperiode?.getBasisJahrPlus1();
    }

    public getNextYear(): number {
        return this.lATSAngabenGemeindeContainer?.gesuchsperiode?.getBasisJahrPlus2();
    }

    private resetBasicValidation(): void {
        // A
        this.angabenForm.controls.angebotVerfuegbarFuerAlleSchulstufen.clearValidators();
        this.angabenForm.controls.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen.clearValidators();
        this.angabenForm.controls.bedarfBeiElternAbgeklaert.clearValidators();
        this.angabenForm.controls.angebotFuerFerienbetreuungVorhanden.clearValidators();
        this.angabenForm.controls.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen.clearValidators();
        this.angabenForm.controls.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen.clearValidators();

        // B
        this.angabenForm.controls.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.controls.geleisteteBetreuungsstundenBesondereBeduerfnisse
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.controls.geleisteteBetreuungsstundenBesondereVolksschulangebot
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.controls.davonStundenZuNormlohnMehrAls50ProzentAusgebildete
            .setValidators([
                numberValidator(ValidationType.POSITIVE_INTEGER)
            ]);
        this.angabenForm.controls.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete
            .setValidators([
                numberValidator(ValidationType.POSITIVE_INTEGER)
            ]);
        this.angabenForm.controls.einnahmenElterngebuehren
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.tagesschuleTeilweiseGeschlossen.clearValidators();
        this.angabenForm.controls.rueckerstattungenElterngebuehrenSchliessung
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.lastenausgleichberechtigteBetreuungsstunden.clearValidators();
        this.angabenForm.controls.ersteRateAusbezahlt
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // eslint-disable-next-line
        this.angabenForm.controls.tagesschuleTeilweiseGeschlossen.valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.controls.rueckerstattungenElterngebuehrenSchliessung
                    .setValidators([
                        this.numberValidator(),
                        Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                    ]);
            } else {
                this.angabenForm.controls.rueckerstattungenElterngebuehrenSchliessung
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // C
        this.angabenForm.controls.gesamtKostenTagesschule
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.einnnahmenVerpflegung
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.einnahmenSubventionenDritter
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.controls.ueberschussErzielt.clearValidators();
        this.angabenForm.controls.ueberschussVerwendung.clearValidators();

        // E
        this.angabenForm.controls.betreuungsstundenDokumentiertUndUeberprueft.clearValidators();
        this.angabenForm.controls.betreuungsstundenDokumentiertUndUeberprueftBemerkung.clearValidators();
        this.angabenForm.controls.elterngebuehrenGemaessVerordnungBerechnet.clearValidators();
        this.angabenForm.controls.elterngebuehrenGemaessVerordnungBerechnetBemerkung.clearValidators();
        this.angabenForm.controls.einkommenElternBelegt.clearValidators();
        this.angabenForm.controls.einkommenElternBelegtBemerkung.clearValidators();
        this.angabenForm.controls.maximalTarif.clearValidators();
        this.angabenForm.controls.maximalTarifBemerkung.clearValidators();
        this.angabenForm.controls.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal.clearValidators();
        this.angabenForm.controls.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung.clearValidators();
        this.angabenForm.controls.ausbildungenMitarbeitendeBelegt.clearValidators();
        this.angabenForm.controls.ausbildungenMitarbeitendeBelegtBemerkung.clearValidators();

        this.triggerFormValidation();
    }

    private allInstitutionsGeprueft(): ValidatorFn {
        return () => this.lATSAngabenGemeindeContainer?.allAngabenInstitutionContainersGeprueft() ? null : {
                notAllInstitutionsGeprueft: true
            };
    }

    // eslint-disable-next-line
    private setupPermissions(
        container: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
        principal: TSBenutzer
    ): void {
        if (container.isAtLeastGeprueft()) {
            this.saveVisible.next(false);
            this.abschliessenVisible.next(false);
            this.falscheAngabenVisible.next(false);
            return;
        }
        if (principal.hasRole(TSRole.SUPER_ADMIN)) {
            const angaben = container.isInBearbeitungGemeinde() ?
                container.angabenDeklaration :
                container.angabenKorrektur;
            if (angaben.isInBearbeitung()) {
                this.saveVisible.next(true);
                this.abschliessenVisible.next(container.allAngabenInstitutionContainersGeprueft());
                this.falscheAngabenVisible.next(false);
            } else {
                this.saveVisible.next(false);
                this.abschliessenVisible.next(false);
                this.falscheAngabenVisible.next(true);
            }
        }
        if (principal.hasOneOfRoles(TSRoleUtil.getMandantOnlyRoles())) {
            if (container.isInBearbeitungGemeinde()) {
                this.saveVisible.next(false);
                this.abschliessenVisible.next(false);
                this.falscheAngabenVisible.next(false);
            } else {
                this.saveVisible.next(container.angabenKorrektur.isInBearbeitung());
                this.abschliessenVisible.next(container.angabenKorrektur.isInBearbeitung());
                this.falscheAngabenVisible.next(!container.angabenKorrektur.isInBearbeitung());
            }
        }
        if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles())) {
            if (container.isInBearbeitungGemeinde()) {
                this.saveVisible.next(container.angabenDeklaration.isInBearbeitung());
                this.abschliessenVisible.next
                (container.angabenDeklaration.isInBearbeitung() && container.allAngabenInstitutionContainersGeprueft());
                this.falscheAngabenVisible.next(!container.angabenDeklaration.isInBearbeitung());
            } else {
                this.saveVisible.next(false);
                this.abschliessenVisible.next(false);
                this.falscheAngabenVisible.next(false);
            }
        }
    }

    public controllingActive(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())
            && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton()
            && EbeguUtil.isNotNullOrUndefined(this.controllingCalculator);
    }

    public showCornonaFrage(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.angabenForm.value.tagesschuleTeilweiseGeschlossen);
    }

    private initControlling(): void {
        combineLatest([
            this.lastenausgleichTSService.findAntragOfPreviousPeriode(this.lATSAngabenGemeindeContainer),
            this.lastenausgleichTSService.getErwarteteBetreuungsstunden(this.lATSAngabenGemeindeContainer)
        ]).subscribe(results => {
            this.previousAntrag = results[0];
            this.erwarteteBetreuungsstunden = results[1];
            this.controllingCalculator = new TSControllingCalculator(this.angabenForm, results[0]);
            this.calculateStarkeVeraenderung();
            this.cd.markForCheck();
        }, err => {
            LOG.error(err);
            console.error(err);
        });
    }

    public clearFormFieldOnChangeToTrue(event: MatRadioChange, formFieldToClear: string): void {
        if (event.value !== true) {
            return;
        }

        this.angabenForm.get(formFieldToClear).setValue(undefined);
    }

    private calculateStarkeVeraenderung(): void {
        const starkeVeraenderungAb = 0.2; //veranderung Betreuungsstunden +/- 20% = Starke Veranderung
        this.controllingCalculator.veraenderungBetreuungsstundenAsNumber$
            .subscribe(value => {
                this.hasStarkeVeraenderung = Math.abs(value) >= starkeVeraenderungAb;
                this.cd.markForCheck();
            });
    }

    public initFormAnswered(): boolean {
        return this.lATSAngabenGemeindeContainer.status !== TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU;
    }
}
