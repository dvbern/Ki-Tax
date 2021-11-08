/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {TSFile} from '../../../../models/TSFile';
import {ApplicationPropertyRS} from '../../../core/rest-services/applicationPropertyRS.rest';
import {WindowRef} from '../../../core/service/windowRef.service';
import {MaterialModule} from '../../material.module';
import {SharedModule} from '../../shared.module';

import {FileUploadComponent} from './file-upload.component';

describe('FileUploadComponent', () => {
    let component: FileUploadComponent<TSFile>;
    let fixture: ComponentFixture<FileUploadComponent<TSFile>>;

    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['getAllowedMimetypes']);

    beforeEach(waitForAsync(() => {
        applicationPropertyRSSpy.getAllowedMimetypes.and.returnValue(of('').toPromise());

        TestBed.configureTestingModule({
                imports: [
                    SharedModule,
                    NoopAnimationsModule,
                    MaterialModule,
                ],
                schemas: [CUSTOM_ELEMENTS_SCHEMA],
                providers: [
                    WindowRef,
                    {provide: NgForm, useValue: new NgForm([], [])},
                    {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
                ],
            }
        )
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(FileUploadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
