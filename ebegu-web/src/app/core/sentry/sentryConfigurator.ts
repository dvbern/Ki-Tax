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
import * as Raven from 'raven-js';
import {environment} from '../../../environments/environment';
import {BUILDTSTAMP, VERSION} from '../../../environments/version';

const ravenPlugin = require('raven-js/plugins/angular');

export function configureRaven(): void {
    const sentryDSN = environment.sentryDSN;
    if (!sentryDSN) {
        console.log('Sentry is disabled because there is no sentryDSN');
        return;
    }
    Raven
        .config(sentryDSN)
        .addPlugin(ravenPlugin, angular)
        .setRelease(VERSION)
        .setExtraContext({buildtimestamp: BUILDTSTAMP})
        .install();
}

export class RavenErrorHandler extends ErrorHandler {
    public handleError(err: any): void {
        if (environment.sentryDSN) {
            Raven.captureException(err);
        }
        super.handleError(err);
    }
}
