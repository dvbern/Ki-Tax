import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DvSimpleTableComponent } from './dv-simple-table.component';

describe('DvSimpleTableComponent', () => {
  let component: DvSimpleTableComponent;
  let fixture: ComponentFixture<DvSimpleTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DvSimpleTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DvSimpleTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
