import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DvRadioContainerXComponent} from './dv-radio-container-x.component';

describe('DvRadioContainerXComponent', () => {
  let component: DvRadioContainerXComponent;
  let fixture: ComponentFixture<DvRadioContainerXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DvRadioContainerXComponent ]
    })
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
