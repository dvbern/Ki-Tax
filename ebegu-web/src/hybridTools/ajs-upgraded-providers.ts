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

import {ApplicationPropertyRS} from '../admin/service/applicationPropertyRS.rest';
import AuthServiceRS from '../authentication/service/AuthServiceRS.rest';

// AuthServiceRS
export function authServiceRSServiceFactory(i: any) {
    return i.get('AuthServiceRS');
}

export const authServiceRSProvider = {
    provide: AuthServiceRS,
    useFactory: authServiceRSServiceFactory,
    deps: ['$injector']
};

// ApplicationPropertyRS
export function applicationPropertyRSServiceFactory(i: any) {
    return i.get('ApplicationPropertyRS');
}

export const applicationPropertyRSProvider = {
    provide: ApplicationPropertyRS,
    useFactory: applicationPropertyRSServiceFactory,
    deps: ['$injector']
};

