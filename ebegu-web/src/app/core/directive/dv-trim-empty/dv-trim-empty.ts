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

import {IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, INgModelController, IScope} from 'angular';

/**
 * this directive can be added to an element that has an ngModel to trim the empty string to null
 */
export default class DVTrimEmpty implements IDirective {

    public restrict = 'A';
    public require = '?ngModel';
    public link: IDirectiveLinkFn;

    public constructor() {
        this.link = (scope: IScope, element: IAugmentedJQuery, attrs, ngModel: INgModelController) => {
            if (ngModel) {
                  const emptyTrimFunc = (value: any) => value === '' ? null : value;
                  ngModel.$parsers.push(emptyTrimFunc);
                }
        };
    }

    public static factory(): IDirectiveFactory {
        const directive = () => new DVTrimEmpty();
        return directive;
    }
}
