import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {WindowRef} from '../../../core/service/windowRef.service';
import {SharedModule} from '../../shared.module';

import { DvMonthPickerComponent } from './dv-month-picker.component';

describe('DvMonthPickerComponent', () => {
  let component: DvMonthPickerComponent;
  let fixture: ComponentFixture<DvMonthPickerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
        imports: [
            SharedModule,
        ],
        providers: [
            WindowRef,
            {provide: NgForm, useValue: new NgForm([], [])},
        ]
    })
        .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
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
