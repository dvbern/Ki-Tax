import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {GemeindeAngabenComponent} from './gemeinde-angaben.component';

describe('GemeindeAngabenComponent', () => {
  let component: GemeindeAngabenComponent;
  let fixture: ComponentFixture<GemeindeAngabenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GemeindeAngabenComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GemeindeAngabenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
