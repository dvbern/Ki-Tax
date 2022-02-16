import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EinkommensverschlechterungLuzernResultateViewComponent } from './einkommensverschlechterung-luzern-resultate-view.component';

describe('EinkommensverschlechterungLuzernResultateViewComponent', () => {
  let component: EinkommensverschlechterungLuzernResultateViewComponent;
  let fixture: ComponentFixture<EinkommensverschlechterungLuzernResultateViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EinkommensverschlechterungLuzernResultateViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EinkommensverschlechterungLuzernResultateViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
