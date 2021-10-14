/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BatchJobRS} from '../../core/service/batchRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {ReportAsyncRS} from '../../core/service/reportAsyncRS.rest';
import {GemeindeModule} from '../../gemeinde/gemeinde.module';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';

import {StatistikComponent} from './statistik.component';

describe('StatistikComponent', () => {
    let component: StatistikComponent;
    let fixture: ComponentFixture<StatistikComponent>;

    const gesuchsperiodeRSSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
        ['getAllGesuchsperioden']);
    const institutionStammdatenRSSpy = jasmine.createSpyObj<InstitutionStammdatenRS>(InstitutionStammdatenRS.name,
        ['getAllTagesschulenForCurrentBenutzer', 'getBetreuungsangeboteForInstitutionenOfCurrentBenutzer']);
    const reportAsyncSpy = jasmine.createSpyObj<ReportAsyncRS>(ReportAsyncRS.name,
        ['getGesuchStichtagReportExcel']);
    const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name,
        ['prepareDownloadWindow', 'startDownload']);
    const batchJobRSSpy = jasmine.createSpyObj<BatchJobRS>(BatchJobRS.name,
        ['getBatchJobsOfUser']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);
    const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
        ['isOneOfRoles', 'isRole']);
    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenWithMahlzeitenverguenstigungForBenutzer']);

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule, NoopAnimationsModule, MaterialModule, GemeindeModule],
            schemas: [],
            providers: [
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRSSpy},
                {provide: InstitutionStammdatenRS, useValue: institutionStammdatenRSSpy},
                {provide: ReportAsyncRS, useValue: reportAsyncSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: BatchJobRS, useValue: batchJobRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
            ],
            declarations: [StatistikComponent
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        gesuchsperiodeRSSpy.getAllGesuchsperioden.and.returnValue(Promise.resolve([]));
        institutionStammdatenRSSpy.getAllTagesschulenForCurrentBenutzer.and.returnValue(Promise.resolve([]));
        gemeindeRSSpy.getGemeindenWithMahlzeitenverguenstigungForBenutzer.and.returnValue(Promise.resolve([]));
        institutionStammdatenRSSpy.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer.and.returnValue(Promise.resolve([]));
        batchJobRSSpy.getBatchJobsOfUser.and.returnValue(of([]));
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(StatistikComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    // Leider geht es nicht, wir wissen nicht genau warum, eventuel muss der dv-accordion migriert werden
    xit('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
