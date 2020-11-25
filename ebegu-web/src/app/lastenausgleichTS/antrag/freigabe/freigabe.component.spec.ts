import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {FreigabeComponent} from './freigabe.component';

describe('FreigabeComponent', () => {
  let component: FreigabeComponent;
  let fixture: ComponentFixture<FreigabeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FreigabeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FreigabeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
