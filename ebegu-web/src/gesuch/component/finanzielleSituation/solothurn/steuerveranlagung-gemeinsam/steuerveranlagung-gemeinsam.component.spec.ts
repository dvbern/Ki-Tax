import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SteuerveranlagungGemeinsamComponent } from './steuerveranlagung-gemeinsam.component';

describe('SteuerveranlagungGemeinsamComponent', () => {
  let component: SteuerveranlagungGemeinsamComponent;
  let fixture: ComponentFixture<SteuerveranlagungGemeinsamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SteuerveranlagungGemeinsamComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SteuerveranlagungGemeinsamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
