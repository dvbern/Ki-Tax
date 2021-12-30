import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AngabenComponent } from './angaben.component';

describe('AngabenComponent', () => {
  let component: AngabenComponent;
  let fixture: ComponentFixture<AngabenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AngabenComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AngabenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
