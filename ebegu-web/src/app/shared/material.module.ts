import {NgModule} from '@angular/core';

import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatButtonModule} from '@angular/material/button';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatDialogModule} from '@angular/material/dialog';
import {MatDividerModule} from '@angular/material/divider';
import {MatInputModule} from '@angular/material/input';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import { MatRadioModule} from '@angular/material/radio';
import {MatSelectModule} from '@angular/material/select';
import {MatSortModule} from '@angular/material/sort';
import {MatLegacyTableModule as MatTableModule} from '@angular/material/legacy-table';
import {MatLegacyTabsModule as MatTabsModule} from '@angular/material/legacy-tabs';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatLegacyTooltipModule as MatTooltipModule} from '@angular/material/legacy-tooltip';
import {MatMomentDateModule} from '@angular/material-moment-adapter';

const MATERIAL_MODULES = [
    MatAutocompleteModule,
     MatButtonModule,
    // MatButtonToggleModule,
    // MatCardModule,
    MatCheckboxModule,
    // MatChipsModule,
    MatDatepickerModule,
    MatDialogModule,
    MatDividerModule,
    // MatExpansionModule,
    // MatGridListModule,
    // MatIconModule,
    MatInputModule,
    // MatListModule,
    // MatMenuModule,
    MatPaginatorModule,
    MatProgressBarModule,
    // MatProgressSpinnerModule,
    MatRadioModule,
    // MatRippleModule,
    MatSelectModule,
    // MatSidenavModule,
    // MatSliderModule,
    // MatSlideToggleModule,
    // MatSnackBarModule,
    MatSortModule,
    // MatStepperModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule
    // MatBottomSheetModule
];

@NgModule({
    imports: [MatMomentDateModule, ...MATERIAL_MODULES],
    exports: [MatMomentDateModule, ...MATERIAL_MODULES],
    providers: [
        { provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { subscriptSizing: 'dynamic' } }
    ]
})
export class MaterialModule {
}
