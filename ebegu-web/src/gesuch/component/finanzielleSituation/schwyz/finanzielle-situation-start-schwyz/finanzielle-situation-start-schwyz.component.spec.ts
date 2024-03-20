import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinanzielleSituationStartSchwyzComponent } from './finanzielle-situation-start-schwyz.component';

describe('FinanzielleSituationStartSchwyzComponent', () => {
  let component: FinanzielleSituationStartSchwyzComponent;
  let fixture: ComponentFixture<FinanzielleSituationStartSchwyzComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FinanzielleSituationStartSchwyzComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FinanzielleSituationStartSchwyzComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
