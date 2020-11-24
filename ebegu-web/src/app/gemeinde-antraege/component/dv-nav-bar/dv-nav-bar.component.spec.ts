import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DvNavBarComponent } from './dv-nav-bar.component';

describe('DvNavBarComponent', () => {
  let component: DvNavBarComponent;
  let fixture: ComponentFixture<DvNavBarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DvNavBarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DvNavBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
