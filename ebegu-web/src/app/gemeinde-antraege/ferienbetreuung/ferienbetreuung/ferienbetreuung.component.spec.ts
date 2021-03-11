import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FerienbetreuungComponent} from './ferienbetreuung.component';

describe('FerienbetreuungComponent', () => {
  let component: FerienbetreuungComponent;
  let fixture: ComponentFixture<FerienbetreuungComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FerienbetreuungComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FerienbetreuungComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
