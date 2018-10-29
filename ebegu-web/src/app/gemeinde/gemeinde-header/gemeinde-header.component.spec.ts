import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GemeindeHeaderComponent } from './gemeinde-header.component';

describe('GemeindeHeaderComponent', () => {
  let component: GemeindeHeaderComponent;
  let fixture: ComponentFixture<GemeindeHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GemeindeHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GemeindeHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
