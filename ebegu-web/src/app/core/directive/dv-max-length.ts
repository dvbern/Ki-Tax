/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {
    IAugmentedJQuery,
    IDirective,
    IDirectiveFactory,
    IDirectiveLinkFn,
    IScope
} from 'angular';

export class DVMaxLength implements IDirective {
    public static $inject = ['CONSTANTS'];

    public restrict = 'A';
    public require = 'ngModel';
    public length: number;
    public link: IDirectiveLinkFn;

    public constructor(CONSTANTS: any) {
        this.length = CONSTANTS.MAX_LENGTH;
        this.link = (
            _scope: IScope,
            _element: IAugmentedJQuery,
            _attrs,
            ctrl: any
        ) => {
            if (!ctrl) {
                return;
            }

            ctrl.$validators.dvMaxLength = (_modelValue: any, viewValue: any) =>
                ctrl.$isEmpty(viewValue) || viewValue.length <= this.length;
        };
    }

    public static factory(): IDirectiveFactory {
        const directive = (CONSTANTS: any) => new DVMaxLength(CONSTANTS);
        directive.$inject = ['CONSTANTS'];
        return directive;
    }
}
