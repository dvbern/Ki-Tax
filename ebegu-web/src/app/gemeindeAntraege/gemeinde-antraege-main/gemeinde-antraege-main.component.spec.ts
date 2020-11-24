import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GemeindeAntraegeMainComponent } from './gemeinde-antraege-main.component';

describe('GemeindeAntraegeMainComponent', () => {
  let component: GemeindeAntraegeMainComponent;
  let fixture: ComponentFixture<GemeindeAntraegeMainComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GemeindeAntraegeMainComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GemeindeAntraegeMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
