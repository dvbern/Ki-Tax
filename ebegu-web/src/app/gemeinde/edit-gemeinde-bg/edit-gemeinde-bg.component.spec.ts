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
import {Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedDirective';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeModule} from '../gemeinde.module';
import {EditGemeindeBGComponent} from './edit-gemeinde-bg.component';

describe('EditGemeindeBGComponent', () => {
    let component: EditGemeindeBGComponent;
    let fixture: ComponentFixture<EditGemeindeBGComponent>;
    const user = new TSBenutzer();

    const i18nServiceSpy = jasmine.createSpyObj<I18nServiceRSRest>(
        I18nServiceRSRest.name,
        ['extractPreferredLanguage']
    );

    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, [
        'params'
    ]);

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        {
            getPrincipalRole: TSRole.SUPER_ADMIN,
            getPrincipal: user,
            isRole: false,
            isOneOfRoles: false
        }
    );
    authServiceSpy.principal$ = of(user) as any;

    const einstellungRSSpy = jasmine.createSpyObj<EinstellungRS>(
        EinstellungRS.name,
        ['findEinstellungByKey', 'findEinstellung']
    );

    const applicationPropertyRSSpy =
        jasmine.createSpyObj<ApplicationPropertyRS>(
            ApplicationPropertyRS.name,
            ['getPublicPropertiesCached']
        );
    applicationPropertyRSSpy.getPublicPropertiesCached.and.resolveTo({} as any);

    const institutionRSSpy = jasmine.createSpyObj(InstitutionRS.name, [
        'getAllBgInstitutionen'
    ]);

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [SharedModule, MaterialModule, GemeindeModule],
            schemas: [],
            providers: [
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: Transition, useValue: transitionSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: EinstellungRS, useValue: einstellungRSSpy},
                {
                    provide: ApplicationPropertyRS,
                    useValue: applicationPropertyRSSpy
                },
                {provide: InstitutionRS, useValue: institutionRSSpy}
            ],
            declarations: []
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(EditGemeindeBGComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
