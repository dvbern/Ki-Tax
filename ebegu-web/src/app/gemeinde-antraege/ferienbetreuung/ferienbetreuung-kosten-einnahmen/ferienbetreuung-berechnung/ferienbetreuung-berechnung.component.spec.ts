import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FerienbetreuungBerechnungComponent} from './ferienbetreuung-berechnung.component';

describe('FerienbetreuungBerechnungComponent', () => {
  let component: FerienbetreuungBerechnungComponent;
  let fixture: ComponentFixture<FerienbetreuungBerechnungComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FerienbetreuungBerechnungComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FerienbetreuungBerechnungComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
