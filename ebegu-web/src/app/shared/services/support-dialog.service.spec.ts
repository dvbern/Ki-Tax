import { TestBed } from '@angular/core/testing';

import { SupportDialogService } from './support-dialog.service';

describe('SupportDialogService', () => {
  let service: SupportDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SupportDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
