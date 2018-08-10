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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import {ApplicationPropertyRS} from '../app/core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../app/core/service/institutionRS.rest';
import {MandantRS} from '../app/core/service/mandantRS.rest';
import {TraegerschaftRS} from '../app/core/service/traegerschaftRS.rest';
import {RouterHelper} from '../dvbModules/router/route-helper-provider';

adminRun.$inject = ['RouterHelper'];

export function adminRun(routerHelper: RouterHelper) {
    routerHelper.configureStates(ng1States);
}

export class IGesuchsperiodeStateParams {
    gesuchsperiodeId: string;
}

export class IInstitutionStateParams {
    institutionId: string;
}

export class IInstitutionStammdatenStateParams {
    institutionStammdatenId: string;
    institutionId: string;
}

export class IBenutzerStateParams {
    benutzerId: string;
}

const applicationPropertiesResolver = ['ApplicationPropertyRS', (applicationPropertyRS: ApplicationPropertyRS) => {
    return applicationPropertyRS.getAllApplicationProperties();
}];

const institutionenResolver = ['InstitutionRS', (institutionRS: InstitutionRS) => {
    return institutionRS.getAllActiveInstitutionen();
}];

const traegerschaftenResolver = ['TraegerschaftRS', (traegerschaftRS: TraegerschaftRS) => {
    return traegerschaftRS.getAllActiveTraegerschaften();
}];

const mandantResolver = ['MandantRS', (mandantRS: MandantRS) => {
    return mandantRS.getFirst();
}];

const ng1States: Ng1StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'admin',
    },
    {
        name: 'admin.view',
        template: '<dv-admin-view flex="auto" class="overflow-scroll" application-properties="$resolve.applicationProperties"></dv-admin-view>',
        url: '/admin',
        resolve: {
            applicationProperties: applicationPropertiesResolver
        }
    },
    {
        name: 'admin.benutzerlist',
        template: '<benutzer-list-view flex="auto" class="overflow-scroll"></benutzer-list-view>',
        url: '/benutzerlist',
    },
    {
        name: 'admin.benutzer',
        template: '<dv-benutzer flex="auto" class="overflow-scroll"></dv-benutzer>',
        url: '/benutzerlist/benutzer/:benutzerId',
    },
    {
        name: 'admin.institutionen',
        template: '<dv-institutionen-list-view flex="auto" class="overflow-scroll"'
        + ' institutionen="$resolve.institutionen"></dv-institutionen-list-view>',
        url: '/institutionen',

        resolve: {
            institutionen: institutionenResolver,
        }
    },
    {
        name: 'admin.institution',
        template: '<dv-institution-view flex="auto" class="overflow-scroll"'
        + ' traegerschaften="$resolve.traegerschaften"'
        + ' mandant="$resolve.mandant"></dv-institution-view>',
        url: '/institutionen/institution/:institutionId',
        params: {
            institutionId: '',
        },

        resolve: {
            traegerschaften: traegerschaftenResolver,
            mandant: mandantResolver
        }
    },
    {
        name: 'admin.institutionstammdaten',
        template: '<dv-institution-stammdaten-view flex="auto" class="overflow-scroll"/>',
        url: '/institutionen/institution/:institutionId/:institutionStammdatenId',
        params: {
            institutionStammdatenId: '',
        },
    },
    {
        name: 'admin.parameter',
        template: '<dv-parameter-view flex="auto" class="overflow-scroll"></dv-parameter-view>',
        url: '/parameter',
    },
    {
        name: 'admin.gesuchsperiode',
        template: '<dv-gesuchsperiode-view flex="auto" class="overflow-scroll"'
        + ' mandant="$resolve.mandant"></dv-gesuchsperiode-view>',
        url: '/parameter/gesuchsperiode/:gesuchsperiodeId',
        params: {
            gesuchsperiodeId: '',
        },
    },
    {
        name: 'admin.ferieninsel',
        template: '<dv-ferieninsel-view flex="auto" class="overflow-scroll"></dv-ferieninsel-view>',
        url: '/ferieninsel',
    },
];
