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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FerienbetreuungKommantarComponent} from './ferienbetreuung-kommantar/ferienbetreuung-kommantar.component';
import {FerienbetreuungRoutingModule} from './ferienbetreuung-routing/ferienbetreuung-routing.module';
import {FerienbetreuungUiViewComponent} from './ferienbetreuung-ui-view/ferienbetreuung-ui-view.component';
import {FerienbetreuungComponent} from './ferienbetreuung/ferienbetreuung.component';
import {FerienbetreuungService} from './services/ferienbetreuung.service';

@NgModule({
    declarations: [
        FerienbetreuungComponent,
        FerienbetreuungKommantarComponent,
        FerienbetreuungUiViewComponent,
    ],
    imports: [
        CommonModule,
        FerienbetreuungRoutingModule,
    ],
    providers: [
        FerienbetreuungService
    ]
})
export class FerienbetreuungModule {
}
