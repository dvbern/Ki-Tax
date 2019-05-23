/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {IComponentOptions, IAugmentedJQuery} from 'angular';
import {ApplicationPropertyRS} from './core/rest-services/applicationPropertyRS.rest';

export class AppAngularjsComponent implements angular.IController {

    public static $inject: string[] = ['$element', 'ApplicationPropertyRS'];

    public constructor(
        private readonly $element: IAugmentedJQuery,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
    ) {
    }

    public $postLink(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(response => {
                this.$element.find('#Intro')
                    .css('background-color', response.backgroundColor);
                this.$element.find('.environment')
                    .css('display', response.devmode ? 'inline' : 'none');
            });
    }
}

export const APP_ANGULARJS_COMPONENT: IComponentOptions = {
    template: require('./app.angularjs.component.html'),
    controller: AppAngularjsComponent,
};
