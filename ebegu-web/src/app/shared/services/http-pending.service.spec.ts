import {TestBed} from '@angular/core/testing';

import {HttpPendingService} from './http-pending.service';

describe('HttpPendingService', () => {
    let service: HttpPendingService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(HttpPendingService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
