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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {FormGroup} from '@angular/forms';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';

export class TSControllingCalculator {

    private _erwarteteBetreuungsstundenCurrentPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _erwarteteBetreuungsstundenLastPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _veraenderungBetreuungsstunden: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _anteilStundenBesondereBeduerfnisseCurrentPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private _anteilStundenBesondereBeduerfnissePreviousPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private _kostenanteilGemeindeGesamtkosten: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _erstragsanteilGemeindeGesamtkosten: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _anteilElternbeitraegeCurrentPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _anteilElternbeitraegePreviousPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);

    private _angabenForm: FormGroup;
    private _previousAntrag: TSLastenausgleichTagesschuleAngabenGemeindeContainer;

    public constructor(
        angabenForm: FormGroup,
        previousAntrag: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
    ) {
        this._angabenForm = angabenForm;
        this._previousAntrag = previousAntrag;
        this.setupCalculations();
    }

    public get erwarteteBetreuungsstundenCurrentPeriode$(): Observable<string> {
        return this._erwarteteBetreuungsstundenCurrentPeriode.asObservable();
    }

    public get erwarteteBetreuungsstundenLastPeriode$(): Observable<string> {
        return this._erwarteteBetreuungsstundenLastPeriode.asObservable();
    }

    public get veraenderungBetreuungsstunden$(): Observable<string> {
        return this._veraenderungBetreuungsstunden.asObservable();
    }

    public get anteilStundenBesondereBeduerfnisseCurrentPeriode$(): Observable<string> {
        return this._anteilStundenBesondereBeduerfnisseCurrentPeriode.asObservable();
    }

    public get anteilStundenBesondereBeduerfnissePreviousPeriode$(): Observable<string> {
        return this._anteilStundenBesondereBeduerfnissePreviousPeriode.asObservable();
    }

    public get kostenanteilGemeindeGesamtkosten$(): Observable<string> {
        return this._kostenanteilGemeindeGesamtkosten.asObservable();
    }

    public get erstragsanteilGemeindeGesamtkosten$(): Observable<string> {
        return this._erstragsanteilGemeindeGesamtkosten.asObservable();
    }

    public get anteilElternbeitraegeCurrentPeriode$(): Observable<string> {
        return this._anteilElternbeitraegeCurrentPeriode.asObservable();
    }

    public get anteilElternbeitraegePreviousPeriode$(): Observable<string> {
        return this._anteilElternbeitraegePreviousPeriode.asObservable();
    }

    private setupCalculations(): void {
        this.calculateVeraenderungBetreuungsstunden();
        this.calculateBesondereBeduerfnisseCurrentPeriode();
        this.calculateBesondereBeduerfnissePreviousPeriode();
        this.calculateAnteilElternbeitraegeCurrentPeriode();
        this.calculateAnteilElternbeitraegePreviousPeriode();
    }

    private calculateVeraenderungBetreuungsstunden(): void {
        if (!(this._previousAntrag?.angabenKorrektur?.lastenausgleichberechtigteBetreuungsstunden)) {
            this._veraenderungBetreuungsstunden.next('?');
            return;
        }
        this._angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
            .valueChanges
            .pipe(
                startWith(this._angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').value)
            ).subscribe(value => {
                let veraenderung =
                    value / this._previousAntrag.angabenKorrektur.lastenausgleichberechtigteBetreuungsstunden;
                veraenderung = veraenderung - 1;
                this._veraenderungBetreuungsstunden.next(this.toPercent(veraenderung));
            });
    }

    private calculateBesondereBeduerfnisseCurrentPeriode(): void {
        combineLatest(
            [
                this._angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse')
                    .valueChanges
                    .pipe(
                        startWith(this._angabenForm.get('geleisteteBetreuungsstundenBesondereBeduerfnisse').value),
                        map(parseFloat),
                    ),
            this._angabenForm.get('lastenausgleichberechtigteBetreuungsstunden')
                .valueChanges
                .pipe(
                    startWith(this._angabenForm.get('lastenausgleichberechtigteBetreuungsstunden').value),
                    map(parseFloat),
                ),
            ],
        ).subscribe(values => {
            const result = values[0] / 3 / values[1];
            this._anteilStundenBesondereBeduerfnisseCurrentPeriode.next(this.toPercent(result));
        });
    }

    private calculateBesondereBeduerfnissePreviousPeriode(): void {
        if (!(this._previousAntrag?.angabenKorrektur?.lastenausgleichberechtigteBetreuungsstunden)) {
            this._anteilStundenBesondereBeduerfnissePreviousPeriode.next('?');
            return;
        }
        const result = this._previousAntrag.angabenKorrektur.geleisteteBetreuungsstundenBesondereBeduerfnisse / 3 /
            this._previousAntrag.angabenKorrektur.lastenausgleichberechtigteBetreuungsstunden;
        this._anteilStundenBesondereBeduerfnissePreviousPeriode.next(this.toPercent(result));
    }

    private calculateAnteilElternbeitraegeCurrentPeriode(): void {
        combineLatest([
            this._angabenForm.get('einnahmenElterngebuehren')
                .valueChanges
                .pipe(
                    startWith(this._angabenForm.get('einnahmenElterngebuehren').value),
                    map(parseFloat),
                ),
            this._angabenForm.get('normlohnkostenBetreuungBerechnet')
                .valueChanges
                .pipe(
                    startWith(this._angabenForm.get('normlohnkostenBetreuungBerechnet').value),
                    map(parseFloat),
                ),
            // tslint:disable-next-line:no-identical-functions
        ]).subscribe(values => {
            this._anteilElternbeitraegeCurrentPeriode.next(
                (this.toPercent(values[0] / values[1])),
            );
        });
    }

    private calculateAnteilElternbeitraegePreviousPeriode(): void {
        if (!(this._previousAntrag?.angabenKorrektur?.einnahmenElterngebuehren)) {
            this._anteilElternbeitraegePreviousPeriode.next('?');
            return;
        }
        const result = this._previousAntrag.angabenKorrektur.einnahmenElterngebuehren /
            this._previousAntrag.angabenKorrektur.normlohnkostenBetreuungBerechnet;
        this._anteilElternbeitraegePreviousPeriode.next(this.toPercent(result));
    }

    private toPercent(value: number): string {
        return (value * 100).toFixed(1) + '%';
    }
}
