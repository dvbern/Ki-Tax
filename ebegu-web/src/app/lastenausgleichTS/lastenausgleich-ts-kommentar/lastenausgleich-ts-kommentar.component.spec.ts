import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LastenausgleichTsKommentarComponent} from './lastenausgleich-ts-kommentar.component';

describe('LastenausgleichTsKommentarComponent', () => {
  let component: LastenausgleichTsKommentarComponent;
  let fixture: ComponentFixture<LastenausgleichTsKommentarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LastenausgleichTsKommentarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastenausgleichTsKommentarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
