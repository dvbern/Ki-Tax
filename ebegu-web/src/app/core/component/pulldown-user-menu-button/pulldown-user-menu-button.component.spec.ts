import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PulldownUserMenuButtonComponent } from './pulldown-user-menu-button.component';

describe('PulldownUserMenuButtonComponent', () => {
  let component: PulldownUserMenuButtonComponent;
  let fixture: ComponentFixture<PulldownUserMenuButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PulldownUserMenuButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PulldownUserMenuButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
