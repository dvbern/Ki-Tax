/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {Ng1StateDeclaration, StateParams} from '@uirouter/angularjs';
import {StateService} from '@uirouter/core';
import {delay, take} from 'rxjs/operators';
import {FamiliensituationVisitor} from '../../../app/core/constants/FamiliensituationVisitor';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {RouterHelper} from '../../../dvbModules/router/route-helper-provider';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {getGesuchModelManager} from '../../gesuch.route';
import {FamiliensituationAppenzellViewXComponent} from './familiensituation-appenzell-view-x/familiensituation-appenzell-view-x.component';
import {FamiliensituationSchwyzComponent} from './familiensituation-schwyz/familiensituation-schwyz.component';
import {FamiliensituationViewXComponent} from './familiensituation-view-x/familiensituation-view-x.component';

familiensituationRun.$inject = ['RouterHelper'];
export function familiensituationRun(routerHelper: RouterHelper): void {
    routerHelper.configureStates([
        new EbeguFamiliensituationState(),
        new EbeguFamiliensituationDefaultState(),
        new EbeguFamiliensituationAppenzellState(),
        new EbeguFamiliensituationSchwyzState()
    ]);
}

const kommentarView = '<kommentar-view';

class EbeguFamiliensituationState implements Ng1StateDeclaration {
    public name = 'gesuch.familiensituation';
    public url = '/familiensituation-route/:gesuchId';
    public onEnter = redirectToFamiliensituation;
}

redirectToFamiliensituation.$inject = [
    'MandantService',
    '$state',
    '$stateParams'
];
function redirectToFamiliensituation(
    mandantService: MandantService,
    $state: StateService,
    $stateParams: StateParams
) {
    mandantService.mandant$.pipe(take(1), delay(1)).subscribe(mandant => {
        const route = new FamiliensituationVisitor().process(mandant);
        $state.transitionTo(route, {gesuchId: $stateParams.gesuchId});
    });
}

class EbeguFamiliensituationDefaultState implements Ng1StateDeclaration {
    public name = 'gesuch.familiensituation-default';
    public url = '/familiensituation/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FamiliensituationViewXComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuch: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous()
    };
}

class EbeguFamiliensituationAppenzellState implements Ng1StateDeclaration {
    public name = 'gesuch.familiensituation-appenzell';
    public url = '/familiensituation-ar/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FamiliensituationAppenzellViewXComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuch: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous()
    };
}

class EbeguFamiliensituationSchwyzState implements Ng1StateDeclaration {
    public name = 'gesuch.familiensituation-schwyz';
    public url = '/familiensituation-sz/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FamiliensituationSchwyzComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuch: getGesuchModelManager
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous()
    };
}
