import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FinanzielleSituationAufteilungComponent} from './finanzielle-situation-aufteilung.component';

describe('FinanzielleSituationAufteilungComponent', () => {
  let component: FinanzielleSituationAufteilungComponent;
  let fixture: ComponentFixture<FinanzielleSituationAufteilungComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FinanzielleSituationAufteilungComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FinanzielleSituationAufteilungComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
