import {ComponentFixture, TestBed} from '@angular/core/testing';

import {InternePendenzenTableComponent} from './interne-pendenzen-table.component';

describe('InternePendenzenTableComponent', () => {
  let component: InternePendenzenTableComponent;
  let fixture: ComponentFixture<InternePendenzenTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InternePendenzenTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InternePendenzenTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
