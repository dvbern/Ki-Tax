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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatSortModule} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateModule} from '@ngx-translate/core';
import {dvDialogProvider, errorServiceProvider, traegerschaftRSProvider} from '../hybridTools/ajs-upgraded-providers';
import {TraegerschaftViewComponent} from './component/traegerschaftView/traegerschaftView';
import {NgAdminRoutingModule} from './ng-admin-routing.module';
import {MatTableModule} from '@angular/material/table';


@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        NgAdminRoutingModule,
        FormsModule,
        MatTableModule,
        MatSortModule,
        NoopAnimationsModule, // we don't want material animations in the project yet
    ],
    declarations: [
        TraegerschaftViewComponent,
    ],
    entryComponents: [
        TraegerschaftViewComponent,
    ],
    providers: [
        traegerschaftRSProvider,
        errorServiceProvider,
        dvDialogProvider,
    ],
})
export class NgAdminModule {
}

