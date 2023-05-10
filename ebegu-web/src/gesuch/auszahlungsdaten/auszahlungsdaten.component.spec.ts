import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuszahlungsdatenComponent } from './auszahlungsdaten.component';

describe('AuszahlungsdatenComponent', () => {
  let component: AuszahlungsdatenComponent;
  let fixture: ComponentFixture<AuszahlungsdatenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AuszahlungsdatenComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuszahlungsdatenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
