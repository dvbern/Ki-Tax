import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DvDemoFeatureWrapperComponent } from './dv-demo-feature-wrapper.component';

describe('DvDemoFetureWrapperComponent', () => {
  let component: DvDemoFeatureWrapperComponent;
  let fixture: ComponentFixture<DvDemoFeatureWrapperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DvDemoFeatureWrapperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DvDemoFeatureWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
