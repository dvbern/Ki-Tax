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
import {TranslateService} from '@ngx-translate/core';
import {createClient} from '../../../../models/TSExternalClient';
import {TSExternalClientAssignment} from '../../../../models/TSExternalClientAssignment';

import {ExternalClientAssignmentComponent} from './external-client-assignment.component';
import SpyObj = jasmine.SpyObj;

describe('ExternalClientAssignmentComponent', () => {
    let component: ExternalClientAssignmentComponent;
    let fixture: ComponentFixture<ExternalClientAssignmentComponent>;
    let translateServiceSpy: SpyObj<TranslateService>;

    beforeEach(waitForAsync(() => {
        translateServiceSpy = jasmine.createSpyObj<TranslateService>(TranslateService.name, ['instant']);
        TestBed.configureTestingModule({
            providers: [
                {
                    provide: TranslateService,
                    useValue: translateServiceSpy,
                },
            ],
            declarations: [ExternalClientAssignmentComponent],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ExternalClientAssignmentComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display KEINE when no assignment', () => {
        translateServiceSpy.instant.and.returnValue('none');
        component.ngOnChanges({externalClients: new SimpleChange(undefined, new TSExternalClientAssignment(), true)});
        expect(translateServiceSpy.instant).toHaveBeenCalledWith('LABEL_KEINE');
        expect(component.assignedClients).toBe('none');
    });

    it('should concat clients by name', () => {
        const clients = new TSExternalClientAssignment();
        clients.assignedClients = [createClient('foo'), createClient('bar')];

        component.ngOnChanges({externalClients: new SimpleChange(undefined, clients, true)});
        expect(component.assignedClients).toBe('bar, foo');
    });
});
