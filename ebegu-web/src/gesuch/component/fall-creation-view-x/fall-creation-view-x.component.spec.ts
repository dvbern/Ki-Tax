import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FallCreationViewXComponent } from './fall-creation-view-x.component';

describe('FallCreationViewXComponent', () => {
  let component: FallCreationViewXComponent;
  let fixture: ComponentFixture<FallCreationViewXComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FallCreationViewXComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FallCreationViewXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
