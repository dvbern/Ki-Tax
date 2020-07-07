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

import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {NotrechtRoutingModule} from './notrecht-routing/notrecht-routing.module';
import {NotrechtComponent} from './notrecht/notrecht.component';
import {RueckforderungFormularComponent} from './rueckforderung-formular/rueckforderung-formular.component';
import {RueckforderungMitteilungenComponent} from './rueckforderung-mitteilung/rueckforderung-mitteilungen.component';
import {SendNotrechtMitteilungComponent} from './send-notrecht-mitteilung/send-notrecht-mitteilung.component';
import { RueckforderungVerlaengerungDialogComponent } from './rueckforderung-formular/rueckforderung-verlaengerung-dialog/rueckforderung-verlaengerung-dialog.component';

@NgModule({
    declarations: [
        NotrechtComponent,
        RueckforderungFormularComponent,
        SendNotrechtMitteilungComponent,
        RueckforderungMitteilungenComponent,
        RueckforderungVerlaengerungDialogComponent
    ],
    imports: [
        MaterialModule,
        NotrechtRoutingModule,
        TranslateModule,
        SharedModule
    ],
    providers: [],
    entryComponents: [
        SendNotrechtMitteilungComponent,
        RueckforderungVerlaengerungDialogComponent
    ],
    // used for dv-accordion
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class NotrechtModule {
}
