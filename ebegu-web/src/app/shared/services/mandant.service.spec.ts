import { TestBed } from '@angular/core/testing';

import { MandantService } from './mandant.service';

describe('MandantService', () => {
  let service: MandantService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MandantService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
