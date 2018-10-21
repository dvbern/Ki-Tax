/*
 * AGPL File-Header
 *
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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import ErrorService from '../../core/errors/service/ErrorService';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {ViewGemeindeComponent} from './view-gemeinde.component';

describe('ViewGemeindeComponent', () => {

    let component: ViewGemeindeComponent;
    let fixture: ComponentFixture<ViewGemeindeComponent>;

    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
        ['getGemeindenForPrincipal$', 'findGemeinde']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const einstellungServiceSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name, ['saveEinstellung']);
    const gesuchsperiodeServiceSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
        ['getAllGesuchsperioden']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: EinstellungRS, useValue: einstellungServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
            ],
            declarations: [
                ViewGemeindeComponent,
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();

        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of(
            [TestDataUtil.createGemeindeBern(), TestDataUtil.createGemeindeOstermundigen()]));
        transitionSpy.params.and.returnValue({});
        gesuchsperiodeServiceSpy.getAllGesuchsperioden.and.returnValue(Promise.resolve([]));
    }));

    beforeEach(async(() => {
        fixture = TestBed.createComponent(ViewGemeindeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', async(() => {
        expect(component).toBeTruthy();
    }));
});
