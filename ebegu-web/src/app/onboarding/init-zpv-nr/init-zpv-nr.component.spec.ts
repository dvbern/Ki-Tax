import { ComponentFixture, TestBed } from '@angular/core/testing';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';

import { InitZpvNrComponent } from './init-zpv-nr.component';

describe('InitZpvNrComponent', () => {
  let component: InitZpvNrComponent;
  let fixture: ComponentFixture<InitZpvNrComponent>;
  const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['burnPortalTimeout']);
  authServiceSpy.burnPortalTimeout.and.returnValue(Promise.resolve(''));
  const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['target']);
  const uiRouterGlobalsSpy = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name,
    ['params']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
        providers: [
            WindowRef,
            {provide: AuthServiceRS, useValue: authServiceSpy},
            {provide: StateService, useValue: stateServiceSpy},
            {provide: UIRouterGlobals, useValue: uiRouterGlobalsSpy},
        ],
        declarations: [ InitZpvNrComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InitZpvNrComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
