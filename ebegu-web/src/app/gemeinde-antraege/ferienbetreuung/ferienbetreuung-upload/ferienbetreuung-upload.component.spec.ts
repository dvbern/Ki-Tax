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
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSBenutzer} from '../../../../models/TSBenutzer';
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
const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['getPrincipal', 'isOneOfRoles']);

authServiceSpy.principal$ = of(new TSBenutzer());
const applicationPropertyRSSpy =
    jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['isDevMode', 'getAllowedMimetypes']);
const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);

const container = new TSFerienbetreuungAngabenContainer();
container.angabenDeklaration = null;

describe('FerienbetreuungUploadComponent', () => {
    let component: FerienbetreuungUploadComponent;
    let fixture: ComponentFixture<FerienbetreuungUploadComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungUploadComponent],
            imports: [
                SharedModule
            ],
            providers: [
                {provide: FerienbetreuungService, useValue: ferienbetreuungServiceSpy},
                {provide: FerienbetreuungDokumentService, useValue: ferienbetreuungDokumentServiceSpy},
                {provide: UploadRS, useValue: uploadRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        ferienbetreuungServiceSpy.getFerienbetreuungContainer
            .and.returnValue(of(new TSFerienbetreuungAngabenContainer()));
        ferienbetreuungDokumentServiceSpy.getAllDokumente.and.returnValue(of([]));
        applicationPropertyRSSpy.getAllowedMimetypes.and.returnValue(Promise.resolve(''));
        fixture = TestBed.createComponent(FerienbetreuungUploadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
