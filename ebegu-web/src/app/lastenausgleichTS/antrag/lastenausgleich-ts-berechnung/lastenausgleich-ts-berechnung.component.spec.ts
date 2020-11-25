import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LastenausgleichTsBerechnungComponent} from './lastenausgleich-ts-berechnung.component';

describe('LastenausgleichTsBerechnungComponent', () => {
  let component: LastenausgleichTsBerechnungComponent;
  let fixture: ComponentFixture<LastenausgleichTsBerechnungComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTsBerechnungComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTsBerechnungComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
