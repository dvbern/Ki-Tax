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
import {FormBuilder, FormGroup} from '@angular/forms';
import {combineLatest} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {GemeindeAntragService} from '../../services/gemeinde-antrag.service';

@Component({
    selector: 'dv-gemeinde-angaben',
    templateUrl: './gemeinde-angaben.component.html',
    styleUrls: ['./gemeinde-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAngabenComponent implements OnInit {

    @Input() public lastenausgleichID: string;

    public formGroup: FormGroup;

    public constructor(
        private readonly fb: FormBuilder,
        private readonly gemeindeAntraegeService: GemeindeAntragService,
        private readonly cd: ChangeDetectorRef,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeAntraegeService.getGemeindeAngabenFor(this.lastenausgleichID)
            .subscribe((gemeindeAngabenContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer) => {
                const gemeindeAngaben = gemeindeAngabenContainer.angabenDeklaration;
                this.setupForm(gemeindeAngaben);
                this.setupCalculcations(gemeindeAngaben);

                this.cd.markForCheck();
            });
    }

    private setupForm(initialGemeindeAngaben: TSLastenausgleichTagesschuleAngabenGemeinde): void {
        this.formGroup = this.fb.group({
            // A
            alleFaelleInKibon: [''],
            angebotVerfuegbarFuerAlleSchulstufen: [initialGemeindeAngaben.angebotVerfuegbarFuerAlleSchulstufen],
            begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen:
                [initialGemeindeAngaben.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen],
            bedarfBeiElternAbgeklaert: [initialGemeindeAngaben.bedarfBeiElternAbgeklaert],
            angebotFuerFerienbetreuungVorhanden: [initialGemeindeAngaben.angebotFuerFerienbetreuungVorhanden],
            // B
            geleisteteBetreuungsstundenOhneBesondereBeduerfnisse:
                [initialGemeindeAngaben.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse],
            geleisteteBetreuungsstundenBesondereBeduerfnisse:
                [initialGemeindeAngaben.geleisteteBetreuungsstundenBesondereBeduerfnisse],
            davonStundenZuNormlohnMehrAls50ProzentAusgebildete:
                [initialGemeindeAngaben.davonStundenZuNormlohnMehrAls50ProzentAusgebildete],
            davonStundenZuNormlohnWenigerAls50ProzentAusgebildete:
                [initialGemeindeAngaben.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete],
            einnahmenElterngebuehren: [initialGemeindeAngaben.einnahmenElterngebuehren],
            // TODO: get this from somwhere in kibon
            ersteRateAusbezahlt: [],
            // TODO: get this from somewhere in kibon
            anteilZusaetzlichVerrechneterStunden: [{value: '11.11%', disabled: true}],
            // C
            gesamtKostenTagesschule: [initialGemeindeAngaben.gesamtKostenTagesschule],
            einnnahmenVerpflegung: [initialGemeindeAngaben.einnnahmenVerpflegung],
            einnahmenSubventionenDritter: [initialGemeindeAngaben.einnahmenSubventionenDritter],
            // D
            bemerkungenWeitereKostenUndErtraege: [initialGemeindeAngaben.bemerkungenWeitereKostenUndErtraege],
            // E
            betreuungsstundenDokumentiertUndUeberprueft:
                [initialGemeindeAngaben.betreuungsstundenDokumentiertUndUeberprueft],
            elterngebuehrenGemaessVerordnungBerechnet:
                [initialGemeindeAngaben.elterngebuehrenGemaessVerordnungBerechnet],
            einkommenElternBelegt: [initialGemeindeAngaben.einkommenElternBelegt],
            maximalTarif: [initialGemeindeAngaben.maximalTarif],
            mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal:
                [initialGemeindeAngaben.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal],
            ausbildungenMitarbeitendeBelegt: [initialGemeindeAngaben.ausbildungenMitarbeitendeBelegt],
            // Bemerkungen
            bemerkungen: [initialGemeindeAngaben.bemerkungen],
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
        });
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
                this.formGroup.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse),
                ),
                this.formGroup.get('geleisteteBetreuungsstundenBesondereBeduerfnisse').valueChanges.pipe(
                    startWith(gemeindeAngabenFromServer.geleisteteBetreuungsstundenBesondereBeduerfnisse),
                ),
            ],
        ).subscribe(formValues => {
            this.formGroup.get('lastenausgleichberechtigteBetreuungsstunden')
                .setValue(parseFloat(formValues[0] || 0) + parseFloat(formValues[1] || 0));
        });

        this.formGroup.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
            .valueChanges
            .subscribe(value => {
                // TODO: replace with config param
                this.formGroup.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                    .setValue(value ? value * 5.25 : 0);
            });

        this.formGroup.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
            .valueChanges
            .subscribe(value => {
                // TODO: replace with config param
                this.formGroup.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                    .setValue(value ? value * 10.39 : 0);
            });

        combineLatest(
            [
                this.formGroup.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0)),
                this.formGroup.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                    .valueChanges
                    .pipe(startWith(0)),
            ],
        ).subscribe(value => this.formGroup.get('normlohnkostenBetreuungBerechnet')
            .setValue(parseFloat(value[0] || 0) + parseFloat(value[1] || 0)),
        );

        combineLatest(
            [
                this.formGroup.get('normlohnkostenBetreuungBerechnet').valueChanges.pipe(startWith(0)),
                this.formGroup.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0)),
            ],
        ).subscribe(values => {
            this.formGroup.get('einnahmenElterngebuehrenPercentual')
                // TODO: clean up
                .setValue((values[0] === 0 ? 0 : values[1] / values[0] * 100).toFixed(2) + '%');
            this.formGroup.get('lastenausgleichsberechtigerBetrag').setValue(values[0] - values[1]);
        });

        combineLatest([
            this.formGroup.get('lastenausgleichsberechtigerBetrag').valueChanges.pipe(startWith(0)),
            this.formGroup.get('ersteRateAusbezahlt').valueChanges.pipe(startWith(0)),
        ]).subscribe(values => {
            this.formGroup.get('zweiteRate').setValue(values[0] - values[1]);
        });

        // TODO: merge with other einnahmenElterngebuehren observable
        this.formGroup.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0))
            .subscribe(value => this.formGroup.get('einnahmenElterngebuehrenRO').setValue(value));

        // TODO: merge with existing observables
        combineLatest([
            this.formGroup.get('gesamtKostenTagesschule').valueChanges.pipe(startWith(0)),
            this.formGroup.get('lastenausgleichsberechtigerBetrag').valueChanges.pipe(startWith(0)),
            this.formGroup.get('einnahmenElterngebuehren').valueChanges.pipe(startWith(0)),
            this.formGroup.get('einnnahmenVerpflegung').valueChanges.pipe(startWith(0)),
            this.formGroup.get('einnahmenSubventionenDritter').valueChanges.pipe(startWith(0)),
        ]).subscribe(values => {
            const gemeindeBeitragOderUeberschuss = values[0] - values[1] - values[2] - values[3] - values[4];
            if (gemeindeBeitragOderUeberschuss < 0) {
                this.formGroup.get('kostenueberschussGemeinde')
                    .setValue(gemeindeBeitragOderUeberschuss);
                this.formGroup.get('kostenbeitragGemeinde')
                    .setValue('');
            } else {
                this.formGroup.get('kostenbeitragGemeinde')
                    .setValue(gemeindeBeitragOderUeberschuss);
                this.formGroup.get('kostenueberschussGemeinde')
                    .setValue('');
            }
        });

    }

    public inMandantRoles(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }
}
