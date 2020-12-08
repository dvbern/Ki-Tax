import { TestBed } from '@angular/core/testing';
import {MatDialogModule} from '@angular/material/dialog';

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
