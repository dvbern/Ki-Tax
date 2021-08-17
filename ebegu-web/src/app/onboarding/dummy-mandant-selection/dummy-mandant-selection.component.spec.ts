import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DummyMandantSelectionComponent } from './dummy-mandant-selection.component';

describe('DummyMandantSelectionComponent', () => {
  let component: DummyMandantSelectionComponent;
  let fixture: ComponentFixture<DummyMandantSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DummyMandantSelectionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DummyMandantSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
