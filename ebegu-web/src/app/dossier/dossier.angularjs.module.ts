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

import {StateProvider, Transition} from '@uirouter/angularjs';
import * as angular from 'angular';
import {DossierRS} from '../../gesuch/service/dossierRS.rest';
import {TSDossier} from '../../models/TSDossier';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {CORE_JS_MODULE} from '../core/core.angularjs.module';
import IPromise = angular.IPromise;

export const DOSSIER_JS_MODULE = angular.module('ebeguWebDossier', [
        CORE_JS_MODULE.name
    ])
        .config(conf)
;

conf.$inject = ['$stateProvider'];

function conf($stateProvider: StateProvider): void {
    $stateProvider.state(
        {
            parent: 'app',
            name: 'dossier',
            abstract: true,
            url: '/dossier/:dossierId',
            resolve: {
                dossier
            },
            data: {
                roles: TSRoleUtil.getAllRolesButAnonymous()
            },
            params: {
                dossierId: {
                    // wir brauchen einen default value, weil der landing state gesuchsteller.dashboard normalerweise
                    // ohne Parameter aufgerufen wird. D.h. in diesem Fall wird einfach das aktuelle Dossier des
                    // Benutzers geladen.
                    // In der URL wird die ID des Dossiers nur angezeigt, wenn man die dossierId explizit spezifiziert.
                    // Mit etwas Zusatzaufwand könnte man die ID in die URL schreiben (dynamic parameter).
                    value: '',
                    squash: true
                }
            }
        }
    );
}

dossier.$inject = ['$transition$', 'DossierRS'];

export function dossier($transition$: Transition, dossierService: DossierRS): IPromise<TSDossier> {
    const dossierId: string = $transition$.params().dossierId;

    if (!dossierId) {
        return dossierService.findNewestDossierByCurrentBenutzerAsBesitzer();
    }

    return dossierService.findDossier(dossierId);
}
