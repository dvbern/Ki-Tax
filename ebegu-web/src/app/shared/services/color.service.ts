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

import {Injectable} from '@angular/core';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';

@Injectable({
    providedIn: 'root'
})
export class ColorService {

    public constructor(
        private applicationPropertyRS: ApplicationPropertyRS,
    ) {
    }

    private static changeColor(color: string, cssVariable: string): void {
        document.documentElement.style.setProperty(cssVariable, color);
    }

    private static changeColors(config: TSPublicAppConfig): void {
        ColorService.changeColor(config.primaryColor, '--primary-color');
        ColorService.changeColor(config.primaryColorDark, '--primary-color-dark');
        ColorService.changeColor(config.primaryColorLight, '--primary-color-light');
    }

    public registerColorChangeForMandant(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(config => {
                ColorService.changeColors(config);
            });
    }
}
