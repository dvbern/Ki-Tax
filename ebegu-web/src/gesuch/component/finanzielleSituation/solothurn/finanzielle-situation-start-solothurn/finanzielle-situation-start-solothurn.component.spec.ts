import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinanzielleSituationStartSolothurnComponent } from './finanzielle-situation-start-solothurn.component';

describe('FinanzielleSituationStartSolothurnComponent', () => {
  let component: FinanzielleSituationStartSolothurnComponent;
  let fixture: ComponentFixture<FinanzielleSituationStartSolothurnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FinanzielleSituationStartSolothurnComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FinanzielleSituationStartSolothurnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
