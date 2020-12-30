import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GemeindeAntraegeComponent } from './gemeinde-antraege.component';

describe('GemeindeAntraegeComponent', () => {
  let component: GemeindeAntraegeComponent;
  let fixture: ComponentFixture<GemeindeAntraegeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GemeindeAntraegeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GemeindeAntraegeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
