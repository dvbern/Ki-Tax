import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminViewXComponent } from './admin-view-x.component';

describe('AdminViewXComponent', () => {
  let component: AdminViewXComponent;
  let fixture: ComponentFixture<AdminViewXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminViewXComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminViewXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
