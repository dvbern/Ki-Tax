import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LastenausgleichTSComponent} from './lastenausgleich-ts.component';

describe('LastenausgleichTSComponent', () => {
  let component: LastenausgleichTSComponent;
  let fixture: ComponentFixture<LastenausgleichTSComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTSComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTSComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
