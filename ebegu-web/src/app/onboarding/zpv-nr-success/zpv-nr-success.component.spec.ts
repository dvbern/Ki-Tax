import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZpvNrSuccessComponent } from './zpv-nr-success.component';

describe('ZpvNrSuccessComponent', () => {
  let component: ZpvNrSuccessComponent;
  let fixture: ComponentFixture<ZpvNrSuccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ZpvNrSuccessComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ZpvNrSuccessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
