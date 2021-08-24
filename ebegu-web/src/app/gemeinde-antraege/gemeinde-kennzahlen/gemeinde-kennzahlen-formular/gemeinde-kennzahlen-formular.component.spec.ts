import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GemeindeKennzahlenFormularComponent } from './gemeinde-kennzahlen-formular.component';

describe('GemeindeKennzahlenFormularComponent', () => {
  let component: GemeindeKennzahlenFormularComponent;
  let fixture: ComponentFixture<GemeindeKennzahlenFormularComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GemeindeKennzahlenFormularComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GemeindeKennzahlenFormularComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
