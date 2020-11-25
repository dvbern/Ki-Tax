import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LastenausgleichTsToolbarComponent} from './lastenausgleich-ts-toolbar.component';

describe('LastenausgleichTsToolbarComponent', () => {
  let component: LastenausgleichTsToolbarComponent;
  let fixture: ComponentFixture<LastenausgleichTsToolbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTsToolbarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTsToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
