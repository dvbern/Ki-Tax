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

import {NgModule} from '@angular/core';
import {
    NgHybridStateDeclaration,
    UIRouterUpgradeModule
} from '@uirouter/angular-hybrid';
import {getBgInstitutionenBetreuungsangebote} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {AddInstitutionComponent} from '../add-institution/add-institution.component';
import {EditInstitutionComponent} from '../edit-institution/edit-institution.component';
import {InstitutionListComponent} from '../list-institution/institution-list.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'institution',
        abstract: true,
        url: '/institution',
        component: UiViewComponent
    },
    {
        name: 'institution.list',
        url: '/list',
        component: InstitutionListComponent,
        data: {
            roles: TSRoleUtil.getInstitutionProfilRoles()
        }
    },
    {
        name: 'institution.add',
        url: '/add',
        component: AddInstitutionComponent,
        data: {
            roles: TSRoleUtil.getInstitutionProfilRoles()
        },
        params: {
            betreuungsangebote: {
                type: 'any',
                value: getBgInstitutionenBetreuungsangebote()
            },
            betreuungsangebot: {
                type: 'any'
            },
            latsOnly: {
                type: 'any',
                value: false
            }
        }
    },
    {
        name: 'institution.edit',
        url: '/edit/:institutionId/:isRegistering',
        component: EditInstitutionComponent,
        data: {
            roles: TSRoleUtil.getInstitutionProfilRoles()
        },
        params: {
            isRegistering: {
                type: 'bool',
                // this parameter is optional: specify a default value
                value: false
            },
            editMode: {
                type: 'bool',
                value: false
            }
        }
    }
];

@NgModule({
    imports: [UIRouterUpgradeModule.forChild({states})],
    exports: [UIRouterUpgradeModule]
})
export class InstitutionRoutingModule {}
