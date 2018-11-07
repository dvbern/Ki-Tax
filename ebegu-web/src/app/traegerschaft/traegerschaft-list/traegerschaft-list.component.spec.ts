import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import {MatDialog} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import ErrorService from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {SharedModule} from '../../shared/shared.module';

import { TraegerschaftListComponent } from './traegerschaft-list.component';

describe('TraegerschaftListComponent', () => {
  let component: TraegerschaftListComponent;
  let fixture: ComponentFixture<TraegerschaftListComponent>;

  beforeEach(async(() => {
      const traegerschaftServiceSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name,
          ['createTraegerschaft']);
      const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
      const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);
      const dvDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
      const stateServiceSpy = jasmine.createSpyObj(StateService.name, ['go']);

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
              {provide: StateService, useValue: stateServiceSpy}
          ],
          declarations: [TraegerschaftListComponent],
      })
          .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
          .compileComponents();
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
