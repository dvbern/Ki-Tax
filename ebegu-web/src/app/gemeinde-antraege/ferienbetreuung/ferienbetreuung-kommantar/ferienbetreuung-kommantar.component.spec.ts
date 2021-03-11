import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FerienbetreuungKommantarComponent} from './ferienbetreuung-kommantar.component';

describe('FerienbetreuungKommantarComponent', () => {
  let component: FerienbetreuungKommantarComponent;
  let fixture: ComponentFixture<FerienbetreuungKommantarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FerienbetreuungKommantarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FerienbetreuungKommantarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
