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
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';

import {ListSozialdienstComponent} from './list-sozialdienst.component';

describe('ListSozialdienstComponent', () => {
    let component: ListSozialdienstComponent;
    let fixture: ComponentFixture<ListSozialdienstComponent>;

    const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const sozialdienstRSSpy = jasmine.createSpyObj<SozialdienstRS>(SozialdienstRS.name,
        ['getSozialdienstList']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ListSozialdienstComponent],
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
            ],
            providers: [
                WindowRef,
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: SozialdienstRS, useValue: sozialdienstRSSpy},
            ],
        })
            .compileComponents();
        sozialdienstRSSpy.getSozialdienstList.and.returnValue(of([]));
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ListSozialdienstComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
