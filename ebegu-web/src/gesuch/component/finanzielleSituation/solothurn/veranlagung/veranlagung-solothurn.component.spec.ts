import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VeranlagungSolothurnComponent } from './veranlagung-solothurn.component';

describe('VeranlagungComponent', () => {
  let component: VeranlagungSolothurnComponent;
  let fixture: ComponentFixture<VeranlagungSolothurnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VeranlagungSolothurnComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VeranlagungSolothurnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
