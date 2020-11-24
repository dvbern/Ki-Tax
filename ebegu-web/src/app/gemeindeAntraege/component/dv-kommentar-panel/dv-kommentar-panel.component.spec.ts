import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DvKommentarPanelComponent } from './dv-kommentar-panel.component';

describe('DvKommentarPanelComponent', () => {
  let component: DvKommentarPanelComponent;
  let fixture: ComponentFixture<DvKommentarPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DvKommentarPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DvKommentarPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
