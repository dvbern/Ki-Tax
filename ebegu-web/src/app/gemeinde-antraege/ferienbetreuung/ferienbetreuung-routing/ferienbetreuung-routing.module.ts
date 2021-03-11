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

import {NgModule} from '@angular/core';
import {NgHybridStateDeclaration, UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {Transition} from '@uirouter/core';
import {FerienbetreuungAngebotComponent} from '../ferienbetreuung-angebot/ferienbetreuung-angebot.component';
import {FerienbetreuungFreigabeComponent} from '../ferienbetreuung-freigabe/ferienbetreuung-freigabe.component';
import {FerienbetreuungKostenEinnahmenComponent} from '../ferienbetreuung-kosten-einnahmen/ferienbetreuung-kosten-einnahmen.component';
import {FerienbetreuungNutzungComponent} from '../ferienbetreuung-nutzung/ferienbetreuung-nutzung.component';
import {FerienbetreuungStammdatenGemeindeComponent} from '../ferienbetreuung-stammdaten-gemeinde/ferienbetreuung-stammdaten-gemeinde.component';
import {FerienbetreuungUploadComponent} from '../ferienbetreuung-upload/ferienbetreuung-upload.component';
import {FerienbetreuungVerfuegungComponent} from '../ferienbetreuung-verfuegung/ferienbetreuung-verfuegung.component';
import {FerienbetreuungComponent} from '../ferienbetreuung/ferienbetreuung.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'FERIENBETREUUNG',
        url: '/ferienbetreuung/:id',
        component: FerienbetreuungComponent,
        resolve: [
            {
                token: 'ferienbetreuungId',
                deps: [Transition],
                resolveFn: (trans: Transition) =>
                    (trans.params().id),
            },
        ],
    },
    {
        name: 'FERIENBETREUUNG.STAMMDATEN_GEMEINDE',
        url: '/stammdaten-gemeinde',
        component: FerienbetreuungStammdatenGemeindeComponent,
    },
    {
        name: 'FERIENBETREUUNG.ANGEBOT',
        url: '/angebot',
        component: FerienbetreuungAngebotComponent,
    },
    {
        name: 'FERIENBETREUUNG.NUTZUNG',
        url: '/nutzung',
        component: FerienbetreuungNutzungComponent,
    },
    {
        name: 'FERIENBETREUUNG.KOSTEN_EINNAHMEN',
        url: '/kosten-einnahmen',
        component: FerienbetreuungKostenEinnahmenComponent,
    },
    {
        name: 'FERIENBETREUUNG.UPLOAD',
        url: '/upload',
        component: FerienbetreuungUploadComponent,
    },
    {
        name: 'FERIENBETREUUNG.FREIGABE',
        url: '/freigabe',
        component: FerienbetreuungFreigabeComponent,
    },
    {
        name: 'FERIENBETREUUNG.VERFUEGUNG',
        url: '/verfuegung',
        component: FerienbetreuungVerfuegungComponent,
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
export class FerienbetreuungRoutingModule {
}
