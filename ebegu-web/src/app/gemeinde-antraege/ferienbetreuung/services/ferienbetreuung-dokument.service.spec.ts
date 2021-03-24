import {TestBed} from '@angular/core/testing';

import {FerienbetreuungDokumentService} from './ferienbetreuung-dokument.service';

describe('FerienbetreuungDokumentService', () => {
  let service: FerienbetreuungDokumentService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FerienbetreuungDokumentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
