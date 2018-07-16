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
import {MAT_DATE_LOCALE, MatDatepickerModule, MatRadioModule, MatSelectModule, MatSortModule} from '@angular/material';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {MatTableModule} from '@angular/material/table';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../core/errors/service/ErrorService';
import GesuchsperiodeRS from '../../../core/service/gesuchsperiodeRS.rest';
import UserRS from '../../../core/service/userRS.rest';
import ZahlungRS from '../../../core/service/zahlungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import GesuchRS from '../../../gesuch/service/gesuchRS.rest';
import {createTranslateLoader} from '../../../ngApp/ng-app.module';
import {NgSharedModule} from '../../../shared/ng-shared.module';
import {ApplicationPropertyRS} from '../../service/applicationPropertyRS.rest';
import {DailyBatchRS} from '../../service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../../service/databaseMigrationRS.rest';
import {TestFaelleRS} from '../../service/testFaelleRS.rest';
import {TestdatenViewComponent} from './testdatenView';

describe('testdatenView', function () {

    let component: TestdatenViewComponent;
    let fixture: ComponentFixture<TestdatenViewComponent>;

    beforeEach(async(() => {
        const testFaelleRSSpy = jasmine.createSpyObj('TestFaelleRS', ['createTestFall', 'createTestFallGS', 'removeFaelleOfGS', 'mutiereFallHeirat',
                'mutiereFallScheidung', 'resetSchulungsdaten', 'deleteSchulungsdaten']);
        testFaelleRSSpy.createTestFall.and.returnValue('idOfCreatedGesuch');
        const dvDialogSpy = jasmine.createSpyObj('DvDialog', ['showDialog']);
        const userRSSpy = jasmine.createSpyObj('UserRS', ['getAllGesuchsteller']);
        userRSSpy.getAllGesuchsteller.and.returnValue(Promise.resolve(true));
        const errorServiceSpy = jasmine.createSpyObj('ErrorService', ['addMesageAsInfo']);
        const gesuchsperiodeRSSpy = jasmine.createSpyObj('GesuchsperiodeRS', ['getAllGesuchsperioden', 'removeGesuchsperiode']);
        gesuchsperiodeRSSpy.getAllGesuchsperioden.and.returnValue(Promise.resolve(true));
        const databaseMigrationRSSpy = jasmine.createSpyObj('DatabaseMigrationRS', ['processScript']);
        const zahlungRSSpy = jasmine.createSpyObj('ZahlungRS', ['zahlungenKontrollieren', 'deleteAllZahlungsauftraege']);
        const applicationPropertyRSSpy = jasmine.createSpyObj('ApplicationPropertyRS', ['isDevMode']);
        applicationPropertyRSSpy.isDevMode.and.returnValue(Promise.resolve(true));
        const gesuchRSSpy = jasmine.createSpyObj('GesuchRS', ['gesuchVerfuegen']);
        const dailyBatchRSSpy = jasmine.createSpyObj('DailyBatchRS', ['runBatchMahnungFristablauf']);
        const gemeindeRSSpy = jasmine.createSpyObj('GemeindeRS', ['getAllGemeinden']);
        gemeindeRSSpy.getAllGemeinden.and.returnValue(Promise.resolve(true));

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
                {provide: TestFaelleRS, useValue: testFaelleRSSpy},
                {provide: DvDialog, useValue: dvDialogSpy},
                {provide: UserRS, useValue: userRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRSSpy},
                {provide: DatabaseMigrationRS, useValue: databaseMigrationRSSpy},
                {provide: ZahlungRS, useValue: zahlungRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                {provide: GesuchRS, useValue: gesuchRSSpy},
                {provide: DailyBatchRS, useValue: dailyBatchRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: MAT_DATE_LOCALE, useValue: 'de-CH'},
            ],
            declarations: [TestdatenViewComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TestdatenViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
