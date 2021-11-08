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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatToolbarModule} from '@angular/material/toolbar';
import {TranslateModule} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {WindowRef} from '../../core/service/windowRef.service';
import {WizardStepXRS} from '../../core/service/wizardStepXRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {WizardSideNavComponent} from './wizard-side-nav.component';

describe('WizardSideNavComponent', () => {
    let component: WizardSideNavComponent;
    let fixture: ComponentFixture<WizardSideNavComponent>;
    const wizardSTepXRS = jasmine.createSpyObj<WizardStepXRS>(WizardStepXRS.name, {
        getAllSteps: of([]),
    });
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                WindowRef,
                {
                    provide: WizardStepXRS,
                    useValue: wizardSTepXRS,
                },
            ],
            declarations: [WizardSideNavComponent],
            imports: [
                UIRouterModule.forRoot({useHash: true}),
                TranslateModule,
                MatToolbarModule,
                SharedModule,
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(WizardSideNavComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
