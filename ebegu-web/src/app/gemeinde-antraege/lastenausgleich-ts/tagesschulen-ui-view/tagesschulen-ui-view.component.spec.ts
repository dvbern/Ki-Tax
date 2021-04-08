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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService, TransitionService} from '@uirouter/angular';

import {TagesschulenUiViewComponent} from './tagesschulen-ui-view.component';

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go', 'is']);

const transitionServiceSpy = jasmine.createSpyObj<TransitionService>(TransitionService.name,
    ['onSuccess']);

describe('TagesschulenUiViewComponent', () => {
    let component: TagesschulenUiViewComponent;
    let fixture: ComponentFixture<TagesschulenUiViewComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TagesschulenUiViewComponent],
            providers: [
                {provide: StateService, useValue: stateServiceSpy},
                {provide: TransitionService, useValue: transitionServiceSpy},
            ],
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TagesschulenUiViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
