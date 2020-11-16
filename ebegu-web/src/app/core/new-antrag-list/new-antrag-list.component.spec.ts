import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewAntragListComponent } from './new-antrag-list.component';

describe('NewAntragListComponent', () => {
  let component: NewAntragListComponent;
  let fixture: ComponentFixture<NewAntragListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewAntragListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewAntragListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
