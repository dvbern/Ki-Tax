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

import {Directive, ElementRef, Injector, Input} from '@angular/core';
import {UpgradeComponent} from '@angular/upgrade/static';

@Directive({
    selector: 'dv-navigation'
})
export class DvNavigationX extends UpgradeComponent {

    @Input() public dvPrevious: () => any;
    @Input() public dvNext: () => any;
    @Input() public dvSave: () => any;
    @Input() public dvCancel: () => any;
    @Input() public dvNextDisabled: () => any;
    @Input() public dvSubStep: number;
    @Input() public dvSubStepName: string;
    @Input() public dvSavingPossible: boolean;
    @Input() public dvTranslateNext: string;
    @Input() public dvTranslatePrevious: string;
    @Input() public containerClass: string;

    public constructor(elementRef: ElementRef, injector: Injector) {
        super('dvNavigation', elementRef, injector);
    }
}
