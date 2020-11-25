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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LastenausgleichTsKommentarComponent} from './lastenausgleich-ts-kommentar.component';

describe('LastenausgleichTsKommentarComponent', () => {
  let component: LastenausgleichTsKommentarComponent;
  let fixture: ComponentFixture<LastenausgleichTsKommentarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTsKommentarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTsKommentarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
