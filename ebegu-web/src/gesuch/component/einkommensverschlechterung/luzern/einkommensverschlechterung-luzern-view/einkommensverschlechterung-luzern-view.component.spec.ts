import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EinkommensverschlechterungLuzernViewComponent } from './einkommensverschlechterung-luzern-view.component';

describe('EinkommensverschlechterungLuzernViewComponent', () => {
  let component: EinkommensverschlechterungLuzernViewComponent;
  let fixture: ComponentFixture<EinkommensverschlechterungLuzernViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EinkommensverschlechterungLuzernViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EinkommensverschlechterungLuzernViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
