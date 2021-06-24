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

export class TSControllingCalculator {

    private _erwarteteBetreuungsstundenCurrentPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _erwarteteBetreuungsstundenLastPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _veraenderungBetreuungsstunden: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _anteilStundenBesondereBeduerfnisseCurrentPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private _anteilStundenBesondereBeduerfnisseLastPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private _kostenanteilGemeindeGesamtkosten: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _erstragsanteilGemeindeGesamtkosten: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _anteilElternbeitraegeCurrentPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);
    private _anteilElternbeitraegeLastPeriode: BehaviorSubject<string> = new BehaviorSubject<string>(undefined);

    private _angabenForm: FormGroup;

    public constructor(angabenForm: FormGroup) {
        this._angabenForm = angabenForm;
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

    public get anteilStundenBesondereBeduerfnisseLastPeriode$(): Observable<string> {
        return this._anteilStundenBesondereBeduerfnisseLastPeriode.asObservable();
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

    public get anteilElternbeitraegeLastPeriode$(): Observable<string> {
        return this._anteilElternbeitraegeLastPeriode.asObservable();
    }

    private setupCalculations(): void {
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
                (values[0] / values[1]).toFixed(2),
            );
        });
    }
}
