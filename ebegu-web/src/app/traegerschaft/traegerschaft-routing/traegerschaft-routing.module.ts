import {NgModule} from '@angular/core';
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {IPromise} from 'angular';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {TraegerschaftEditComponent} from '../traegerschaft-edit/traegerschaft-edit.component';
import {TraegerschaftListComponent} from '../traegerschaft-list/traegerschaft-list.component';

const states: Ng2StateDeclaration[] = [
    {
        parent: 'app',
        name: 'traegerschaft',
        abstract: true,
        url: '/traegerschaft',
        component: UiViewComponent,
    },
    {
        name: 'traegerschaft.list',
        url: '/list',
        component: TraegerschaftListComponent,
        resolve: [
            {
                token: 'traegerschaften',
                deps: [TraegerschaftRS],
                resolveFn: getTraegerschaften,
            },
        ],
        data: {
            roles: TSRoleUtil.getMandantRoles(),
        },
    },
    {
        name: 'traegerschaft.edit',
        url: '/edit/:traegerschaftId',
        component: TraegerschaftEditComponent,
        data: {
            roles: TSRoleUtil.getMandantRoles(),
        },
    },
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
})
export class TraegerschaftRoutingModule {
}

function getTraegerschaften(traegerschaftRS: TraegerschaftRS): IPromise<TSTraegerschaft[]> {
    return traegerschaftRS.getAllActiveTraegerschaften();
}

