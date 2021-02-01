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
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, Subscription} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
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

    public angabenForm: FormGroup;
    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public formularInitForm: FormGroup;
    private subscription: Subscription;
    public formFreigebenTriggered = false;

    public constructor(
        private readonly fb: FormBuilder,
        private readonly gemeindeAntraegeService: GemeindeAntragService,
        private readonly cd: ChangeDetectorRef,
        private readonly authServiceRS: AuthServiceRS,
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly errorService: ErrorService,
        private readonly translateService: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer()
            .subscribe(container => {
                this.lATSAngabenGemeindeContainer = container;
                if (this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst !== null) {
                    const gemeindeAngaben = container.angabenDeklaration;
                    this.setupForm(gemeindeAngaben);
                    this.setupCalculcations(gemeindeAngaben);
                }
                this.initLATSGemeindeInitializationForm();
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
                        disabled: this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst !== null,
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
            // TODO: get this from somwhere in kibon
            ersteRateAusbezahlt: [],
            // TODO: get this from somewhere in kibon
            anteilZusaetzlichVerrechneterStunden: [{value: '11.11%', disabled: true}],
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
            einnahmenElterngebuehrenPercentual: [{value: '', disabled: true}],
            einnahmenElterngebuehrenRO: [{value: '', disabled: true}],
            lastenausgleichsberechtigerBetrag: [{value: '', disabled: true}],
            zweiteRate: [{value: '', disabled: true}],
            kostenbeitragGemeinde: [{value: '', disabled: true}],
            kostenueberschussGemeinde: [{value: '', disabled: true}],
            erwarteterKostenbeitragGemeinde: [{value: '', disabled: true}],
        });

        // tslint:disable-next-line:max-line-length
        if (this.lATSAngabenGemeindeContainer.status !== TSLastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE &&
            this.lATSAngabenGemeindeContainer.status !== TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU) {
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
        })

        // B
        this.angabenForm.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse')
            .setValidators([Validators.required]);
        this.angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse').setValidators([Validators.required]);
        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete').setValidators([Validators.required]);
        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .setValidators([Validators.required]);
        this.angabenForm.get('einnahmenElterngebuehren').setValidators([Validators.required]);

        // C
        this.angabenForm.get('gesamtKostenTagesschule').setValidators([Validators.required]);
        this.angabenForm.get('einnnahmenVerpflegung').setValidators([Validators.required]);
        this.angabenForm.get('einnahmenSubventionenDritter').setValidators([Validators.required]);

        // E
        this.angabenForm.get('betreuungsstundenDokumentiertUndUeberprueft').setValidators([Validators.required]);
        this.angabenForm.get('elterngebuehrenGemaessVerordnungBerechnet').setValidators([Validators.required]);
        this.angabenForm.get('einkommenElternBelegt').setValidators([Validators.required]);
        this.angabenForm.get('maximalTarif').setValidators([Validators.required]);
        this.angabenForm.get('mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal')
            .setValidators([Validators.required]);
        this.angabenForm.get('ausbildungenMitarbeitendeBelegt').setValidators([Validators.required]);
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
        });

        this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .valueChanges
            .subscribe(value => {
                // TODO: replace with config param
                this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                    .setValue(value ? value * 5.25 : 0);
            });

        this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .valueChanges
            .subscribe(value => {
                // TODO: replace with config param
                this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                    .setValue(value ? value * 10.39 : 0);
            });

        combineLatest(
            [
                this.angabenForm.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0)),
                this.angabenForm.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0)),
            ],
        ).subscribe(value => this.angabenForm.get('normlohnkostenBetreuungBerechnet')
            .setValue(parseFloat(value[0] || 0) + parseFloat(value[1] || 0)),
        );

        combineLatest(
            [
                this.angabenForm.get('normlohnkostenBetreuungBerechnet').valueChanges.pipe(startWith(0)),
                this.angabenForm.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0)),
            ],
        ).subscribe(values => {
            this.angabenForm.get('einnahmenElterngebuehrenPercentual')
                // TODO: clean up
                .setValue((values[0] === 0 ? 0 : values[1] / values[0] * 100).toFixed(2) + '%');
            this.angabenForm.get('lastenausgleichsberechtigerBetrag').setValue(values[0] - values[1]);
        });

        combineLatest([
            this.angabenForm.get('lastenausgleichsberechtigerBetrag').valueChanges.pipe(startWith(0)),
            this.angabenForm.get('ersteRateAusbezahlt').valueChanges.pipe(startWith(0)),
        ]).subscribe(values => {
            this.angabenForm.get('zweiteRate').setValue(values[0] - values[1]);
        });

        // TODO: merge with other einnahmenElterngebuehren observable
        this.angabenForm.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0))
            .subscribe(value => this.angabenForm.get('einnahmenElterngebuehrenRO').setValue(value));

        // TODO: merge with existing observables
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
        });

        // TODO: merge with existing observables
        this.angabenForm.get('gesamtKostenTagesschule').valueChanges.pipe(startWith(0)).subscribe(value => {
            this.angabenForm.get('erwarteterKostenbeitragGemeinde').setValue(value * 0.2);
        });

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
            this.lATSAngabenGemeindeContainer.angabenDeklaration = this.angabenForm.value;
            this.lastenausgleichTSService.saveLATSAngabenGemeindeContainer(this.lATSAngabenGemeindeContainer);
        }

    }

    public onFormFreigebenSubmit(): void {
        this.formFreigebenTriggered = true;
        this.enableFormValidation();
        for (const key in this.angabenForm.controls) {
            if (this.angabenForm.get(key) !== null) {
                this.angabenForm.get(key).updateValueAndValidity();
            }
        }
        this.angabenForm.updateValueAndValidity();
        if (this.angabenForm.valid) {
            this.latsGemeindeAngabenFreigeben();
        }

    }

    public latsGemeindeAngabenFreigeben(): void {
        this.lATSAngabenGemeindeContainer.angabenDeklaration = this.angabenForm.value;
        this.lastenausgleichTSService.lATSAngabenGemeindeFuerInstitutionenFreigeben(this.lATSAngabenGemeindeContainer);
    }
}
