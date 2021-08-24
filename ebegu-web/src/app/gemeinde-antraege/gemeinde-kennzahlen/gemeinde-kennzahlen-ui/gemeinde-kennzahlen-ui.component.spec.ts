import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GemeindeKennzahlenUiComponent } from './gemeinde-kennzahlen-ui.component';

describe('GemeindeKennzahlenUiComponent', () => {
  let component: GemeindeKennzahlenUiComponent;
  let fixture: ComponentFixture<GemeindeKennzahlenUiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GemeindeKennzahlenUiComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GemeindeKennzahlenUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
