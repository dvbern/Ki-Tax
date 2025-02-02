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
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

import {FinanzielleSituationAufteilungComponent} from './finanzielle-situation-aufteilung.component';

describe('FinanzielleSituationAufteilungComponent', () => {
    let component: FinanzielleSituationAufteilungComponent;
    let fixture: ComponentFixture<FinanzielleSituationAufteilungComponent>;

    const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
        GesuchModelManager.name,
        ['getGesuch']
    );
    const finanzielleSituationRSSpy =
        jasmine.createSpyObj<FinanzielleSituationRS>(
            FinanzielleSituationRS.name,
            ['updateFromAufteilung']
        );

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [FinanzielleSituationAufteilungComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {
                    provide: FinanzielleSituationRS,
                    useValue: finanzielleSituationRSSpy
                },
                {provide: NgForm, useValue: new NgForm([], [])}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        gesuchModelManagerSpy.getGesuch.and.returnValue(createGesuch());
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(
            FinanzielleSituationAufteilungComponent
        );
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    function createGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        const finSitContainer1 = new TSFinanzielleSituationContainer();
        const finSitContainer2 = new TSFinanzielleSituationContainer();
        finSitContainer1.finanzielleSituationJA = new TSFinanzielleSituation();
        finSitContainer2.finanzielleSituationJA = new TSFinanzielleSituation();
        gesuch.gesuchsteller1 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller1.finanzielleSituationContainer = finSitContainer1;
        gesuch.gesuchsteller2 = new TSGesuchstellerContainer();
        gesuch.gesuchsteller2.finanzielleSituationContainer = finSitContainer2;
        return gesuch;
    }
});
