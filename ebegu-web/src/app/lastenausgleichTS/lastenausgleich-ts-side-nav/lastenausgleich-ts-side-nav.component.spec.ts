import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LastenausgleichTsSideNavComponent} from './lastenausgleich-ts-side-nav.component';

describe('LastenausgleichTsSideNavComponent', () => {
  let component: LastenausgleichTsSideNavComponent;
  let fixture: ComponentFixture<LastenausgleichTsSideNavComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTsSideNavComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTsSideNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
