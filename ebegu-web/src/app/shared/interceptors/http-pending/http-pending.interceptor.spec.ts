import {TestBed} from '@angular/core/testing';

import {HttpPendingInterceptor} from './http-pending.interceptor';

describe('HttpPendingInterceptor', () => {
    beforeEach(() =>
        TestBed.configureTestingModule({
            providers: [HttpPendingInterceptor]
        })
    );

    it('should be created', () => {
        const interceptor: HttpPendingInterceptor = TestBed.inject(
            HttpPendingInterceptor
        );
        expect(interceptor).toBeTruthy();
    });
});
