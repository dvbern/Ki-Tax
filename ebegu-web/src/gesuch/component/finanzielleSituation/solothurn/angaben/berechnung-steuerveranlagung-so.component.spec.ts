import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BerechnungSteuerveranlagungSoComponent } from './berechnung-steuerveranlagung-so.component';

describe('AngabenComponent', () => {
  let component: BerechnungSteuerveranlagungSoComponent;
  let fixture: ComponentFixture<BerechnungSteuerveranlagungSoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BerechnungSteuerveranlagungSoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BerechnungSteuerveranlagungSoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
