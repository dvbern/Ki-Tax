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
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateModule} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {NotrechtRoutingModule} from '../notrecht-routing/notrecht-routing.module';
import {NotrechtModule} from '../notrecht.module';
import {RueckforderungFormularComponent} from './rueckforderung-formular.component';

describe('RueckforderungFormularComponent', () => {
    let component: RueckforderungFormularComponent;
    let fixture: ComponentFixture<RueckforderungFormularComponent>;

    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const notrechtRSSpy = jasmine.createSpyObj<NotrechtRS>(NotrechtRS.name, ['findRueckforderungFormular']);

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                MaterialModule,
                NotrechtRoutingModule,
                TranslateModule,
                NotrechtModule
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                WindowRef,
                {provide: Transition, useValue: transitionSpy},
                {provide: NotrechtRS, useValue: notrechtRSSpy},
            ],
            declarations: [],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();
        transitionSpy.params.and.returnValue({});
        transitionSpy.from.and.returnValue({});
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RueckforderungFormularComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
