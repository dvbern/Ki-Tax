import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SteuerveranlagungErhaltenComponent } from './steuerveranlagung-erhalten.component';

describe('SteuerveranlagungErhaltenComponent', () => {
  let component: SteuerveranlagungErhaltenComponent;
  let fixture: ComponentFixture<SteuerveranlagungErhaltenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SteuerveranlagungErhaltenComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SteuerveranlagungErhaltenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
