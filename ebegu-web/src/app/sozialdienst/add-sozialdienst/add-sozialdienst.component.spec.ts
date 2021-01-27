import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddSozialdienstComponent } from './add-sozialdienst.component';

describe('AddSozialdienstComponent', () => {
  let component: AddSozialdienstComponent;
  let fixture: ComponentFixture<AddSozialdienstComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddSozialdienstComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddSozialdienstComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
