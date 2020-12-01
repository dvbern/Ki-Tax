/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {DOCUMENT} from '@angular/common';
import {ErrorHandler, Inject, Injectable} from '@angular/core';
import {GuidedTourService, WindowRefService} from 'ngx-guided-tour';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class KiBonGuidedTourService extends GuidedTourService {

    private readonly guidedTourSubject$ = new BehaviorSubject(false);
    private readonly _guidedTour$ = this.guidedTourSubject$.asObservable();

    private constructor(public errorHandler: ErrorHandler,
                        private readonly window: WindowRefService,
                        @Inject(DOCUMENT) private readonly domEl: any) {
        super(errorHandler, window, domEl);
    }

    public emit(): void {
        this.guidedTourSubject$.next(true);
    }

    public get guidedTour$(): Observable<boolean | null> {
        return this._guidedTour$;
    }
}
