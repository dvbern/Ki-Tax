import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PulldownUserMenuComponent } from './pulldown-user-menu.component';

describe('PulldownUserMenuComponent', () => {
  let component: PulldownUserMenuComponent;
  let fixture: ComponentFixture<PulldownUserMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PulldownUserMenuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PulldownUserMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
