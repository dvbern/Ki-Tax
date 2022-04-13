/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {fromEvent, Observable} from 'rxjs';
import {map, startWith, throttleTime} from 'rxjs/operators';
import {TSMandant} from '../../../models/TSMandant';
import {MandantService} from '../../shared/services/mandant.service';

@Component({
    selector: 'dv-portal-selection',
    templateUrl: './portal-selection.component.html',
    styleUrls: ['./portal-selection.component.less', './../onboarding.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortalSelectionComponent implements OnInit {
    public mandantFilter: string;
    public mandants: TSMandant[];
    public isScreenMobile$: Observable<boolean>;

    public constructor(
        private mandantService: MandantService
    ) {
    }

    public ngOnInit(): void {
        this.mandants = [];
        this.mandants.push(new TSMandant('Kanton Bern'));
        this.mandants.push(new TSMandant('Kanton Luzern'));

        // Checks if screen size is less than 1024 pixels
        const checkScreenSize = () => document.body.offsetWidth < 700;

        // Create observable from window resize event throttled so only fires every 500ms
        this.isScreenMobile$ = fromEvent(window, 'resize').pipe(
            startWith(checkScreenSize()),
            throttleTime(50),
            map(checkScreenSize));

    }

}
