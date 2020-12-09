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

import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {NgxIbanModule} from 'ngx-iban';
import {NgxMaterialTimepickerModule} from 'ngx-material-timepicker';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {AddInstitutionComponent} from './add-institution/add-institution.component';
import {EditBetreuungsstandortComponent} from './betreuungsstandort/edit-betreuungsstandort.component';
import {EditInstitutionBetreuungsgutscheineComponent} from './edit-institution-betreuungsgutscheine/edit-institution-betreuungsgutscheine.component';
import {EditInstitutionFerieninselComponent} from './edit-institution-ferieninsel/edit-institution-ferieninsel.component';
import {DialogImportFromOtherInstitution} from './edit-institution-tagesschule/dialog-import-from-other-institution/dialog-import-from-other-institution.component';
import {EditInstitutionTagesschuleComponent} from './edit-institution-tagesschule/edit-institution-tagesschule.component';
import {EditInstitutionComponent} from './edit-institution/edit-institution.component';
import {ModulTagesschuleDialogComponent} from './edit-modul-tagesschule/modul-tagesschule-dialog.component';
import {InstitutionRoutingModule} from './institution-routing/institution-routing.module';
import {InstitutionListComponent} from './list-institution/institution-list.component';

@NgModule({
    imports: [
        SharedModule,
        InstitutionRoutingModule,
        MaterialModule,
        NgxIbanModule,
        NgxMaterialTimepickerModule,
    ],
    // adding custom elements schema disables Angular's element validation: you can now use transclusion for the
    // dv-accordion-tab with multi-slot transclusion (tab-title & tab-body elements).
    // See https://stackoverflow.com/a/51214263
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    declarations: [
        InstitutionListComponent,
        EditInstitutionComponent,
        AddInstitutionComponent,
        EditInstitutionBetreuungsgutscheineComponent,
        EditInstitutionTagesschuleComponent,
        EditInstitutionFerieninselComponent,
        ModulTagesschuleDialogComponent,
        DialogImportFromOtherInstitution,
        EditBetreuungsstandortComponent
    ],
    providers: [],
})
export class InstitutionModule {
}
