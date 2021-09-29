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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BatchJobRS} from '../../core/service/batchRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {ReportAsyncRS} from '../../core/service/reportAsyncRS.rest';

import {StatistikComponent} from './statistik.component';

const gesuchsperiodeRSSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
    ['getAllGesuchsperioden']);
const institutionStammdatenRSSpy = jasmine.createSpyObj<InstitutionStammdatenRS>(InstitutionStammdatenRS.name,
    ['getAllTagesschulenForCurrentBenutzer']);
const reportAsyncSpy = jasmine.createSpyObj<ReportAsyncRS>(ReportAsyncRS.name,
    []);
const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name,
    ['prepareDownloadWindow', 'startDownload']);
const batchJobRSSpy = jasmine.createSpyObj<BatchJobRS>(BatchJobRS.name,
    ['getBatchJobsOfUser']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);
const translateSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name, ['instant']);
const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);
const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenWithMahlzeitenverguenstigungForBenutzer']);

describe('StatistikComponent', () => {
    let component: StatistikComponent;
    let fixture: ComponentFixture<StatistikComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [StatistikComponent],
            providers: [
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRSSpy},
                {provide: InstitutionStammdatenRS, useValue: institutionStammdatenRSSpy},
                {provide: ReportAsyncRS, useValue: reportAsyncSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: BatchJobRS, useValue: batchJobRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TranslateService, useValue: translateSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(StatistikComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
