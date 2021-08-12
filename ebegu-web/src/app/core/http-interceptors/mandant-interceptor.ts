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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {switchMap, tap} from 'rxjs/operators';
import {KiBonMandant, MandantService} from '../../shared/services/mandant.service';

@Injectable({
    providedIn: 'root',
})
export class MandantInterceptor implements HttpInterceptor {

    public constructor(
        private mandantService: MandantService,
    ) {
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            tap(mandant => {
                console.log(mandant);
                if (mandant === KiBonMandant.NONE) {
                    console.log('uiuiui, redirect to mandant selection');
                }
                return next.handle(req);
            })
        );
        return this.mandantService.mandant$.pipe(
            switchMap(mandant => {
                console.log(mandant);
                if (mandant === KiBonMandant.NONE) {
                    console.log('uiuiui, redirect to mandant selection');
                }
                return next.handle(req);
            }
        )
        );
    }

}