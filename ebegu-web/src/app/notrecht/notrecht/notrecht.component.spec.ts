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
import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateModule} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {NotrechtRoutingModule} from '../notrecht-routing/notrecht-routing.module';
import {NotrechtModule} from '../notrecht.module';

import {NotrechtComponent} from './notrecht.component';

describe('NotrechtComponent', () => {
    let component: NotrechtComponent;
    let fixture: ComponentFixture<NotrechtComponent>;
    const notrechtRSSpy = jasmine.createSpyObj<NotrechtRS>(NotrechtRS.name,
        ['initializeRueckforderungFormulare', 'getRueckforderungFormulareForCurrentBenutzer']);
    const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isRole', 'isOneOfRoles']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['addMesageAsInfo']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const downloadRSSpy =  jasmine.createSpyObj<DownloadRS>(DownloadRS.name, ['prepareDownloadWindow']);

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                NotrechtRoutingModule,
                TranslateModule,
                MaterialModule,
                SharedModule,
                NotrechtModule,
                NoopAnimationsModule
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                WindowRef,
                {provide: NotrechtRS, useValue: notrechtRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: DownloadRS, useValue: downloadRSSpy},
            ],
            declarations: [],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();
        notrechtRSSpy.getRueckforderungFormulareForCurrentBenutzer.and.resolveTo([]);
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(NotrechtComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
