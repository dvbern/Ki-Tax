import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZahlungviewXComponent } from './zahlungview-x.component';

describe('ZahlungviewXComponent', () => {
  let component: ZahlungviewXComponent;
  let fixture: ComponentFixture<ZahlungviewXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ZahlungviewXComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ZahlungviewXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
