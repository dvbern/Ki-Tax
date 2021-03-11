import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FerienbetreuungUiViewComponent} from './ferienbetreuung-ui-view.component';

describe('FerienbetreuungUiViewComponent', () => {
  let component: FerienbetreuungUiViewComponent;
  let fixture: ComponentFixture<FerienbetreuungUiViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FerienbetreuungUiViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FerienbetreuungUiViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
