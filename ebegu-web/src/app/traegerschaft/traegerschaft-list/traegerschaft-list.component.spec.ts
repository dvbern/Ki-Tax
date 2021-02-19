/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {SharedModule} from '../../shared/shared.module';

import { TraegerschaftListComponent } from './traegerschaft-list.component';

describe('TraegerschaftListComponent', () => {
  let component: TraegerschaftListComponent;
  let fixture: ComponentFixture<TraegerschaftListComponent>;

  beforeEach(waitForAsync(() => {
      const traegerschaftServiceSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name,
          ['createTraegerschaft', 'getAllActiveTraegerschaften']);
      const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
      const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles', 'isRole']);
      const dvDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
      const stateServiceSpy = jasmine.createSpyObj(StateService.name, ['go']);
      const i18nServiceSpy = jasmine
          .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);

      TestBed.configureTestingModule({
          imports: [
              SharedModule,
              NoopAnimationsModule,
          ],
          providers: [
              {provide: TraegerschaftRS, useValue: traegerschaftServiceSpy},
              {provide: ErrorService, useValue: errorServiceSpy},
              {provide: AuthServiceRS, useValue: authServiceSpy},
              {provide: MatDialog, useValue: dvDialogSpy},
              {provide: StateService, useValue: stateServiceSpy},
              {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
          ],
          declarations: [TraegerschaftListComponent],
      })
          .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
          .compileComponents();
      traegerschaftServiceSpy.getAllActiveTraegerschaften.and.returnValue(Promise.resolve([]));
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TraegerschaftListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
