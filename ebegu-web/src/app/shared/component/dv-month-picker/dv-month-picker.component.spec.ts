import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DvMonthPickerComponent } from './dv-month-picker.component';

describe('DvMonthPickerComponent', () => {
  let component: DvMonthPickerComponent;
  let fixture: ComponentFixture<DvMonthPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DvMonthPickerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DvMonthPickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
