import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {SharedModule} from '../../../shared/shared.module';

import {DvRadioContainerXComponent} from './dv-radio-container-x.component';

describe('DvRadioContainerXComponent', () => {
  let component: DvRadioContainerXComponent;
  let fixture: ComponentFixture<DvRadioContainerXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DvRadioContainerXComponent ]
    })
    .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DvRadioContainerXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
