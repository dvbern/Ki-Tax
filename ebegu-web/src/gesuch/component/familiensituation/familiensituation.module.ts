/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
import {CommonModule} from '@angular/common';
import {WarningComponent} from '../../../app/shared/component/warning/warning.component';
import {SharedModule} from '../../../app/shared/shared.module';
import {DvNgGsRemovalConfirmationDialogComponent} from './dv-ng-gs-removal-confirmation-dialog/dv-ng-gs-removal-confirmation-dialog.component';
import {FamiliensituationAppenzellViewXComponent} from './familiensituation-appenzell-view-x/familiensituation-appenzell-view-x.component';
import {FamiliensituationSchwyzComponent} from './familiensituation-schwyz/familiensituation-schwyz.component';
import {FamiliensituationViewXComponent} from './familiensituation-view-x/familiensituation-view-x.component';

@NgModule({
    declarations: [
        DvNgGsRemovalConfirmationDialogComponent,
        FamiliensituationViewXComponent,
        FamiliensituationAppenzellViewXComponent,
        FamiliensituationSchwyzComponent
    ],
    imports: [CommonModule, SharedModule, WarningComponent]
})
export class FamiliensituationModule {}
