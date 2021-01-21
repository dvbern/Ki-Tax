import {HttpClientModule} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';

import {LastenausgleichTSService} from './lastenausgleich-ts.service';

describe('GemeindeAngaben', () => {
  let service: LastenausgleichTSService;

  beforeEach(() => {
    TestBed.configureTestingModule({
        imports: [HttpClientModule]
    });
    service = TestBed.inject(LastenausgleichTSService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
