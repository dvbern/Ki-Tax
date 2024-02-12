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

import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {UploadRS} from '../../../core/service/uploadRS.rest';
import {LastenausgleichRS} from '../../services/lastenausgleichRS.rest';

import {LastenausgleichViewXComponent} from './lastenausgleich-view-x.component';

const translateSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name, ['instant']);
const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name,
    ['prepareDownloadWindow', 'startDownload']);
const uploadRSSpy = jasmine.createSpyObj<UploadRS>(UploadRS.name, ['uploadZemisExcel']);
const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles', 'isRole']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['isDevMode']);
const matDialogSpy = jasmine.createSpyObj<MatDialog>(MatDialog.name, ['open']);
const lastenausgleichSpy = jasmine.createSpyObj<LastenausgleichRS>(LastenausgleichRS.name,
    ['getAllLastenausgleiche']);

describe('LastenausgleichViewXComponent', () => {
    let component: LastenausgleichViewXComponent;
    let fixture: ComponentFixture<LastenausgleichViewXComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [LastenausgleichViewXComponent],
            providers: [
                {provide: TranslateService, useValue: translateSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: UploadRS, useValue: uploadRSSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                {provide: MatDialog, useValue: matDialogSpy},
                {provide: LastenausgleichRS, useValue: lastenausgleichSpy}
            ],
            imports: [
                HttpClientModule
            ]
        })
            .compileComponents();

        applicationPropertyRSSpy.isDevMode.and.returnValue(of(true).toPromise());
        lastenausgleichSpy.getAllLastenausgleiche.and.returnValue(of([]));
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(LastenausgleichViewXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
