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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {Directive, ElementRef, Input, OnInit} from '@angular/core';
import {DemoFeatureRS} from '../../service/demoFeatureRS.rest';
import {TSDemoFeature} from './TSDemoFeature';

// Directive decorator
@Directive({selector: '[dvDemoFeature]'})
// Directive class
export class DvDemoFeatureDirective implements OnInit {

    @Input() public dvDemoFeature: TSDemoFeature;
    @Input() private readonly hideIfDemoFeatureActive: boolean = false;

    public constructor(
        private readonly elementRef: ElementRef,
        private readonly demofeatureRS: DemoFeatureRS
    ) {
    }

    public ngOnInit(): void {
        this.setElementDisplayValue('none');
        this.checkIfAllowed();
    }

    public checkIfAllowed(): void {
        this.demofeatureRS.isDemoFeatureAllowed(this.dvDemoFeature)
            .then(isAllowed => {
                if(isAllowed && !this.hideIfDemoFeatureActive) {
                    this.setElementDisplayValue('block');
                } else if (!isAllowed && this.hideIfDemoFeatureActive) {
                    this.setElementDisplayValue('block');
                }
            });
    }

    private setElementDisplayValue(value: string): void {
        this.elementRef.nativeElement.style.display = value;
    }
}
