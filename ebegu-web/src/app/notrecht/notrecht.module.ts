/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {MaterialModule} from '../shared/material.module';
import {NotrechtRoutingModule} from './notrecht-routing/notrecht-routing.module';
import {NotrechtComponent} from './notrecht/notrecht.component';
import {RueckforderungFormularComponent} from './rueckforderung-formular/rueckforderung-formular.component';

@NgModule({
    declarations: [
        NotrechtComponent,
        RueckforderungFormularComponent
    ],
    imports: [
        CommonModule,
        NotrechtRoutingModule,
        TranslateModule,
        MaterialModule,
    ],
    providers: []
})
export class NotrechtModule {
}
