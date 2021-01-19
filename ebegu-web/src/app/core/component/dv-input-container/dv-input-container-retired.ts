/*
Copyright (C) $today.year DV Bern AG, Switzerland

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions} from 'angular';

export class DvInputContainerRetiredComponentConfig implements IComponentOptions {
    public transclude = true;
    public template = require('./dv-input-container-retired.html');
    public controller = DvInputContainerRetiredController;
    public controllerAs = 'vm';
}

export class DvInputContainerRetiredController {

    public static $inject: ReadonlyArray<string> = [];
}
