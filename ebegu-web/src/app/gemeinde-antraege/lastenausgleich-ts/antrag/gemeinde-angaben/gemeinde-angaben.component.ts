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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnDestroy,
    OnInit,
    ViewEncapsulation
} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatRadioChange} from '@angular/material/radio';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {BehaviorSubject, combineLatest, Subject, Subscription} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus
} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeStatus
} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSWizardStepXTyp} from '../../../../../models/enums/TSWizardStepXTyp';
import {
    TSLastenausgleichTagesschuleAngabenGemeinde
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {
    TSLastenausgleichTagesschuleAngabenGemeindeContainer
} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSEinstellung} from '../../../../../models/TSEinstellung';
import {TSExceptionReport} from '../../../../../models/TSExceptionReport';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {MathUtil} from '../../../../../utils/MathUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {
    DvNgConfirmDialogComponent
} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
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

    public angabenForm: FormGroup;
    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public formularInitForm: FormGroup;
    private subscription: Subscription;
    public abschliessenValidationActive = false;
    public lohnnormkostenSettingMoreThanFifty$: Subject<TSEinstellung> = new Subject<TSEinstellung>();
    public lohnnormkostenSettingLessThanFifty$: Subject<TSEinstellung> = new Subject<TSEinstellung>();

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
        if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles()
            .concat(TSRole.SUPER_ADMIN)) && container.isInBearbeitungGemeinde() && container.angabenDeklaration.isInBearbeitung()) {
            this.formularInitForm.enable();
        } else {
            this.formularInitForm.disable();
        }
    }

    private setupForm(initialGemeindeAngaben: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        this.angabenForm = this.fb.group({
            status: initialGemeindeAngaben?.status,
            version: initialGemeindeAngaben?.version,
            // A
            alleFaelleInKibon: [this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst],
            angebotVerfuegbarFuerAlleSchulstufen: [
                initialGemeindeAngaben?.angebotVerfuegbarFuerAlleSchulstufen
            ],
            begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen:
                [initialGemeindeAngaben?.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen],
            bedarfBeiElternAbgeklaert: [
                initialGemeindeAngaben?.bedarfBeiElternAbgeklaert
            ],
            angebotFuerFerienbetreuungVorhanden: [
                initialGemeindeAngaben?.angebotFuerFerienbetreuungVorhanden
            ],
            // B
            geleisteteBetreuungsstundenOhneBesondereBeduerfnisse:
                [
                    initialGemeindeAngaben?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse,
                    numberValidator(ValidationType.POSITIVE_INTEGER)
                ],
            geleisteteBetreuungsstundenBesondereBeduerfnisse:
                [
                    initialGemeindeAngaben?.geleisteteBetreuungsstundenBesondereBeduerfnisse,
                    numberValidator(ValidationType.POSITIVE_INTEGER)
                ],
            geleisteteBetreuungsstundenBesondereVolksschulangebot:
                [
                    initialGemeindeAngaben?.geleisteteBetreuungsstundenBesondereVolksschulangebot,
                    numberValidator(ValidationType.POSITIVE_INTEGER)
                ],
            davonStundenZuNormlohnMehrAls50ProzentAusgebildete:
                [
                    initialGemeindeAngaben?.davonStundenZuNormlohnMehrAls50ProzentAusgebildete,
                    numberValidator(ValidationType.POSITIVE_INTEGER)
                ],
            davonStundenZuNormlohnWenigerAls50ProzentAusgebildete:
                [
                    initialGemeindeAngaben?.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete,
                    numberValidator(ValidationType.POSITIVE_INTEGER)
                ],
            einnahmenElterngebuehren: [
                initialGemeindeAngaben?.einnahmenElterngebuehren, Validators.compose([
                    this.numberValidator(),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ])
            ],
            einnahmenElterngebuehrenVolksschulangebot: [
                initialGemeindeAngaben?.einnahmenElterngebuehrenVolksschulangebot, Validators.compose([
                    this.numberValidator(),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ])
            ],
            ersteRateAusbezahlt: [
                initialGemeindeAngaben?.ersteRateAusbezahlt,
                numberValidator(ValidationType.POSITIVE_INTEGER)
            ],
            tagesschuleTeilweiseGeschlossen: [initialGemeindeAngaben?.tagesschuleTeilweiseGeschlossen],
            rueckerstattungenElterngebuehrenSchliessung: [
                initialGemeindeAngaben?.rueckerstattungenElterngebuehrenSchliessung,
                Validators.compose([
                    this.numberValidator(),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ])
            ],
            // C
            gesamtKostenTagesschule: [
                initialGemeindeAngaben?.gesamtKostenTagesschule, Validators.compose([
                    this.numberValidator(),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ])
            ],
            einnnahmenVerpflegung: [
                initialGemeindeAngaben?.einnnahmenVerpflegung, Validators.compose([
                    this.numberValidator(),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ])
            ],
            einnahmenSubventionenDritter: [
                initialGemeindeAngaben?.einnahmenSubventionenDritter,
                Validators.compose([
                    this.numberValidator(),
                    Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                ])
            ],
            ueberschussErzielt: [initialGemeindeAngaben?.ueberschussErzielt],
            ueberschussVerwendung: [initialGemeindeAngaben?.ueberschussVerwendung],
            // D
            bemerkungenWeitereKostenUndErtraege: [initialGemeindeAngaben?.bemerkungenWeitereKostenUndErtraege],
            // E
            betreuungsstundenDokumentiertUndUeberprueft:
                [initialGemeindeAngaben?.betreuungsstundenDokumentiertUndUeberprueft],
            betreuungsstundenDokumentiertUndUeberprueftBemerkung:
                [initialGemeindeAngaben?.betreuungsstundenDokumentiertUndUeberprueftBemerkung],
            elterngebuehrenGemaessVerordnungBerechnet:
                [initialGemeindeAngaben?.elterngebuehrenGemaessVerordnungBerechnet],
            elterngebuehrenGemaessVerordnungBerechnetBemerkung:
                [initialGemeindeAngaben?.elterngebuehrenGemaessVerordnungBerechnetBemerkung],
            einkommenElternBelegt: [initialGemeindeAngaben?.einkommenElternBelegt],
            einkommenElternBelegtBemerkung: [initialGemeindeAngaben?.einkommenElternBelegtBemerkung],
            maximalTarif: [initialGemeindeAngaben?.maximalTarif],
            maximalTarifBemerkung: [initialGemeindeAngaben?.maximalTarifBemerkung],
            mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal:
                [
                    initialGemeindeAngaben?.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal
                ],
            mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung:
                [
                    initialGemeindeAngaben?.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung
                ],
            ausbildungenMitarbeitendeBelegt: [
                {value: initialGemeindeAngaben?.ausbildungenMitarbeitendeBelegt, disabled: false}
            ],
            ausbildungenMitarbeitendeBelegtBemerkung: [
                {value: initialGemeindeAngaben?.ausbildungenMitarbeitendeBelegtBemerkung, disabled: false}
            ],
            // Bemerkungen
            bemerkungen: [initialGemeindeAngaben?.bemerkungen],
            bemerkungStarkeVeraenderung: [initialGemeindeAngaben?.bemerkungStarkeVeraenderung],
            // calculated values
            lastenausgleichberechtigteBetreuungsstunden: [{value: ''}],
            davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet: [{value: '', disabled: true}],
            davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet: [{value: '', disabled: true}],
            normlohnkostenBetreuungBerechnet: [{value: '', disabled: true}],
            lastenausgleichsberechtigerBetrag: [{value: '', disabled: true}],
            lastenausgleichsberechtigerBetragRO: [{value: '', disabled: true}],
            einnahmenElterngebuehrenRO: [{value: '', disabled: true}],
            kostenbeitragGemeinde: [{value: '', disabled: true}],
            kostenueberschussGemeinde: [{value: '', disabled: true}],
            erwarteterKostenbeitragGemeinde: [{value: '', disabled: true}],
            schlusszahlung: [{value: '', disabled: true}]
        });

        if (!this.lATSAngabenGemeindeContainer.isGemeindeFormularInBearbeitungForRole(this.authServiceRS.getPrincipalRole())) {
            this.angabenForm.disable();
        }
    }

    private enableFormValidation(): void {
        // A
        this.angabenForm.get('angebotVerfuegbarFuerAlleSchulstufen').setValidators([Validators.required]);
        this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen')
            .setValidators([Validators.required]);
        this.angabenForm.get('bedarfBeiElternAbgeklaert').setValidators([Validators.required]);
        this.angabenForm.get('angebotFuerFerienbetreuungVorhanden').setValidators([Validators.required]);

        this.angabenForm.get('angebotVerfuegbarFuerAlleSchulstufen').valueChanges.subscribe(value => {
            this.setValidatorRequiredIfFalse('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen', value);
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // B
        this.angabenForm.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereVolksschulangebot')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .setValidators([
                Validators.required,
                numberValidator(ValidationType.POSITIVE_INTEGER),
                this.plausibilisierungAddition()
            ]);
        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .setValidators([
                Validators.required,
                numberValidator(ValidationType.POSITIVE_INTEGER),
                this.plausibilisierungAddition()
            ]);
        this.angabenForm.get('einnahmenElterngebuehren')
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        if (this.showCornonaFrage()) {
            this.angabenForm.get('tagesschuleTeilweiseGeschlossen')
                .setValidators([Validators.required]);
        }
        this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
            .setValidators([
                this.plausibilisierungTageschulenStunden(),
                this.allInstitutionsGeprueft()
            ]);
        this.angabenForm.get('ersteRateAusbezahlt')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // eslint-disable-next-line
        this.angabenForm.get('tagesschuleTeilweiseGeschlossen').valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
                    .setValidators([Validators.required, this.numberValidator()]);
            } else {
                this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // C
        this.angabenForm.get('gesamtKostenTagesschule')
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('einnnahmenVerpflegung')
            .setValidators([
                Validators.required, this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('einnahmenSubventionenDritter')
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('ueberschussErzielt')
            .setValidators([Validators.required]);
        // eslint-disable-next-line
        this.angabenForm.get('ueberschussErzielt').valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.get('ueberschussVerwendung')
                    .setValidators([Validators.required]);
            } else {
                this.angabenForm.get('ueberschussVerwendung')
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // E
        this.angabenForm.get('betreuungsstundenDokumentiertUndUeberprueft').setValidators([Validators.required]);
        this.angabenForm.get('betreuungsstundenDokumentiertUndUeberprueft').valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('betreuungsstundenDokumentiertUndUeberprueftBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'betreuungsstundenDokumentiertUndUeberprueftBemerkung Subscribe Error')));

        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnet').setValidators([Validators.required]);
        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnet').valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('elterngebuehrenGemaessVerordnungBerechnetBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'elterngebuehrenGemaessVerordnungBerechnetBemerkung ValueChanges Error')));

        this.angabenForm.get('einkommenElternBelegt').setValidators([Validators.required]);
        this.angabenForm.get('einkommenElternBelegt').valueChanges.subscribe(value => {
                this.setValidatorRequiredIfFalse('einkommenElternBelegtBemerkung', value);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant(
                'einkommenElternBelegtBemerkung ValueChanges error')));

        this.angabenForm.get('maximalTarif').setValidators([Validators.required]);
        this.angabenForm.get('maximalTarif').valueChanges.subscribe(value => {
            this.setValidatorRequiredIfFalse('maximalTarifBemerkung', value);
        }, () => this.errorService.addMesageAsError(this.translateService.instant('Maximal Tarif ValueChanges error')));

        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal')
            .setValidators([Validators.required]);
        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal')
            .valueChanges
            .subscribe(value => {
                    this.setValidatorRequiredIfFalse('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung',
                        value);
                },
                () => this.errorService.addMesageAsError(this.translateService.instant(
                    'mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal ValueChanges error')));

        this.angabenForm.get('ausbildungenMitarbeitendeBelegt').setValidators([Validators.required]);
        this.angabenForm.get('ausbildungenMitarbeitendeBelegt').valueChanges.subscribe(value => {
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
        return control => parseFloat(this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').value) ===
            MathUtil.addFloatPrecisionSafe(
                parseFloat(this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete').value),
                parseFloat(this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').value)
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

            return this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').value === tagesschulenSum ?
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
                this.angabenForm.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse)
                ),
                this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenBesondereBeduerfnisse)
                ),
                this.angabenForm.get('geleisteteBetreuungsstundenBesondereVolksschulangebot').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenBesondereVolksschulangebot)
                )
            ]
        ).subscribe(formValues => {
            this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
                .setValue(
                    MathUtil.addArrayFloatPrecisionSafe(formValues[0] || 0,
                        [formValues[1] || 0, formValues[2] || 0]),
                );
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete').updateValueAndValidity();
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').updateValueAndValidity();
            this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
                .updateValueAndValidity({emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // eslint-disable-next-line max-len
    private setupStundenNormlohnMehr50ProzentCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').valueChanges.pipe(
                startWith(gemeindeAngabenFromServer?.davonStundenZuNormlohnMehrAls50ProzentAusgebildete),
                map(value => this.parseFloatSafe(value))
            ),
            this.lohnnormkostenSettingMoreThanFifty$
        ]).subscribe(valueAndParameter => {
            const value = valueAndParameter[0];
            const lohnkostenParam = parseFloat(valueAndParameter[1].value);
            const roundedValue = (value && lohnkostenParam) ? (value * lohnkostenParam).toFixed(2) : 0;
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                .setValue(roundedValue);
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
                .updateValueAndValidity({onlySelf: true, emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    private setValidatorRequiredIfFalse(fieldname: string, value: boolean): void {
        if (!value) {
            this.angabenForm.get(fieldname).setValidators([Validators.required]);
        } else {
            this.angabenForm.get(fieldname).setValidators(null);
        }
    }

    // eslint-disable-next-line max-len
    private setupStundenWeniger50ProzentCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete').valueChanges.pipe(
                startWith(gemeindeAngabenFromServer?.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete)
            ),
            this.lohnnormkostenSettingLessThanFifty$
        ]).subscribe(valueAndParamter => {
            const value = valueAndParamter[0];
            const lohnkostenParam = parseFloat(valueAndParamter[1].value);
            const roundedValue = (value && lohnkostenParam) ? (value * lohnkostenParam).toFixed(2) : 0;
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                .setValue(roundedValue);
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
                .updateValueAndValidity({onlySelf: true, emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // eslint-disable-next-line max-len
    private setupLastenausgleichsberechtigterBetragCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('normlohnkostenBetreuungBerechnet')
                .valueChanges
                .pipe(
                    startWith(0),
                    map(value => this.parseFloatSafe(value))
                ),
            this.angabenForm.get('einnahmenElterngebuehren')
                .valueChanges
                .pipe(
                    startWith(gemeindeAngabenFromServer?.einnahmenElterngebuehren || 0),
                    map(value => this.parseFloatSafe(value)),
                ),
        ]).subscribe(values => {
                const result = MathUtil.subtractFloatPrecisionSafe(values[0], values[1]);
                // round to next Franken
                const roundedResult = Math.ceil(result).toFixed(2);
                this.angabenForm.get('lastenausgleichsberechtigerBetrag').setValue(
                    roundedResult,
                );
                this.angabenForm.get('lastenausgleichsberechtigerBetragRO').setValue(
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
            this.angabenForm.get('gesamtKostenTagesschule')
                .valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.gesamtKostenTagesschule || 0),
                    map(value => this.parseFloatSafe(value))),
            this.angabenForm.get('lastenausgleichsberechtigerBetrag').valueChanges.pipe(startWith(0),
                map(value => this.parseFloatSafe(value))),
            this.angabenForm.get('einnahmenElterngebuehren')
                .valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnahmenElterngebuehren || 0),
                    map(value => this.parseFloatSafe(value))),
            this.angabenForm.get('einnnahmenVerpflegung')
                .valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnnahmenVerpflegung || 0),
                    map(value => this.parseFloatSafe(value))),
            this.angabenForm.get('einnahmenSubventionenDritter')
                .valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnahmenSubventionenDritter || 0))
        ]).subscribe(values => {
                const gemeindeBeitragOderUeberschuss = MathUtil.subtractArrayFloatPrecisionSafe(values[0],
                    values.slice(1, 4));
                if (+gemeindeBeitragOderUeberschuss < 0) {
                    this.angabenForm.get('kostenueberschussGemeinde')
                        .setValue(gemeindeBeitragOderUeberschuss);
                    this.angabenForm.get('kostenbeitragGemeinde')
                        .setValue('');
                } else {
                    this.angabenForm.get('kostenbeitragGemeinde')
                        .setValue(gemeindeBeitragOderUeberschuss);
                    this.angabenForm.get('kostenueberschussGemeinde')
                        .setValue('');
                }

                this.angabenForm.get('erwarteterKostenbeitragGemeinde')
                    .setValue((values[0] * this.kostenbeitragGemeinde).toFixed(2));
                this.angabenForm.get('einnahmenElterngebuehrenRO').setValue(values[2].toFixed(2));
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR'))
        );
    }

    private setupNormlohnkostenBetreuungBerechnetCalculations(): void {
        combineLatest(
            [
                this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0), map (formValue => parseFloat(formValue))),
                this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0), map (formValue => parseFloat(formValue)))
            ]
        ).subscribe(value => {
                const normlohnkostenExact = MathUtil.addFloatPrecisionSafe(value[0], value[1]);
                const normlohnkostenRounded = EbeguUtil.ceilToFiveRappen(normlohnkostenExact);
                this.angabenForm.get('normlohnkostenBetreuungBerechnet')
                    .setValue(normlohnkostenRounded.toFixed(2));
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR'))
        );
    }

    // eslint-disable-next-line max-len
    private setupSchlusszahlungenCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('lastenausgleichsberechtigerBetrag')
                .valueChanges
                .pipe(
                    startWith(0),
                    map(value => this.parseFloatSafe(value))
                ),
            this.angabenForm.get('ersteRateAusbezahlt')
                .valueChanges
                .pipe(
                    startWith(gemeindeAngabenFromServer?.ersteRateAusbezahlt || 0),
                    map(value => this.parseFloatSafe(value))
                )
            // eslint-disable-next-line
        ]).subscribe(values => {
            this.angabenForm.get('schlusszahlung').setValue(
                (values[0] - values[1]).toFixed(2)
            );
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    private parseFloatSafe(formValue: string): number {
        const unsafeParsed = parseFloat(formValue);
        return isNaN(unsafeParsed) ? 0 : unsafeParsed;
    }

    private lATSAngabenGemeindeFuerInstitutionenFreigeben(): void {
        this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst =
            this.formularInitForm.get('alleAngabenInKibonErfasst').value;
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
            Object.assign(this.lATSAngabenGemeindeContainer.angabenKorrektur = this.angabenForm.getRawValue());
        } else {
            this.lATSAngabenGemeindeContainer.angabenDeklaration = new TSLastenausgleichTagesschuleAngabenGemeinde();
            Object.assign(this.lATSAngabenGemeindeContainer.angabenDeklaration, this.angabenForm.getRawValue());
        }
        this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst =
            this.formularInitForm.get('alleAngabenInKibonErfasst').value;
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
            Object.assign(this.lATSAngabenGemeindeContainer.angabenKorrektur = this.angabenForm.getRawValue());
        } else {
            this.lATSAngabenGemeindeContainer.angabenDeklaration = new TSLastenausgleichTagesschuleAngabenGemeinde();
            Object.assign(this.lATSAngabenGemeindeContainer.angabenDeklaration = this.angabenForm.getRawValue());
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
            this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen').setValue(null);
        }
    }

    public inKantonPruefung(): boolean {
        return this.lATSAngabenGemeindeContainer.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON &&
            this.inMandantRoles();
    }

    public formularNotEditable(): boolean {
        return this.lATSAngabenGemeindeContainer.isInBearbeitungGemeinde() && // eslint-disable-next-line max-len
            this.lATSAngabenGemeindeContainer.angabenDeklaration.status === TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN ||
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOnlyRoles()) && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton() ||
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton() && // eslint-disable-next-line max-len
            this.lATSAngabenGemeindeContainer.angabenKorrektur.status === TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
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
        this.angabenForm.get('angebotVerfuegbarFuerAlleSchulstufen').clearValidators();
        this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen').clearValidators();
        this.angabenForm.get('bedarfBeiElternAbgeklaert').clearValidators();
        this.angabenForm.get('angebotFuerFerienbetreuungVorhanden').clearValidators();
        this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen').clearValidators();
        this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen').clearValidators();

        // B
        this.angabenForm.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse')
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse')
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereVolksschulangebot')
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .setValidators([
                numberValidator(ValidationType.POSITIVE_INTEGER)
            ]);
        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .setValidators([
                numberValidator(ValidationType.POSITIVE_INTEGER)
            ]);
        this.angabenForm.get('einnahmenElterngebuehren')
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('tagesschuleTeilweiseGeschlossen').clearValidators();
        this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').clearValidators();
        this.angabenForm.get('ersteRateAusbezahlt')
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // eslint-disable-next-line
        this.angabenForm.get('tagesschuleTeilweiseGeschlossen').valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
                    .setValidators([
                        this.numberValidator(),
                        Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
                    ]);
            } else {
                this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // C
        this.angabenForm.get('gesamtKostenTagesschule')
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('einnnahmenVerpflegung')
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('einnahmenSubventionenDritter')
            .setValidators([
                this.numberValidator(),
                Validators.pattern(CONSTANTS.PATTERN_TWO_DECIMALS)
            ]);
        this.angabenForm.get('ueberschussErzielt').clearValidators();
        this.angabenForm.get('ueberschussVerwendung').clearValidators();

        // E
        this.angabenForm.get('betreuungsstundenDokumentiertUndUeberprueft').clearValidators();
        this.angabenForm.get('betreuungsstundenDokumentiertUndUeberprueftBemerkung').clearValidators();
        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnet').clearValidators();
        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnetBemerkung').clearValidators();
        this.angabenForm.get('einkommenElternBelegt').clearValidators();
        this.angabenForm.get('einkommenElternBelegtBemerkung').clearValidators();
        this.angabenForm.get('maximalTarif').clearValidators();
        this.angabenForm.get('maximalTarifBemerkung').clearValidators();
        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal').clearValidators();
        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung').clearValidators();
        this.angabenForm.get('ausbildungenMitarbeitendeBelegt').clearValidators();
        this.angabenForm.get('ausbildungenMitarbeitendeBelegtBemerkung').clearValidators();

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
        // eslint-disable-next-line
        if (principal.hasOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles())) {
            if (container.isInBearbeitungGemeinde()) {
                this.saveVisible.next(container.angabenDeklaration.isInBearbeitung());
                this.abschliessenVisible.next(container.angabenDeklaration.isInBearbeitung() && container.allAngabenInstitutionContainersGeprueft());
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
            && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton();
    }

    public showCornonaFrage(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.angabenForm.get('tagesschuleTeilweiseGeschlossen').value);
    }

    private initControlling(): void {
        combineLatest([
            this.lastenausgleichTSService.findAntragOfPreviousPeriode(this.lATSAngabenGemeindeContainer),
            this.lastenausgleichTSService.getErwarteteBetreuungsstunden(this.lATSAngabenGemeindeContainer)
        ]).subscribe(results => {
            this.previousAntrag = results[0];
            this.erwarteteBetreuungsstunden = results[1];
            this.controllingCalculator = new TSControllingCalculator(this.angabenForm, results[0]);
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
}
