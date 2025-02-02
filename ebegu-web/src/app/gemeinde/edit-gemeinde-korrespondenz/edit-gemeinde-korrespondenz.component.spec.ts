/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeModule} from '../gemeinde.module';
import {EditGemeindeKorrespondenzComponent} from './edit-gemeinde-korrespondenz.component';

describe('EditGemeindeKorrespondenzComponent', () => {
    let component: EditGemeindeKorrespondenzComponent;
    let fixture: ComponentFixture<EditGemeindeKorrespondenzComponent>;

    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, [
        'downloadMusterDokument'
    ]);
    const downloadRSSpy = jasmine.createSpyObj<DownloadRS>(DownloadRS.name, [
        'openDownload'
    ]);
    const translateRSSpy = jasmine.createSpyObj<TranslateService>(
        TranslateService.name,
        ['instant', 'setDefaultLang', 'use']
    );

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule, MaterialModule, GemeindeModule],
            schemas: [],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
                {provide: TranslateService, useValue: translateRSSpy}
            ],
            declarations: []
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(EditGemeindeKorrespondenzComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
