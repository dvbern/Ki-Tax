/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {Directive, EventEmitter, Input, Output} from '@angular/core';
import {LoadingButtonDirective} from '../app/shared/directive/loading-button.directive';

/**
 * This mock should be used when testing an angular component that uses LoadingButtonDirective
 * to avoid conflicts with dependencies
 */
@Directive({
    selector: 'dv-loading-button',
})
export class MockDvLoadingButton {

    @Input() public type: string;
    @Input() public delay: string;
    @Input() public buttonClass: string;
    @Input() public forceWaitService: string;
    @Input() public buttonDisabled: '<';
    @Input() public ariaLabel: string;
    @Input() public inputId: string;

    @Output() public readonly buttonClick: EventEmitter<void>;
}

export const SHARED_MODULE_OVERRIDES = {
    remove: {
        declarations: [LoadingButtonDirective],
        exports: [LoadingButtonDirective],
    },
    add: {
        declarations: [MockDvLoadingButton],
        exports: [MockDvLoadingButton],
    }
};
