import { TestBed } from '@angular/core/testing';
import {MatLegacyDialogModule as MatDialogModule} from '@angular/material/legacy-dialog';

import { SupportDialogService } from './support-dialog.service';

describe('SupportDialogService', () => {
  let service: SupportDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
        imports: [MatDialogModule]
    });
    service = TestBed.inject(SupportDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
