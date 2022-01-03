import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BruttolohnComponent } from './bruttolohn.component';

describe('BruttolohnComponent', () => {
  let component: BruttolohnComponent;
  let fixture: ComponentFixture<BruttolohnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BruttolohnComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BruttolohnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
