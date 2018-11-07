import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {TraegerschaftEditComponent} from './traegerschaft-edit/traegerschaft-edit.component';
import {TraegerschaftListComponent} from './traegerschaft-list/traegerschaft-list.component';
import {TraegerschaftRoutingModule} from './traegerschaft-routing/traegerschaft-routing.module';

@NgModule({
    imports: [
        SharedModule,
        TraegerschaftRoutingModule,
        MaterialModule,
    ],
    // adding custom elements schema disables Angular's element validation: you can now use transclusion for the
    // dv-accordion-tab with multi-slot transclusion (tab-title & tab-body elements).
    // See https://stackoverflow.com/a/51214263
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    declarations: [
        TraegerschaftEditComponent,
        TraegerschaftListComponent,
    ],
    entryComponents: [
        TraegerschaftEditComponent,
        TraegerschaftListComponent,
    ],
    providers: [],
})
export class TraegerschaftModule {
}
