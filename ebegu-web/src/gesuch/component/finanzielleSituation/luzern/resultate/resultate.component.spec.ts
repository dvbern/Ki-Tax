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
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';

import {ResultateComponent} from './resultate.component';

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name, ['areThereOnlyFerieninsel', 'getBasisjahr', 'getBasisjahrPlus']);
const wizardStepMangerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name, ['getCurrentStep', 'setCurrentStep']);

describe('ResultateComponent', () => {
    let component: ResultateComponent;
    let fixture: ComponentFixture<ResultateComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                ResultateComponent,
            ],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: WizardStepManager, useValue: wizardStepMangerSpy}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ResultateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
