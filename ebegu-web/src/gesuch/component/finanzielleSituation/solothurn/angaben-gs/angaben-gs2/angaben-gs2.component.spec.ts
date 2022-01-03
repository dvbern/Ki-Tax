import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AngabenGs2Component } from './angaben-gs2.component';

describe('AngabenGsComponent', () => {
  let component: AngabenGs2Component;
  let fixture: ComponentFixture<AngabenGs2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AngabenGs2Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AngabenGs2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
