/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedDirective';
import {SharedModule} from '../../../shared/shared.module';
import {DVInputContainerXComponent} from './dv-input-container-x.component';

describe('DvInputContainerX', () => {
    let component: DVInputContainerXComponent;
    let fixture: ComponentFixture<DVInputContainerXComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DVInputContainerXComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DVInputContainerXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
