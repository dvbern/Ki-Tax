import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitZpvNrComponent } from './init-zpv-nr.component';

describe('InitZpvNrComponent', () => {
  let component: InitZpvNrComponent;
  let fixture: ComponentFixture<InitZpvNrComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InitZpvNrComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InitZpvNrComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
