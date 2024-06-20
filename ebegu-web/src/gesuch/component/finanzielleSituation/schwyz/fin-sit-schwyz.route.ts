/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import {ILogService, IPromise, IQService} from 'angular';
import {RouterHelper} from '../../../../dvbModules/router/route-helper-provider';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {FinanzielleSituationGsSchwyzComponent} from './finanzielle-situation-gs-schwyz/finanzielle-situation-gs-schwyz.component';
import {FinanzielleSituationResultateSchwyzComponent} from './finanzielle-situation-resultate-schwyz/finanzielle-situation-resultate-schwyz.component';
import {FinanzielleSituationStartSchwyzComponent} from './finanzielle-situation-start-schwyz/finanzielle-situation-start-schwyz.component';

finSitSchwyzRun.$inject = ['RouterHelper'];

export function finSitSchwyzRun(routerHelper: RouterHelper): void {
    routerHelper.configureStates(ng1States);
}

const kommentarView = '<kommentar-view>';

export class EbeguFinanzielleSituationStartSchwyzState
    implements Ng1StateDeclaration
{
    public name = 'gesuch.finanzielleSituationStartSchwyz';
    public url = '/finanzielleSituationSchywz/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationStartSchwyzComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguFinanzielleSituationGS1SchwyzState
    implements Ng1StateDeclaration
{
    public name = 'gesuch.finanzielleSituationSchwyzGS1';
    public url = '/finanzielleSituationSchywz/:gesuchstellerNumber/:gesuchId';

    public params: {
        gesuchstellerNumber: 1;
    };

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationGsSchwyzComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguFinanzielleSituationGS2SchwyzState
    implements Ng1StateDeclaration
{
    public name = 'gesuch.finanzielleSituationSchwyzGS2';
    public url = '/finanzielleSituationSchywz/:gesuchstellerNumber/:gesuchId';

    public params: {
        gesuchstellerNumber: 1;
    };

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationGsSchwyzComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguFinanzielleSituationResultateSchwyzState
    implements Ng1StateDeclaration
{
    public name = 'gesuch.finanzielleSituationSchwyzResultate';
    public url = '/finanzielleSituationSchywz/:gesuchId/resultate';

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationResultateSchwyzComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

const ng1States: Ng1StateDeclaration[] = [
    new EbeguFinanzielleSituationStartSchwyzState(),
    new EbeguFinanzielleSituationGS1SchwyzState(),
    new EbeguFinanzielleSituationGS2SchwyzState(),
    new EbeguFinanzielleSituationResultateSchwyzState()
];

export class IGesuchStateParams {
    public gesuchId: string;
}

getGesuchModelManager.$inject = [
    'GesuchModelManager',
    'BerechnungsManager',
    '$stateParams',
    '$q',
    '$log'
];

export function getGesuchModelManager(
    gesuchModelManager: GesuchModelManager,
    berechnungsManager: BerechnungsManager,
    $stateParams: IGesuchStateParams,
    $q: IQService,
    $log: ILogService
): IPromise<TSGesuch> {
    if ($stateParams) {
        const gesuchIdParam = $stateParams.gesuchId;
        if (gesuchIdParam) {
            if (
                !gesuchModelManager.getGesuch() ||
                (gesuchModelManager.getGesuch() &&
                    gesuchModelManager.getGesuch().id !== gesuchIdParam) ||
                gesuchModelManager.getGesuch().emptyCopy
            ) {
                // Wenn die antrags id im GescuchModelManager nicht mit der GesuchId ueberreinstimmt wird das gesuch
                // neu geladen Ebenfalls soll das Gesuch immer neu geladen werden, wenn es sich beim Gesuch im
                // Gesuchmodelmanager um eine leere Mutation handelt oder um ein leeres Erneuerungsgesuch
                berechnungsManager.clear();
                return gesuchModelManager.openGesuch(gesuchIdParam);
            }

            return $q.resolve(gesuchModelManager.getGesuch());
        }
    }
    $log.warn('keine stateParams oder keine gesuchId, gebe undefined zurueck');
    return $q.resolve(undefined);
}
