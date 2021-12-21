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
import {ListResourceRS} from '../../../../../app/core/service/listResourceRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

import {InfomaFieldsComponent} from './infoma-fields.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(GesuchModelManager.name,
    ['getGesuch', 'getFamiliensituation']);
const listResourceRSSpy = jasmine.createSpyObj<ListResourceRS>(ListResourceRS.name,
    ['getLaenderList']);
const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles']);

describe('InfomaFieldsComponent', () => {
    let component: InfomaFieldsComponent;
    let fixture: ComponentFixture<InfomaFieldsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                InfomaFieldsComponent
            ],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: ListResourceRS, useValue: listResourceRSSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy},
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(InfomaFieldsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
