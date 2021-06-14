/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService} from '@uirouter/angular';
import {UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {GesuchRS} from '../../gesuch/service/gesuchRS.rest';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {AntragStatusHistoryRS} from '../core/service/antragStatusHistoryRS.rest';

import {VerlaufComponent} from './verlauf.component';

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go']);
const gesuchRSSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name,
    ['getAllAntragDTOForDossier', 'getAllAntragDTOForDossier']);
const antragStatusHistoryRSSpy = jasmine.createSpyObj<AntragStatusHistoryRS>(AntragStatusHistoryRS.name,
    ['loadAllAntragStatusHistoryByGesuchsperiode']);
const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);
const uiRouterGlobalsSpy = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name,
    ['params']);
const ebeguUtilSpy = jasmine.createSpyObj<EbeguUtil>(EbeguUtil.name,
    ['getAntragTextDateAsString']);

describe('VerlaufComponent', () => {
    let component: VerlaufComponent;
    let fixture: ComponentFixture<VerlaufComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [VerlaufComponent],
            providers: [
                {provide: StateService, useValue: stateServiceSpy},
                {provide: GesuchRS, useValue: gesuchRSSpy},
                {provide: AntragStatusHistoryRS, useValue: antragStatusHistoryRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: UIRouterGlobals, useValue: uiRouterGlobalsSpy},
                {provide: EbeguUtil, useValue: ebeguUtilSpy},
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(VerlaufComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
