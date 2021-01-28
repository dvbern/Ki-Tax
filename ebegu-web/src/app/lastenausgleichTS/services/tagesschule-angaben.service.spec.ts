import {HttpClientModule} from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { TagesschuleAngabenRS } from './tagesschule-angaben.service.rest';

describe('TagesschuleAngabenService', () => {
  let service: TagesschuleAngabenRS;

  beforeEach(() => {
    TestBed.configureTestingModule({
        imports: [HttpClientModule]
    });
    service = TestBed.inject(TagesschuleAngabenRS);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
