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

import {SimpleChange} from '@angular/core';
import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {createClient} from '../../../../models/TSExternalClient';
import {TSExternalClientAssignment} from '../../../../models/TSExternalClientAssignment';
import {WindowRef} from '../../../core/service/windowRef.service';
import {MaterialModule} from '../../material.module';
import {SharedModule} from '../../shared.module';

import {ExternalClientMultiselectComponent} from './external-client-multiselect.component';

describe('ExternalClientMultiselectComponent', () => {
    let component: ExternalClientMultiselectComponent;
    let fixture: ComponentFixture<ExternalClientMultiselectComponent>;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
            ],
            providers: [
                WindowRef,
                {provide: NgForm, useValue: new NgForm([], [])},
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ExternalClientMultiselectComponent);
        component = fixture.componentInstance;
        component.externalClients = new TSExternalClientAssignment();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have empty options when no clients', () => {
        expect(component.options).toEqual([]);
    });

    it('should merge assigned and available clients and sort them as options', () => {
        const foo = createClient('foo');
        const bar = createClient('bar');
        const returns = createClient('returns');

        const clients = new TSExternalClientAssignment();
        clients.assignedClients = [foo, bar];
        clients.availableClients = [returns];

        component.ngOnChanges({externalClients: new SimpleChange(component.externalClients, clients, false)});

        expect(component.options).toEqual([bar, foo, returns]);
    });
});
