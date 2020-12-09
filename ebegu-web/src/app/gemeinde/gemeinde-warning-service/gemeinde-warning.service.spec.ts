import {TestBed} from '@angular/core/testing';

import {GemeindeWarningService} from './gemeinde-warning.service';

describe('GemeindeWarningService', () => {
  let service: GemeindeWarningService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GemeindeWarningService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
