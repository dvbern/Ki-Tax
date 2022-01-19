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
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {BerechnungsManager} from '../../../../service/berechnungsManager';

import {SelbstdeklarationComponent} from './selbstdeklaration.component';

describe('SelbstdeklarationComponent', () => {
  let component: SelbstdeklarationComponent;
  let fixture: ComponentFixture<SelbstdeklarationComponent>;
  const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(BerechnungsManager.name, ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']);
  berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(Promise.resolve(new TSFinanzielleSituationResultateDTO()));

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelbstdeklarationComponent ],
      providers: [
        {provide: NgForm, useValue: new NgForm([], [])},
        {provide: BerechnungsManager, useValue: berechnungsManagerSpy}
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelbstdeklarationComponent);
    component = fixture.componentInstance;
    component.model = new TSFinanzielleSituationContainer();
    component.model.finanzielleSituationJA = new TSFinanzielleSituation();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
