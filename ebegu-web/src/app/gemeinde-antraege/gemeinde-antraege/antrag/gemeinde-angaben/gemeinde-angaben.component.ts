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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatRadioChange} from '@angular/material/radio';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Subject, Subscription} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSWizardStepXTyp} from '../../../../../models/enums/TSWizardStepXTyp';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSEinstellung} from '../../../../../models/TSEinstellung';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {HTTP_ERROR_CODES} from '../../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {WizardStepXRS} from '../../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../../shared/validators/number-validator.directive';
import {LastenausgleichTSService} from '../../../lastenausgleich-ts/services/lastenausgleich-ts.service';
import {GemeindeAntragService} from '../../../services/gemeinde-antrag.service';
import {UnsavedChangesService} from '../../../services/unsaved-changes.service';

@Component({
    selector: 'dv-gemeinde-angaben',
    templateUrl: './gemeinde-angaben.component.html',
    styleUrls: ['./gemeinde-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
})
export class GemeindeAngabenComponent implements OnInit {

    @Input() public lastenausgleichID: string;
    @Input() public triggerValidationOnInit = false;

    public angabenForm: FormGroup;
    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public formularInitForm: FormGroup;
    private subscription: Subscription;
    public abschliessenValidationActive = false;
    public lohnnormkostenSettingMoreThanFifty$: Subject<TSEinstellung> = new Subject<TSEinstellung>();
    public lohnnormkostenSettingLessThanFifty$: Subject<TSEinstellung> = new Subject<TSEinstellung>();

    private readonly kostenbeitragGemeinde = 0.2;
    private readonly WIZARD_TYPE: TSWizardStepXTyp = TSWizardStepXTyp.LASTENAUSGLEICH_TAGESSCHULEN;

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
        private readonly unsavedChangesService: UnsavedChangesService
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer()
            .subscribe(container => {
                this.lATSAngabenGemeindeContainer = container;
                if (this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst !== null) {
                    const gemeindeAngaben = container.getAngabenToWorkWith();
                    this.setupForm(gemeindeAngaben);
                    this.setupCalculcations(gemeindeAngaben);
                }
                this.initLATSGemeindeInitializationForm();
                this.settings.findEinstellung(TSEinstellungKey.LATS_LOHNNORMKOSTEN,
                    this.lATSAngabenGemeindeContainer.gemeinde?.id,
                    this.lATSAngabenGemeindeContainer.gesuchsperiode?.id)
                    .then(setting => this.lohnnormkostenSettingMoreThanFifty$.next(setting));
                this.settings.findEinstellung(TSEinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50,
                    this.lATSAngabenGemeindeContainer.gemeinde?.id,
                    this.lATSAngabenGemeindeContainer.gesuchsperiode?.id)
                    .then(setting => this.lohnnormkostenSettingLessThanFifty$.next(setting));
                this.unsavedChangesService.registerForm(this.angabenForm);
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

    private initLATSGemeindeInitializationForm(): void {
        if (this.formularInitForm) {
            this.formularInitForm.patchValue({
                alleAngabenInKibonErfasst: this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst,
            });
        } else {
            this.formularInitForm = this.fb.group({
                alleAngabenInKibonErfasst: [
                    this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst,
                    Validators.required
                ],
            });
        }
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
            this.formularInitForm.disable();
        }
    }

    private setupForm(initialGemeindeAngaben: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        this.angabenForm = this.fb.group({
            status: initialGemeindeAngaben.status,
            // A
            alleFaelleInKibon: [this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst],
            angebotVerfuegbarFuerAlleSchulstufen: [
                initialGemeindeAngaben?.angebotVerfuegbarFuerAlleSchulstufen,
            ],
            begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen:
                [initialGemeindeAngaben?.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen],
            bedarfBeiElternAbgeklaert: [
                initialGemeindeAngaben?.bedarfBeiElternAbgeklaert,
            ],
            angebotFuerFerienbetreuungVorhanden: [
                initialGemeindeAngaben?.angebotFuerFerienbetreuungVorhanden,
            ],
            // B
            geleisteteBetreuungsstundenOhneBesondereBeduerfnisse:
                [
                    initialGemeindeAngaben?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse,
                    numberValidator(ValidationType.POSITIVE_INTEGER),
                ],
            geleisteteBetreuungsstundenBesondereBeduerfnisse:
                [
                    initialGemeindeAngaben?.geleisteteBetreuungsstundenBesondereBeduerfnisse,
                    numberValidator(ValidationType.POSITIVE_INTEGER),
                ],
            davonStundenZuNormlohnMehrAls50ProzentAusgebildete:
                [
                    initialGemeindeAngaben?.davonStundenZuNormlohnMehrAls50ProzentAusgebildete,
                    numberValidator(ValidationType.POSITIVE_INTEGER),
                ],
            davonStundenZuNormlohnWenigerAls50ProzentAusgebildete:
                [
                    initialGemeindeAngaben?.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete,
                    numberValidator(ValidationType.POSITIVE_INTEGER),
                ],
            einnahmenElterngebuehren: [initialGemeindeAngaben?.einnahmenElterngebuehren, this.numberValidator()],
            ersteRateAusbezahlt: [
                initialGemeindeAngaben?.ersteRateAusbezahlt,
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ],
            tagesschuleTeilweiseGeschlossen: [initialGemeindeAngaben?.tagesschuleTeilweiseGeschlossen],
            rueckerstattungenElterngebuehrenSchliessung: [
                initialGemeindeAngaben?.rueckerstattungenElterngebuehrenSchliessung,
                this.numberValidator(),
            ],
            // C
            gesamtKostenTagesschule: [initialGemeindeAngaben?.gesamtKostenTagesschule, this.numberValidator()],
            einnnahmenVerpflegung: [initialGemeindeAngaben?.einnnahmenVerpflegung, this.numberValidator()],
            einnahmenSubventionenDritter: [
                initialGemeindeAngaben?.einnahmenSubventionenDritter,
                this.numberValidator(),
            ],
            ueberschussErzielt: [initialGemeindeAngaben?.ueberschussErzielt],
            ueberschussVerwendung: [initialGemeindeAngaben?.ueberschussVerwendung],
            // D
            bemerkungenWeitereKostenUndErtraege: [initialGemeindeAngaben?.bemerkungenWeitereKostenUndErtraege],
            // E
            betreuungsstundenDokumentiertUndUeberprueft:
                [initialGemeindeAngaben?.betreuungsstundenDokumentiertUndUeberprueft],
            elterngebuehrenGemaessVerordnungBerechnet:
                [initialGemeindeAngaben?.elterngebuehrenGemaessVerordnungBerechnet],
            einkommenElternBelegt: [initialGemeindeAngaben?.einkommenElternBelegt],
            maximalTarif: [initialGemeindeAngaben?.maximalTarif],
            mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal:
                [
                    initialGemeindeAngaben?.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal,
                ],
            ausbildungenMitarbeitendeBelegt: [
                {value: initialGemeindeAngaben?.ausbildungenMitarbeitendeBelegt, disabled: false},
            ],
            // Bemerkungen
            bemerkungen: [initialGemeindeAngaben?.bemerkungen],
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
            schlusszahlung: [{value: '', disabled: true}],
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
            if (value === false) {
                this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen')
                    .setValidators([Validators.required]);
            } else {
                this.angabenForm.get('begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen')
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // B
        this.angabenForm.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);
        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .setValidators([
                Validators.required,
                numberValidator(ValidationType.POSITIVE_INTEGER),
                this.plausibilisierungAddition(),
            ]);
        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .setValidators([
                Validators.required,
                numberValidator(ValidationType.POSITIVE_INTEGER),
                this.plausibilisierungAddition(),
            ]);
        this.angabenForm.get('einnahmenElterngebuehren')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('tagesschuleTeilweiseGeschlossen')
            .setValidators([Validators.required]);
        this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
            .setValidators([
                this.plausibilisierungTageschulenStunden(),
                this.allInstitutionsGeprueft(),
            ]);
        this.angabenForm.get('ersteRateAusbezahlt')
            .setValidators([Validators.required, numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // tslint:disable-next-line:no-identical-functions
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
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('einnnahmenVerpflegung')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('einnahmenSubventionenDritter')
            .setValidators([this.numberValidator()]);
        this.angabenForm.get('ueberschussErzielt')
            .setValidators([Validators.required]);
        // tslint:disable-next-line:no-identical-functions
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
        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnet').setValidators([Validators.required]);
        this.angabenForm.get('einkommenElternBelegt').setValidators([Validators.required]);
        this.angabenForm.get('maximalTarif').setValidators([Validators.required]);
        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal')
            .setValidators([Validators.required]);
        this.angabenForm.get('ausbildungenMitarbeitendeBelegt').setValidators([Validators.required]);
    }

    private numberValidator(): ValidatorFn {
        // tslint:disable-next-line:no-unnecessary-type-annotation
        return (control: AbstractControl): {} | null => {
            return isNaN(control.value) ? {
                noNumberError: control.value,
            } : null;
        };
    }

    private plausibilisierungAddition(): ValidatorFn {
        return control => {
            return parseFloat(this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').value) ===
            parseFloat(this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete').value) +
            parseFloat(this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').value) ? null : {
                plausibilisierungAdditionError: control.value,
            };
        };
    }

    private plausibilisierungTageschulenStunden(): ValidatorFn {
        return control => {
            const tagesschulenSum = this.lATSAngabenGemeindeContainer.angabenInstitutionContainers.reduce((
                accumulator,
                next,
                ) => accumulator + (next.isInBearbeitungInstitution() ?
                next.angabenDeklaration.betreuungsstundenEinschliesslichBesondereBeduerfnisse :
                next.angabenKorrektur.betreuungsstundenEinschliesslichBesondereBeduerfnisse),
                0);

            return this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').value === tagesschulenSum ?
                null :
                {
                    plausibilisierungTagesschulenStundenError: control.value,
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

    // tslint:disable-next-line:max-line-length
    private setupLastenausgleichberechtigteBetreuungsstundenCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest(
            [
                this.angabenForm.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse),
                ),
                this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer?.geleisteteBetreuungsstundenBesondereBeduerfnisse),
                ),
            ],
        ).subscribe(formValues => {
            this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
                .setValue(parseFloat(formValues[0] || 0) + parseFloat(formValues[1] || 0));
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete').updateValueAndValidity();
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').updateValueAndValidity();
            this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
                .updateValueAndValidity({emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // tslint:disable-next-line:max-line-length
    private setupStundenNormlohnMehr50ProzentCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').valueChanges.pipe(
                startWith(gemeindeAngabenFromServer?.davonStundenZuNormlohnMehrAls50ProzentAusgebildete),
            ),
            this.lohnnormkostenSettingMoreThanFifty$,
        ]).subscribe(valueAndParameter => {
            const value = valueAndParameter[0];
            const lohnkostenParam = parseFloat(valueAndParameter[1].value);
            const roundedValue = (value && lohnkostenParam) ? +(value * lohnkostenParam).toFixed(2) : 0;
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                .setValue(roundedValue);
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
                .updateValueAndValidity({onlySelf: true, emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // tslint:disable-next-line:max-line-length
    private setupStundenWeniger50ProzentCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete').valueChanges.pipe(
                startWith(gemeindeAngabenFromServer?.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete),
            ),
            this.lohnnormkostenSettingLessThanFifty$,
        ]).subscribe(valueAndParamter => {
            const value = valueAndParamter[0];
            const lohnkostenParam = parseFloat(valueAndParamter[1].value);
            const roundedValue = (value && lohnkostenParam) ? +(value * lohnkostenParam).toFixed(2) : 0;
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                .setValue(roundedValue);
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
                .updateValueAndValidity({onlySelf: true, emitEvent: false});
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));
    }

    // tslint:disable-next-line:max-line-length
    private setupLastenausgleichsberechtigterBetragCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('normlohnkostenBetreuungBerechnet').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('einnahmenElterngebuehren')
                .valueChanges
                .pipe(startWith(gemeindeAngabenFromServer?.einnahmenElterngebuehren || 0)),
        ]).subscribe(values => {
                this.angabenForm.get('lastenausgleichsberechtigerBetrag').setValue(
                    // round to 0.2
                    Math.round((values[0] - values[1])),
                );
                this.angabenForm.get('lastenausgleichsberechtigerBetragRO').setValue(
                    // round to 0.2
                    Math.round((values[0] - values[1])),
                );
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')),
        );
    }

    // tslint:disable-next-line:max-line-length
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
                .pipe(startWith(gemeindeAngabenFromServer?.einnahmenSubventionenDritter || 0)),
        ]).subscribe(values => {
                const gemeindeBeitragOderUeberschuss = values[0] - values[1] - values[2] - values[3] - values[4];
                if (gemeindeBeitragOderUeberschuss < 0) {
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

                this.angabenForm.get('erwarteterKostenbeitragGemeinde').setValue((values[0] * this.kostenbeitragGemeinde).toFixed(2));
                this.angabenForm.get('einnahmenElterngebuehrenRO').setValue(values[2]);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')),
        );
    }

    private setupNormlohnkostenBetreuungBerechnetCalculations(): void {
        combineLatest(
            [
                this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0)),
                this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0)),
            ],
        ).subscribe(value => {
                this.angabenForm.get('normlohnkostenBetreuungBerechnet')
                    .setValue(+(parseFloat(value[0] || 0) + parseFloat(value[1] || 0)).toFixed(2));
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')),
        );
    }

    // tslint:disable-next-line:max-line-length
    private setupSchlusszahlungenCalculations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        combineLatest([
            this.angabenForm.get('lastenausgleichsberechtigerBetrag').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('ersteRateAusbezahlt')
                .valueChanges
                .pipe(
                    startWith(gemeindeAngabenFromServer?.ersteRateAusbezahlt || 0),
                    map(value => this.parseFloatSafe(value)),
                ),
            // tslint:disable-next-line:no-identical-functions
        ]).subscribe(values => {
            this.angabenForm.get('schlusszahlung').setValue(
                +(values[0] - values[1]).toFixed(2),
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
                this.translateService.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
            );
            return;
        }
        // tslint:disable-next-line:max-line-length
        if (this.lATSAngabenGemeindeContainer.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON) {
            this.lATSAngabenGemeindeContainer.angabenKorrektur = this.angabenForm.value;
        } else {
            this.lATSAngabenGemeindeContainer.angabenDeklaration = this.angabenForm.value;
        }
        this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst = this.formularInitForm.get('alleAngabenInKibonErfasst').value;
        this.lastenausgleichTSService.saveLATSAngabenGemeindeContainer(this.lATSAngabenGemeindeContainer);
        this.angabenForm.markAsPristine();

    }

    public async onAbschliessen(): Promise<void> {
        this.enableAndTriggerFormValidation();

        if (!this.angabenForm.valid) {
            this.errorService.addMesageAsError(
                this.translateService.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
            );
            return;
        }
        if (!await this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN')) {
            return;
        }

        // tslint:disable-next-line:max-line-length
        if (this.lATSAngabenGemeindeContainer.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON) {
            this.lATSAngabenGemeindeContainer.angabenKorrektur = this.angabenForm.value;
        } else {
            this.lATSAngabenGemeindeContainer.angabenDeklaration = this.angabenForm.value;
        }
        this.errorService.clearAll();
        this.lastenausgleichTSService.latsAngabenGemeindeFormularAbschliessen(this.lATSAngabenGemeindeContainer)
            .subscribe(container => this.handleSaveSuccess(container),
                err => this.handleSaveError(err));
    }

    private confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translateService.instant(frageKey),
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
        } else {
            this.errorService.clearAll();
        }
        this.cd.markForCheck();
        this.wizardRS.updateSteps(this.WIZARD_TYPE, this.uiRouterGlobals.params.id);
    }

    private handleSaveError(error: any): void {
        // tslint:disable-next-line:early-exit
        if (error.status === HTTP_ERROR_CODES.BAD_REQUEST) {
            if (error.error.includes('institution')) {
                this.errorService.addMesageAsError(this.translateService.instant(
                    'LATS_NICHT_ALLE_INSTITUTIONEN_ABGESCHLOSSEN'));
            } else if (error.error.includes('incomplete')) {
                this.errorService.addMesageAsError(this.translateService.instant(
                    'LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'));
            } else {
                this.errorService.addMesageAsError(this.translateService.instant('SAVE_ERROR'));
            }
        } else {
            this.errorService.addMesageAsError(this.translateService.instant('SAVE_ERROR'));
        }
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
        return this.lATSAngabenGemeindeContainer.isInBearbeitungGemeinde() && // tslint:disable-next-line:max-line-length
            this.lATSAngabenGemeindeContainer.angabenDeklaration.status === TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN ||
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOnlyRoles()) && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton() ||
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) && this.lATSAngabenGemeindeContainer.isAtLeastInBearbeitungKanton() && // tslint:disable-next-line:max-line-length
            this.lATSAngabenGemeindeContainer.angabenKorrektur.status === TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
    }

    public onFalscheAngaben(): void {
        this.lastenausgleichTSService.falscheAngaben(this.lATSAngabenGemeindeContainer);
    }

    public falscheAngabenVisible(): boolean {
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOnlyRoles().concat(TSRole.SUPER_ADMIN))) {
            return false;
        }
        return this.lATSAngabenGemeindeContainer?.isInBearbeitungGemeinde() &&
            this.lATSAngabenGemeindeContainer?.angabenDeklaration.status ===
            TSLastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
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
        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .setValidators([
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ]);
        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .setValidators([
                numberValidator(ValidationType.POSITIVE_INTEGER),
            ]);
        this.angabenForm.get('einnahmenElterngebuehren')
            .setValidators([this.numberValidator()]);
        this.angabenForm.get('tagesschuleTeilweiseGeschlossen').clearValidators();
        this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
            .setValidators([this.numberValidator()]);
        this.angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').clearValidators();
        this.angabenForm.get('ersteRateAusbezahlt')
            .setValidators([numberValidator(ValidationType.POSITIVE_INTEGER)]);

        // tslint:disable-next-line:no-identical-functions
        this.angabenForm.get('tagesschuleTeilweiseGeschlossen').valueChanges.subscribe(value => {
            if (value === true) {
                this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
                    .setValidators([this.numberValidator()]);
            } else {
                this.angabenForm.get('rueckerstattungenElterngebuehrenSchliessung')
                    .setValidators(null);
            }
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        // C
        this.angabenForm.get('gesamtKostenTagesschule')
            .setValidators([this.numberValidator()]);
        this.angabenForm.get('einnnahmenVerpflegung')
            .setValidators([this.numberValidator()]);
        this.angabenForm.get('einnahmenSubventionenDritter')
            .setValidators([this.numberValidator()]);
        this.angabenForm.get('ueberschussErzielt').clearValidators();
        this.angabenForm.get('ueberschussVerwendung').clearValidators();

        // E
        this.angabenForm.get('betreuungsstundenDokumentiertUndUeberprueft').clearValidators();
        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnet').clearValidators();
        this.angabenForm.get('einkommenElternBelegt').clearValidators();
        this.angabenForm.get('maximalTarif').clearValidators();
        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal').clearValidators();
        this.angabenForm.get('ausbildungenMitarbeitendeBelegt').clearValidators();

        this.triggerFormValidation();
    }

    private allInstitutionsGeprueft(): ValidatorFn {
        return () => {
            return this.lATSAngabenGemeindeContainer?.allAngabenInstitutionContainersGeprueft() ? null : {
                notAllInstitutionsGeprueft: true,
            };
        };
    }

    public abschliessenVisible(): boolean {
        return this.lATSAngabenGemeindeContainer?.allAngabenInstitutionContainersGeprueft();
    }
}
