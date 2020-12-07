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
import {ChangeDetectorRef, Component, Inject} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {GuidedTourComponent, GuidedTourService, WindowRefService} from 'ngx-guided-tour';

@Component({
    selector: 'kibon-guided-tour',
    templateUrl: './kibon-guided-tour.component.html',
    styleUrls: ['./kibon-guided-tour.component.scss'],
})
export class KiBonGuidedTourComponent extends GuidedTourComponent {

    public tourStepWidth = 500;
    // tslint:disable
    public constructor(private readonly translate: TranslateService,
                       public readonly guidedTourService: GuidedTourService,
                       private readonly changeDetectorRef: ChangeDetectorRef,
                       private readonly window: WindowRefService,
                       @Inject(DOCUMENT) private readonly domEl: any) {
        super(guidedTourService, window, domEl);
    }

    // tslint:enable
    public updateStepLocation(): void {
        super.updateStepLocation();
        this.changeDetectorRef.markForCheck();
    }
}
