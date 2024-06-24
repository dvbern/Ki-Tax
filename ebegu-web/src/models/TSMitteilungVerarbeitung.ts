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

import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSBetreuungsmitteilung} from './TSBetreuungsmitteilung';
import {TSExceptionReport} from './TSExceptionReport';
import {TSMitteilungVerarbeitungResult} from './TSMitteilungVerarbeitungResult';

export class TSMitteilungVerarbeitung {
    private readonly _successItems$: BehaviorSubject<TSBetreuungsmitteilung[]>;
    private readonly _failedItems$: BehaviorSubject<TSBetreuungsmitteilung[]>;
    private readonly _errorItems$: BehaviorSubject<
        {errors: TSExceptionReport[]; mitteilung: TSBetreuungsmitteilung}[]
    >;
    private readonly _total: number;
    private readonly _count$: BehaviorSubject<number>;

    public constructor(total: number) {
        this._total = total;
        this._successItems$ = new BehaviorSubject<TSBetreuungsmitteilung[]>([]);
        this._failedItems$ = new BehaviorSubject<TSBetreuungsmitteilung[]>([]);
        this._errorItems$ = new BehaviorSubject<
            {errors: TSExceptionReport[]; mitteilung: TSBetreuungsmitteilung}[]
        >([]);
        this._count$ = new BehaviorSubject<number>(0);
    }

    public get results(): Observable<TSMitteilungVerarbeitungResult> {
        return combineLatest([
            this._successItems$,
            this._failedItems$,
            this._count$,
            this._errorItems$
        ]).pipe(
            map(([successItems, failedItems, count, errors]) => ({
                count,
                successItems,
                failedItems,
                total: this._total,
                errors
            }))
        );
    }

    public addSuccess(appliedMitteilung: TSBetreuungsmitteilung): void {
        this._successItems$.next(
            this._successItems$.value.concat(appliedMitteilung)
        );
        this.increaseCount();
    }

    public addFailure(appliedMitteilung: TSBetreuungsmitteilung): void {
        this._failedItems$.next(
            this._failedItems$.value.concat(appliedMitteilung)
        );
        this.increaseCount();
    }

    public addError(
        mitteilung: TSBetreuungsmitteilung,
        errors: TSExceptionReport[]
    ): void {
        this._errorItems$.next(
            this._errorItems$.value.concat({errors, mitteilung})
        );
        this.increaseCount();
    }

    private increaseCount(): void {
        this._count$.next(this._count$.value + 1);
    }
}
