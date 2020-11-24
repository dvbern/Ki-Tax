import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LastenausgleichTagesschuleComponent } from './lastenausgleich-tagesschule.component';

describe('LastenausgleichTagesschuleComponent', () => {
  let component: LastenausgleichTagesschuleComponent;
  let fixture: ComponentFixture<LastenausgleichTagesschuleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTagesschuleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTagesschuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
