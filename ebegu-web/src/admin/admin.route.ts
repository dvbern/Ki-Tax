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
import {InstitutionRS} from '../core/service/institutionRS.rest';
import {MandantRS} from '../core/service/mandantRS.rest';
import {TraegerschaftRS} from '../core/service/traegerschaftRS.rest';
import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import {ApplicationPropertyRS} from './service/applicationPropertyRS.rest';

adminRun.$inject = ['RouterHelper'];

/* @ngInject */
export function adminRun(routerHelper: RouterHelper) {
    routerHelper.configureStates(getStates());
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

function getStates(): Ng1StateDeclaration[] {
    return [
        {
            name: 'admin',
            template: '<dv-admin-view flex="auto" class="overflow-scroll" application-properties="$resolve.applicationProperties"></dv-admin-view>',
            url: '/admin',
            resolve: {
                applicationProperties: getApplicationProperties
            }
        },
        {
            name: 'testdaten',
            template: '<dv-testdaten-view flex="auto" class="overflow-scroll"></dv-testdaten-view>',
            url: '/testdaten'
        },
        {
            name: 'institutionen',
            template: '<dv-institutionen-list-view flex="auto" class="overflow-scroll"'
            + ' institutionen="$resolve.institutionen"></dv-institutionen-list-view>',
            url: '/institutionen',

            resolve: {
                institutionen: getInstitutionen,
            }
        },
        {
            name: 'institution',
            template: '<dv-institution-view flex="auto" class="overflow-scroll"'
            + ' traegerschaften="$resolve.traegerschaften"'
            + ' mandant="$resolve.mandant"></dv-institution-view>',
            url: '/institutionen/institution/:institutionId',

            resolve: {
                traegerschaften: getTraegerschaften,
                mandant: getMandant
            }
        },
        {
            name: 'institutionstammdaten',
            template: '<dv-institution-stammdaten-view flex="auto" class="overflow-scroll"/>',
            url: '/institutionen/institution/:institutionId/:institutionStammdatenId',
        },
        {
            name: 'parameter',
            template: '<dv-parameter-view flex="auto" class="overflow-scroll" ebeguParameter="vm.ebeguParameter"></dv-parameter-view>',
            url: '/parameter',
        },
        {
            name: 'gesuchsperiode',
            template: '<dv-gesuchsperiode-view flex="auto" class="overflow-scroll"'
            + ' mandant="$resolve.mandant"></dv-gesuchsperiode-view>',
            url: '/parameter/gesuchsperiode/:gesuchsperiodeId',

            resolve: {
                traegerschaften: getTraegerschaften,
                mandant: getMandant
            }
        },
        {
            name: 'ferieninsel',
            template: '<dv-ferieninsel-view flex="auto" class="overflow-scroll"></dv-ferieninsel-view>',
            url: '/ferieninsel',
        },
        {
            name: 'traegerschaft',
            template: '<dv-traegerschaft-view flex="auto" class="overflow-scroll" traegerschaften="$resolve.traegerschaften" ></dv-traegerschaft-view>',
            url: '/traegerschaft',
            resolve: {
                traegerschaften: getTraegerschaften,
            }
        }
    ];
}

// FIXME dieses $inject wird ignoriert, d.h, der Parameter der Funktion muss exact dem Namen des Services entsprechen (Grossbuchstaben am Anfang). Warum?
getApplicationProperties.$inject = ['ApplicationPropertyRS'];

/* @ngInject */
function getApplicationProperties(ApplicationPropertyRS: ApplicationPropertyRS) {
    return ApplicationPropertyRS.getAllApplicationProperties();
}

// FIXME dieses $inject wird ignoriert, d.h, der Parameter der Funktion muss exact dem Namen des Services entsprechen (Grossbuchstaben am Anfang). Warum?
getInstitutionen.$inject = ['InstitutionRS'];

/* @ngInject */
function getInstitutionen(InstitutionRS: InstitutionRS) {
    return InstitutionRS.getAllActiveInstitutionen();
}

// FIXME dieses $inject wird ignoriert, d.h, der Parameter der Funktion muss exact dem Namen des Services entsprechen (Grossbuchstaben am Anfang). Warum?
getTraegerschaften.$inject = ['TraegerschaftRS'];

/* @ngInject */
function getTraegerschaften(TraegerschaftRS: TraegerschaftRS) {
    return TraegerschaftRS.getAllActiveTraegerschaften();
}

// FIXME dieses $inject wird ignoriert, d.h, der Parameter der Funktion muss exact dem Namen des Services entsprechen (Grossbuchstaben am Anfang). Warum?
getMandant.$inject = ['MandantRS'];

/* @ngInject */
function getMandant(MandantRS: MandantRS) {
    return MandantRS.getFirst();
}
