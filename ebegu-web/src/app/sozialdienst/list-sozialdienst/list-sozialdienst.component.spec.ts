import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListSozialdienstComponent } from './list-sozialdienst.component';

describe('ListSozialdienstComponent', () => {
  let component: ListSozialdienstComponent;
  let fixture: ComponentFixture<ListSozialdienstComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListSozialdienstComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListSozialdienstComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
