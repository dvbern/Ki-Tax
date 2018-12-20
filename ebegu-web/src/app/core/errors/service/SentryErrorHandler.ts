import {ErrorHandler, Injectable} from '@angular/core';
import * as Raven from 'raven-js';
import {environment} from '../../../../environments/environment';

@Injectable({
    providedIn: 'root',
})
export class SentryErrorHandler extends ErrorHandler {
    constructor() {
        super();
    }

    handleError(err: any): void {
        if (environment.sentryDSN) {
            Raven.captureException(err);
        }
        super.handleError(err);
    }
}
