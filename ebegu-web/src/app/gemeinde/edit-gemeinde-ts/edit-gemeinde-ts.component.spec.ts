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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {EditGemeindeComponentTS} from './edit-gemeinde-ts.component';

describe('EditGemeindeComponentTS', () => {

    let component: EditGemeindeComponentTS;
    let fixture: ComponentFixture<EditGemeindeComponentTS>;

    const insitutionSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name, ['getInstitutionenForGemeinde']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            declarations:
                [EditGemeindeComponentTS],
            imports: [MaterialModule, TranslateModule.forRoot(), UpgradeModule, BrowserAnimationsModule],
            schemas:
                [CUSTOM_ELEMENTS_SCHEMA],
            providers:
                [
                    {provide: InstitutionRS, useValue: insitutionSpy},
                    {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                    {provide: StateService, useValue: stateServiceSpy}
                ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(EditGemeindeComponentTS);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
