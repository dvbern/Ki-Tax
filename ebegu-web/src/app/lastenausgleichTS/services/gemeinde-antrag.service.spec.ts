import {HttpClientModule} from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { GemeindeAntragService } from './gemeinde-antrag.service';

describe('GemeindeAntragService', () => {
  let service: GemeindeAntragService;

  beforeEach(() => {
    TestBed.configureTestingModule({
        imports: [HttpClientModule]
    });
    service = TestBed.inject(GemeindeAntragService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
