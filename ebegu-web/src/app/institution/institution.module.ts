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
import {IbanModule} from 'ngx-iban';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {AddInstitutionComponent} from './add-institution/add-institution.component';
import {EditInstitutionBetreuungsgutscheineComponent} from './edit-institution-betreuungsgutscheine/edit-institution-betreuungsgutscheine.component';
import {EditInstitutionFerieninselComponent} from './edit-institution-ferieninsel/edit-institution-ferieninsel.component';
import {EditInstitutionTagesschuleComponent} from './edit-institution-tagesschule/edit-institution-tagesschule.component';
import {EditInstitutionComponent} from './edit-institution/edit-institution.component';
import {InstitutionRoutingModule} from './institution-routing/institution-routing.module';
import {InstitutionListComponent} from './list-institution/institution-list.component';
import {ViewInstitutionBetreuungsgutscheineComponent} from './view-institution-betreuungsgutscheine/view-institution-betreuungsgutscheine.component';
import {ViewInstitutionFerieninselComponent} from './view-institution-ferieninsel/view-institution-ferieninsel.component';
import {ViewInstitutionTagesschuleComponent} from './view-institution-tagesschule/view-institution-tagesschule.component';

@NgModule({
    imports: [
        SharedModule,
        InstitutionRoutingModule,
        MaterialModule,
        IbanModule,
    ],
    declarations: [
        InstitutionListComponent,
        EditInstitutionComponent,
        AddInstitutionComponent,
        EditInstitutionBetreuungsgutscheineComponent,
        ViewInstitutionBetreuungsgutscheineComponent,
        EditInstitutionTagesschuleComponent,
        ViewInstitutionTagesschuleComponent,
        EditInstitutionFerieninselComponent,
        ViewInstitutionFerieninselComponent,
    ],
    entryComponents: [
        InstitutionListComponent,
        EditInstitutionComponent,
        AddInstitutionComponent,
        EditInstitutionBetreuungsgutscheineComponent,
        ViewInstitutionBetreuungsgutscheineComponent,
        EditInstitutionTagesschuleComponent,
        ViewInstitutionTagesschuleComponent,
        EditInstitutionFerieninselComponent,
        ViewInstitutionFerieninselComponent,
    ],
    providers: [],
})
export class InstitutionModule {
}
