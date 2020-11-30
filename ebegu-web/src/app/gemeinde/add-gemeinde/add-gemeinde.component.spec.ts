/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import {StateService, Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {AddGemeindeComponent} from './add-gemeinde.component';

describe('AddGemeindeComponent', () => {

    let component: AddGemeindeComponent;
    let fixture: ComponentFixture<AddGemeindeComponent>;

    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
        ['getGemeindenForPrincipal$', 'findGemeinde', 'getUnregisteredBfsGemeinden']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const benutzerServiceSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['removeBenutzer']);
    const einstellungServiceSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name, ['saveEinstellung']);
    const gesuchsperiodeServiceSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
        ['getAllGesuchsperioden']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, [
        'getPrincipal',
        'hasMandantAngebotTS'
    ]);

    beforeEach(waitForAsync(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: BenutzerRS, useValue: benutzerServiceSpy},
                {provide: EinstellungRS, useValue: einstellungServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeServiceSpy},
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [
                AddGemeindeComponent,
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of(
            [TestDataUtil.createGemeindeParis(), TestDataUtil.createGemeindeLondon()]));
        gemeindeServiceSpy.getUnregisteredBfsGemeinden.and.resolveTo([]);
        transitionSpy.params.and.returnValue({});
        gesuchsperiodeServiceSpy.getAllGesuchsperioden.and.resolveTo([]);
        authServiceSpy.getPrincipal.and.returnValue(TestDataUtil.createSuperadmin());
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(AddGemeindeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});
