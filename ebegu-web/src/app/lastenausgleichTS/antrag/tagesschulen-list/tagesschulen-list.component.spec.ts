import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TagesschulenListComponent } from './tagesschulen-list.component';

describe('TagesschulenListComponent', () => {
  let component: TagesschulenListComponent;
  let fixture: ComponentFixture<TagesschulenListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TagesschulenListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TagesschulenListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
