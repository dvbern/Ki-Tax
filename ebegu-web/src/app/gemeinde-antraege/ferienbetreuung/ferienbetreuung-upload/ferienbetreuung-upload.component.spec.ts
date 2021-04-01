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
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {UploadRS} from '../../../core/service/uploadRS.rest';
import {SharedModule} from '../../../shared/shared.module';
import {FerienbetreuungDokumentService} from '../services/ferienbetreuung-dokument.service';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

import {FerienbetreuungUploadComponent} from './ferienbetreuung-upload.component';

const ferienbetreuungServiceSpy = jasmine.createSpyObj<FerienbetreuungService>(FerienbetreuungService.name,
    ['getFerienbetreuungContainer']);
const ferienbetreuungDokumentServiceSpy = jasmine.createSpyObj<FerienbetreuungDokumentService>(
    FerienbetreuungDokumentService.name, ['getAllDokumente']
);
const uploadRSSpy = jasmine.createSpyObj<UploadRS>(UploadRS.name, ['uploadFerienbetreuungDokumente']);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsError']);
const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name,
    ['prepareDownloadWindow', 'getAccessTokenFerienbetreuungDokument', 'startDownload']);
const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, []);

describe('FerienbetreuungUploadComponent', () => {
    let component: FerienbetreuungUploadComponent;
    let fixture: ComponentFixture<FerienbetreuungUploadComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungUploadComponent],
            imports: [
                SharedModule,
            ],
            providers: [
                {provide: FerienbetreuungService, useValue: ferienbetreuungServiceSpy},
                {provide: FerienbetreuungDokumentService, useValue: ferienbetreuungDokumentServiceSpy},
                {provide: UploadRS, useValue: uploadRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FerienbetreuungUploadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
