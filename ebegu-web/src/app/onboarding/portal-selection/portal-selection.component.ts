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

import {Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {fromEvent, Observable} from 'rxjs';
import {map, startWith, throttleTime} from 'rxjs/operators';
import {TSMandant} from '../../../models/TSMandant';
import {LogFactory} from '../../core/logging/LogFactory';
import {MandantService} from '../../shared/services/mandant.service';

const LOG = LogFactory.createLog('PortalSelectionComponent');

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

    private readonly MOBILE_THRESHOLD = 700;
    private readonly THROTTLE_TIME = 50;

    public constructor(
        private readonly mandantService: MandantService,
        private readonly routerGlobals: UIRouterGlobals,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.mandantService.getAll().subscribe(mandants => {
            this.mandants = mandants;
            this.cd.markForCheck();
        }, error => LOG.error(error));

        // Checks if screen size is less than 1024 pixels
        const checkScreenSize = () => document.body.offsetWidth < this.MOBILE_THRESHOLD;

        // Create observable from window resize event throttled so only fires every 500ms
        this.isScreenMobile$ = fromEvent(window, 'resize').pipe(
            startWith(checkScreenSize()),
            throttleTime(this.THROTTLE_TIME),
            map(checkScreenSize));

    }

    public selectMandant(mandant: TSMandant): void {
        const kibonMandant = this.mandantService.mandantToKibonMandant(mandant);
        this.mandantService.selectMandant(kibonMandant, this.routerGlobals.params.path || '');
    }

    public getMandantLogoUrl(mandant: TSMandant): string {
        const kibonMandant = this.mandantService.mandantToKibonMandant(mandant);
        return `assets/images/${this.mandantService.getMandantLogoName(kibonMandant)}`;
    }
}
