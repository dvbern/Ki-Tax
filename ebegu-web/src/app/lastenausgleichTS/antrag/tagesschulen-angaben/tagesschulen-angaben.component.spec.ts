import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TagesschulenAngabenComponent} from './tagesschulen-angaben.component';

describe('TagesschulenAngabenComponent', () => {
  let component: TagesschulenAngabenComponent;
  let fixture: ComponentFixture<TagesschulenAngabenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TagesschulenAngabenComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TagesschulenAngabenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
