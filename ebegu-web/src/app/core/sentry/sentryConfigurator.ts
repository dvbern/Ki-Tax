/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ErrorHandler} from '@angular/core';
import * as Sentry from '@sentry/browser';
import {environment} from '../../../environments/environment';
import {VERSION} from '../../../environments/version';
import {Angular as AngularIntegration} from '@sentry/integrations';

export function configureSentry(): void {
    const sentryDSN = environment.sentryDSN;
    if (!sentryDSN) {
        console.log('Sentry is disabled because there is no sentryDSN');
        return;
    }
    Sentry.init({
        release: VERSION,
        dsn: sentryDSN,
        integrations: [new AngularIntegration()]
    });
}

export class SentryErrorHandler extends ErrorHandler {
    public handleError(err: any): void {
        if (environment.sentryDSN) {
            Sentry.captureException(err);
        }
        super.handleError(err);
    }
}
