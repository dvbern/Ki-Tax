import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DvWizardStepsComponent } from './dv-wizard-steps.component';

describe('DvWizardStepsComponent', () => {
  let component: DvWizardStepsComponent;
  let fixture: ComponentFixture<DvWizardStepsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DvWizardStepsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DvWizardStepsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
