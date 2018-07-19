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
import {MatDialogModule, MatSelectModule} from '@angular/material';
import {TranslateModule} from '@ngx-translate/core';
import {DvNgErrorMessages} from '../core/component/dv-error-messages/dv-ng-error-messages';
import {DvNgGemeindeDialogComponent} from '../core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {DvNgShowElementDirective} from '../core/directive/dv-ng-show-element/dv-ng-show-element.directive';
import {DvNgDebounceClickDirective} from '../core/directive/dv-ng-debounce-click/dv-ng-debounce-click.directive';

@NgModule({
    imports: [
        TranslateModule,
        CommonModule,
        MatDialogModule,
        MatSelectModule,
        FormsModule,
    ],
    declarations: [
        DvNgDebounceClickDirective,
        DvNgErrorMessages,
        DvNgShowElementDirective,
        DvNgGemeindeDialogComponent,
    ],
    entryComponents: [
        DvNgErrorMessages,
        DvNgGemeindeDialogComponent,
    ],
    exports: [
        DvNgDebounceClickDirective,
        DvNgErrorMessages,
        DvNgShowElementDirective,
        DvNgGemeindeDialogComponent,
        MatSelectModule,
        FormsModule,
    ],
})
export class NgSharedModule {
}


