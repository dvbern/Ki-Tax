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
import {MAT_DATE_LOCALE, MatDatepickerModule, MatRadioModule, MatSelectModule, MatSortModule} from '@angular/material';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateModule} from '@ngx-translate/core';
import {
    dailyBatchRSProvider,
    databaseMigrationRSProvider,
    dvDialogProvider,
    errorServiceProvider,
    gemeindeRSProvider,
    gesuchRSProvider,
    gesuchsperiodeRSProvider,
    testFaelleRSProvider,
    traegerschaftRSProvider,
    userRSProvider,
    zahlungRSProvider
} from '../hybridTools/ajs-upgraded-providers';
import {NgSharedModule} from '../shared/ng-shared.module';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';
import {TraegerschaftViewComponent} from './component/traegerschaftView/traegerschaftView';
import {NgAdminRoutingModule} from './ng-admin-routing.module';
import {MatTableModule} from '@angular/material/table';

@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        NgAdminRoutingModule,
        MatTableModule,
        MatSortModule,
        MatRadioModule,
        MatDatepickerModule,
        MatMomentDateModule,
        MatSelectModule,
        NoopAnimationsModule, // we don't want material animations in the project yet
        NgSharedModule,
    ],
    declarations: [
        TraegerschaftViewComponent,
        TestdatenViewComponent,
        BatchjobTriggerViewComponent
    ],
    entryComponents: [
        TraegerschaftViewComponent,
        TestdatenViewComponent,
        BatchjobTriggerViewComponent
    ],
    providers: [
        traegerschaftRSProvider,
        testFaelleRSProvider,
        userRSProvider,
        databaseMigrationRSProvider,
        zahlungRSProvider,
        gesuchRSProvider,
        dailyBatchRSProvider,
        errorServiceProvider,
        dvDialogProvider,
        gesuchsperiodeRSProvider,
        {provide: MAT_DATE_LOCALE, useValue: 'de-CH'},
        gemeindeRSProvider
    ],
})
export class NgAdminModule {
}

