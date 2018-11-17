/*
 * AGPL File-Header
 *
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
import {MatPaginator} from '@angular/material';
import {StammdatenHeaderComponent} from '../shared/component/stammdaten-header/stammdaten-header.component';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {AddInstitutionComponent} from './add-institution/add-institution.component';
import {EditInstitutionComponent} from './edit-institution/edit-institution.component';
import {InstitutionRoutingModule} from './institution-routing/institution-routing.module';
import {InstitutionListComponent} from './institutionenListView/institution-list.component';

@NgModule({
    imports: [
        SharedModule,
        InstitutionRoutingModule,
        MaterialModule,
    ],
    declarations: [
        InstitutionListComponent,
        EditInstitutionComponent,
        AddInstitutionComponent,
        StammdatenHeaderComponent,
    ],
    entryComponents: [
        InstitutionListComponent,
        EditInstitutionComponent,
        AddInstitutionComponent,
        StammdatenHeaderComponent,
    ],
    providers: [],
})
export class InstitutionModule {
}
