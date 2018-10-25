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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import ErrorService from '../../core/errors/service/ErrorService';
import BenutzerRS from '../../core/service/benutzerRS.rest';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeKonfigComponent} from '../gemeinde-konfiguration/gemeinde-konfig.component';
import {EditGemeindeComponent} from './edit-gemeinde.component';

describe('EditGemeindeComponent', () => {

    let component: EditGemeindeComponent;
    let fixture: ComponentFixture<EditGemeindeComponent>;

    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
        ['getGemeindenForPrincipal$', 'findGemeinde']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const benutzerServiceSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['findBenutzerByEmail']);
    const einstellungServiceSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name, ['saveEinstellung']);
    const gesuchsperiodeServiceSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
        ['getAllGesuchsperioden']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: BenutzerRS, useValue: benutzerServiceSpy},
                {provide: EinstellungRS, useValue: einstellungServiceSpy},
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeServiceSpy},
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
            ],
            declarations: [
                EditGemeindeComponent,
                GemeindeKonfigComponent,
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();

        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of(
            [TestDataUtil.createGemeindeBern(), TestDataUtil.createGemeindeOstermundigen()]));
        transitionSpy.params.and.returnValue({});
        transitionSpy.from.and.returnValue({});
        gesuchsperiodeServiceSpy.getAllGesuchsperioden.and.returnValue(Promise.resolve([]));
    }));

    beforeEach(async(() => {
        fixture = TestBed.createComponent(EditGemeindeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', async(() => {
        expect(component).toBeTruthy();
    }));
});
