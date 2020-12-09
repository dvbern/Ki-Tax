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

import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../../app/shared/shared.module';
import {DailyBatchRS} from '../../service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../../service/databaseMigrationRS.rest';
import {BatchjobTriggerViewComponent} from './batchjobTriggerView';

describe('batchjobTriggerView', () => {

    let component: BatchjobTriggerViewComponent;
    let fixture: ComponentFixture<BatchjobTriggerViewComponent>;

    beforeEach(waitForAsync(() => {
        const dvDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
        const databaseMigrationRSSpy = jasmine.createSpyObj('DatabaseMigrationRS', ['processScript']);
        const dailyBatchRSSpy = jasmine.createSpyObj('DailyBatchRS', ['runBatchMahnungFristablauf']);
        const i18nServiceSpy = jasmine
            .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: MatDialog, useValue: dvDialogSpy},
                {provide: DatabaseMigrationRS, useValue: databaseMigrationRSSpy},
                {provide: DailyBatchRS, useValue: dailyBatchRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
            ],
            declarations: [BatchjobTriggerViewComponent],
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
