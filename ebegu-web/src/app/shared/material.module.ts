import {NgModule} from '@angular/core';
import {
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatPaginatorModule,
    MatRadioModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
} from '@angular/material';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {MatTooltipModule} from '@angular/material/tooltip';

const MATERIAL_MODULES = [
    // MatAutocompleteModule,
    // MatButtonModule,
    // MatButtonToggleModule,
    // MatCardModule,
    MatCheckboxModule,
    // MatChipsModule,
    MatDatepickerModule,
    MatDialogModule,
    // MatDividerModule,
    // MatExpansionModule,
    // MatGridListModule,
    // MatIconModule,
    // MatInputModule,
    // MatListModule,
    // MatMenuModule,
    MatPaginatorModule,
    // MatProgressBarModule,
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
    // MatToolbarModule,
    MatTooltipModule,
    // MatBottomSheetModule
];

@NgModule({
    imports: [MatMomentDateModule, ...MATERIAL_MODULES],
    exports: [MatMomentDateModule, ...MATERIAL_MODULES],
})
export class MaterialModule {
}
