/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';

import {GesuchsperiodeListViewXComponent} from './gesuchsperiode-list-view-x.component';

describe('GesuchsperiodeListViewXComponent', () => {
    let component: GesuchsperiodeListViewXComponent;
    let fixture: ComponentFixture<GesuchsperiodeListViewXComponent>;

    const gesuchsperiodeRSSpy = jasmine.createSpyObj<GesuchsperiodeRS>(
        GesuchsperiodeRS.name,
        ['getAllGesuchsperioden']
    );
    const stateServiceSpy = jasmine.createSpyObj<StateService>(
        StateService.name,
        ['go']
    );
    const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['isOneOfRoles']
    );

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [GesuchsperiodeListViewXComponent],
            providers: [
                {provide: GesuchsperiodeRS, useValue: gesuchsperiodeRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: StateService, useValue: stateServiceSpy}
            ]
        }).compileComponents();
        gesuchsperiodeRSSpy.getAllGesuchsperioden.and.resolveTo([]);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(GesuchsperiodeListViewXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
