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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {combineLatest, Subject, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../models/enums/TSEinstellungKey';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSEinstellung} from '../../../../models/TSEinstellung';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {GemeindeAntragService} from '../../services/gemeinde-antrag.service';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-gemeinde-angaben',
    templateUrl: './gemeinde-angaben.component.html',
    styleUrls: ['./gemeinde-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAngabenComponent implements OnInit {

    @Input() public lastenausgleichID: string;
    @Input() public triggerValidationOnInit = false;

    public angabenForm: FormGroup;
    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public formularInitForm: FormGroup;
    private subscription: Subscription;
    public formFreigebenTriggered = false;
    public lohnnormkostenSettingMoreThanFifty$: Subject<TSEinstellung> = new Subject<TSEinstellung>();
    public lohnnormkostenSettingLessThanFifty$: Subject<TSEinstellung> = new Subject<TSEinstellung>();

    private readonly kostenbeitragGemeinde = 0.2;

    public constructor(
        private readonly fb: FormBuilder,
        private readonly gemeindeAntraegeService: GemeindeAntragService,
        private readonly cd: ChangeDetectorRef,
        private readonly authServiceRS: AuthServiceRS,
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly errorService: ErrorService,
        private readonly translateService: TranslateService,
        private readonly settings: EinstellungRS
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
            if (this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst !== null) {
                this.formularInitForm.get('alleAngabenInKibonErfasst').disable();
            }
        } else {
            this.formularInitForm = new FormGroup({
                alleAngabenInKibonErfasst: new FormControl(
                    {
                        value: this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst,
                        disabled: this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst !== null ||
                            !this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles()),
                    },
                    Validators.required,
                ),
            });
        }
    }

    private setupForm(initialGemeindeAngaben: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        this.angabenForm = this.fb.group({
            // A
            alleFaelleInKibon: [{value: this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst, disabled: true}],
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
                [initialGemeindeAngaben?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse],
            geleisteteBetreuungsstundenBesondereBeduerfnisse:
                [initialGemeindeAngaben?.geleisteteBetreuungsstundenBesondereBeduerfnisse],
            davonStundenZuNormlohnMehrAls50ProzentAusgebildete:
                [initialGemeindeAngaben?.davonStundenZuNormlohnMehrAls50ProzentAusgebildete],
            davonStundenZuNormlohnWenigerAls50ProzentAusgebildete:
                [initialGemeindeAngaben?.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete],
            einnahmenElterngebuehren: [initialGemeindeAngaben?.einnahmenElterngebuehren],
            // C
            gesamtKostenTagesschule: [initialGemeindeAngaben?.gesamtKostenTagesschule],
            einnnahmenVerpflegung: [initialGemeindeAngaben?.einnnahmenVerpflegung],
            einnahmenSubventionenDritter: [initialGemeindeAngaben?.einnahmenSubventionenDritter],
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
                initialGemeindeAngaben?.ausbildungenMitarbeitendeBelegt,
            ],
            // Bemerkungen
            bemerkungen: [initialGemeindeAngaben?.bemerkungen],
            // calculated values
            lastenausgleichberechtigteBetreuungsstunden: [{value: '', disabled: true}],
            davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet: [{value: '', disabled: true}],
            davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet: [{value: '', disabled: true}],
            normlohnkostenBetreuungBerechnet: [{value: '', disabled: true}],
            lastenausgleichsberechtigerBetrag: [{value: '', disabled: true}],
            einnahmenElterngebuehrenRO: [{value: '', disabled: true}],
            kostenbeitragGemeinde: [{value: '', disabled: true}],
            kostenueberschussGemeinde: [{value: '', disabled: true}],
            erwarteterKostenbeitragGemeinde: [{value: '', disabled: true}],
        });

        if (!this.lATSAngabenGemeindeContainer.isInBearbeitungForRole(this.authServiceRS.getPrincipalRole())) {
            this.angabenForm.disable();
        }

        if (this.triggerValidationOnInit) {
            this.triggerFormValidation();
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
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('einnahmenElterngebuehren')
            .setValidators([Validators.required, this.numberValidator()]);

        // C
        this.angabenForm.get('gesamtKostenTagesschule')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('einnnahmenVerpflegung')
            .setValidators([Validators.required, this.numberValidator()]);
        this.angabenForm.get('einnahmenSubventionenDritter')
            .setValidators([Validators.required, this.numberValidator()]);

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

    /**
     * Sets up form obervers that calculate intermediate results of the form that are presented to the user each
     * time the inputs change
     *
     * @param gemeindeAngabenFromServer existing data, used for initiating some calculations
     */
    private setupCalculcations(gemeindeAngabenFromServer: TSLastenausgleichTagesschuleAngabenGemeinde): void {
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
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        combineLatest([
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
                .valueChanges,
            this.lohnnormkostenSettingLessThanFifty$,
        ]).subscribe(valueAndParamter => {
            const value = valueAndParamter[0];
            const lohnkostenParam = parseFloat(valueAndParamter[1].value);
            this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                .setValue((value && lohnkostenParam) ? value * lohnkostenParam : 0);
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

        combineLatest([
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
                .valueChanges,
            this.lohnnormkostenSettingMoreThanFifty$,
        ]).subscribe(valueAndParameter => {
            const value = valueAndParameter[0];
            const lohnkostenParam = parseFloat(valueAndParameter[1].value);
            this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                .setValue((value && lohnkostenParam) ? value * lohnkostenParam : 0);
        }, () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')));

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
                    .setValue(parseFloat(value[0] || 0) + parseFloat(value[1] || 0));
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')),
        );

        combineLatest([
            this.angabenForm.get('gesamtKostenTagesschule').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('lastenausgleichsberechtigerBetrag').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('einnnahmenVerpflegung').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('einnahmenSubventionenDritter').valueChanges.pipe(startWith(0)),
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

                this.angabenForm.get('erwarteterKostenbeitragGemeinde').setValue(values[0] * this.kostenbeitragGemeinde);
                this.angabenForm.get('einnahmenElterngebuehrenRO').setValue(values[2]);
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')),
        );

        combineLatest([
            this.angabenForm.get('normlohnkostenBetreuungBerechnet').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0)),
        ]).subscribe(values => {
                this.angabenForm.get('lastenausgleichsberechtigerBetrag').setValue(
                    // round to 0.2
                    Math.round((values[0] - values[1]) / 5) * 5,
                );
            },
            () => this.errorService.addMesageAsError(this.translateService.instant('LATS_CALCULATION_ERROR')),
        );

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
        if (this.angabenForm.valid) {
            if (this.lATSAngabenGemeindeContainer.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON) {
                this.lATSAngabenGemeindeContainer.angabenKorrektur = this.angabenForm.value;
            } else {
                this.lATSAngabenGemeindeContainer.angabenDeklaration = this.angabenForm.value;
            }
            this.lastenausgleichTSService.saveLATSAngabenGemeindeContainer(this.lATSAngabenGemeindeContainer);
        }

    }

    public triggerFormValidation(): void {
        this.formFreigebenTriggered = true;
        this.enableFormValidation();
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
        return this.lATSAngabenGemeindeContainer.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU ||
        this.lATSAngabenGemeindeContainer.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.GEPRUEFT;
    }
}
