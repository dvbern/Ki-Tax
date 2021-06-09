import {ComponentFixture, TestBed} from '@angular/core/testing';

import {InternePendenzenComponent} from './interne-pendenzen.component';

describe('InternePendenzenComponent', () => {
  let component: InternePendenzenComponent;
  let fixture: ComponentFixture<InternePendenzenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InternePendenzenComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InternePendenzenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
