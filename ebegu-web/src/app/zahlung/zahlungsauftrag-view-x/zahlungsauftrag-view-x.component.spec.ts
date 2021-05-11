import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ZahlungsauftragViewXComponent } from './zahlungsauftrag-view-x.component';

describe('ZahlungsauftragViewXComponent', () => {
  let component: ZahlungsauftragViewXComponent;
  let fixture: ComponentFixture<ZahlungsauftragViewXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ZahlungsauftragViewXComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ZahlungsauftragViewXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
