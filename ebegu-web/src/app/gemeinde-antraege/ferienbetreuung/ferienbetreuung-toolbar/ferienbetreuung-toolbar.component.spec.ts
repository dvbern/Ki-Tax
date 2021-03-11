import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FerienbetreuungToolbarComponent} from './ferienbetreuung-toolbar.component';

describe('FerienbetreuungToolbarComponent', () => {
  let component: FerienbetreuungToolbarComponent;
  let fixture: ComponentFixture<FerienbetreuungToolbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FerienbetreuungToolbarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FerienbetreuungToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
