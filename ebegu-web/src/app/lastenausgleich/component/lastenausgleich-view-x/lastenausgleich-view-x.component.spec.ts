import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LastenausgleichViewXComponent } from './lastenausgleich-view-x.component';

describe('LastenausgleichViewXComponent', () => {
  let component: LastenausgleichViewXComponent;
  let fixture: ComponentFixture<LastenausgleichViewXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LastenausgleichViewXComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichViewXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
