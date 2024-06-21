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

import {IComponentOptions, IController} from 'angular';
import {TSSearchResultEntry} from '../../../../../models/dto/TSSearchResultEntry';

export class DvSearchResultIconComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        entry: '<'
    };
    public template = require('./dv-search-result-icon.html');
    public controller = DvSearchResultController;
    public controllerAs = 'vm';
}

export class DvSearchResultController implements IController {
    public static $inject: ReadonlyArray<string> = [];

    public entry: TSSearchResultEntry;
}
