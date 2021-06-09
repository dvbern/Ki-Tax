import {TestBed} from '@angular/core/testing';

import {InternePendenzenRS} from './internePendenzenRS';

describe('InternePendenzenRS', () => {
  let service: InternePendenzenRS;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InternePendenzenRS);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
