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
import {NgForm} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MaterialModule} from '../../../../../app/shared/material.module';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {VeranlagungComponent} from './veranlagung.component';

describe('VeranlagungComponent', () => {
    let component: VeranlagungComponent;
    let fixture: ComponentFixture<VeranlagungComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [VeranlagungComponent],
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
            ],
            providers: [
                {provide: NgForm, useValue: new NgForm([], [])},
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(VeranlagungComponent);
        component = fixture.componentInstance;
        component.model = createFinSitContainer();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createFinSitContainer(): TSFinanzielleSituationContainer {
        const finanzielleSituationContainer = new TSFinanzielleSituationContainer();
        finanzielleSituationContainer.finanzielleSituationJA = new TSFinanzielleSituation();
        return finanzielleSituationContainer;
    }
});
