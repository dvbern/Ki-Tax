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

import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeModule} from '../gemeinde.module';
import {EditGemeindeStammdatenComponent} from './edit-gemeinde-stammdaten.component';

describe('EditGemeindeStammdatenComponent', () => {

    let component: EditGemeindeStammdatenComponent;
    let fixture: ComponentFixture<EditGemeindeStammdatenComponent>;

    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
            ['getPublicPropertiesCached']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isRole']);

    beforeEach(waitForAsync(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
                GemeindeModule
            ],
            schemas: [],
            providers: [
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy}
            ],
            declarations: [
            ]
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES
        ).compileComponents();

    }));

    beforeEach(waitForAsync(() => {
        const properties = new TSPublicAppConfig();
        applicationPropertyRSSpy.getPublicPropertiesCached.and.returnValue(of(properties).toPromise());
        fixture = TestBed.createComponent(EditGemeindeStammdatenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
