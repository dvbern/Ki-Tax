import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import ErrorService from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {GemeindeModule} from '../../gemeinde/gemeinde.module';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {TraegerschaftModule} from '../traegerschaft.module';

import {TraegerschaftEditComponent} from './traegerschaft-edit.component';

describe('TraegerschaftEditComponent', () => {

  let component: TraegerschaftEditComponent;
  let fixture: ComponentFixture<TraegerschaftEditComponent>;

    const traegerschaftRSSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name,
        ['findTraegerschaft']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);

  beforeEach(async(() => {
      TestBed.configureTestingModule({
          imports: [
              SharedModule,
              NoopAnimationsModule,
              MaterialModule,
              TraegerschaftModule,
          ],
          schemas: [CUSTOM_ELEMENTS_SCHEMA],
          providers: [
              {provide: Transition, useValue: transitionSpy},
              {provide: StateService, useValue: stateServiceSpy},
              {provide: ErrorService, useValue: errorServiceSpy},
              {provide: TraegerschaftRS, useValue: traegerschaftRSSpy},
          ],
          declarations: [
          ],
      }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
      ).compileComponents();

      transitionSpy.params.and.returnValue({});
      transitionSpy.from.and.returnValue({});
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TraegerschaftEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
