/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UIRouterModule} from '@uirouter/angular';
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import RouterModule from '../../../../dvbModules/router/router.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSPublicAppConfig} from '../../../../models/TSPublicAppConfig';
import {I18nServiceRSRest} from '../../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../../shared/shared.module';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';

import {PulldownUserMenuComponent} from './pulldown-user-menu.component';

describe('PulldownUserMenuComponent', () => {
    let component: PulldownUserMenuComponent;
    let fixture: ComponentFixture<PulldownUserMenuComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['getPrincipalRole', 'isOneOfRoles']
    );
    const appPropSpy = jasmine.createSpyObj<ApplicationPropertyRS>(
        ApplicationPropertyRS.name,
        ['getPublicPropertiesCached']
    );
    const stateServiceSpy = jasmine.createSpyObj<StateService>(
        StateService.name,
        ['href']
    );
    stateServiceSpy.href.and.returnValue('');
    appPropSpy.getPublicPropertiesCached.and.resolveTo({} as TSPublicAppConfig);
    authServiceSpy.principal$ = of(new TSBenutzer());
    authServiceSpy.isOneOfRoles.and.returnValue(true);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            providers: [
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: ApplicationPropertyRS, useValue: appPropSpy},
                {provide: StateService, useValue: stateServiceSpy}
            ],
            declarations: [PulldownUserMenuComponent],
            imports: [SharedModule, UIRouterModule.forRoot()]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PulldownUserMenuComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
