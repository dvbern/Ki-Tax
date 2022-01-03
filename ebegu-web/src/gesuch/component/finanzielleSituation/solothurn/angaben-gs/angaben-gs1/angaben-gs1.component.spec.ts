import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AngabenGs1Component } from './angaben-gs1.component';

describe('AngabenGsComponent', () => {
  let component: AngabenGs1Component;
  let fixture: ComponentFixture<AngabenGs1Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AngabenGs1Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AngabenGs1Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
