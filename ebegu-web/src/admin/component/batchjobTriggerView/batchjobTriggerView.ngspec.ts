/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {MatDatepickerModule, MatRadioModule, MatSelectModule, MatSortModule} from '@angular/material';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {MatTableModule} from '@angular/material/table';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import {createTranslateLoader} from '../../../ngApp/ng-app.module';
import {NgSharedModule} from '../../../shared/ng-shared.module';
import {DailyBatchRS} from '../../service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../../service/databaseMigrationRS.rest';
import {BatchjobTriggerViewComponent} from './batchjobTriggerView';

describe('batchjobTriggerView', function () {

    let component: BatchjobTriggerViewComponent;
    let fixture: ComponentFixture<BatchjobTriggerViewComponent>;

    beforeEach(async(() => {
        const dvDialogSpy = jasmine.createSpyObj('DvDialog', ['showDialog']);
        const databaseMigrationRSSpy = jasmine.createSpyObj('DatabaseMigrationRS', ['processScript']);
        const dailyBatchRSSpy = jasmine.createSpyObj('DailyBatchRS', ['runBatchMahnungFristablauf']);

        TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useFactory: (createTranslateLoader),
                        deps: [HttpClient]
                    }
                }),
                FormsModule,
                MatTableModule,
                MatSortModule,
                NoopAnimationsModule, // we don't want material animations in the project yet
                NgSharedModule,
                CommonModule,
                MatRadioModule,
                MatDatepickerModule,
                MatMomentDateModule,
                MatSelectModule,
            ],
            providers: [
                {provide: DvDialog, useValue: dvDialogSpy},
                {provide: DatabaseMigrationRS, useValue: databaseMigrationRSSpy},
                {provide: DailyBatchRS, useValue: dailyBatchRSSpy},
            ],
            declarations: [BatchjobTriggerViewComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(BatchjobTriggerViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
