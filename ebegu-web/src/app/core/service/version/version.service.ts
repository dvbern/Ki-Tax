/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {Injectable, OnDestroy} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {VERSION} from '../../../../environments/version';
import {LogFactory} from '../../logging/LogFactory';

const LOG = LogFactory.createLog('VersionService');

/**
 * This service handles the kibon backend version and mismatches with the frontend version
 */
@Injectable({
    providedIn: 'root',
})
export class VersionService implements OnDestroy {

    private readonly _$versionMismatch: Subject<string> = new Subject();
    private readonly _$backendVersionChange: Subject<string> = new Subject();
    private _backendVersion: string;

    private eventCaptured: boolean;

    private readonly _$unsubcribe: Subject<boolean> = new Subject();

    public constructor() {
    }

    private static hasVersionCompatibility(frontendVersion: string, backendVersion: string): boolean {
        // Wir erwarten, dass die Versionsnummern im Frontend und Backend immer synchronisiert werden
        return frontendVersion === backendVersion;
    }

    public get $versionMismatch(): Observable<string> {
        return this._$versionMismatch.asObservable()
                .pipe(
                        takeUntil(this._$unsubcribe),
                );
    }

    public get $backendVersionChange(): Observable<string> {
        return this._$backendVersionChange;
    }

    public ngOnDestroy(): void {
        this._$unsubcribe.next(true);
    }

    public versionMismatchHandled(): void {
        this.eventCaptured = true;
    }

    public updateBackendVersion(newVersion: string): void {

        if (this.eventCaptured && newVersion === this._backendVersion) {
            // if the event hasn't been captured yet we wait until it gets captured
            return;
        }

        if (this._backendVersion !== newVersion) {
            this._$backendVersionChange.next(newVersion);
        }

        this._backendVersion = newVersion;

        if (VersionService.hasVersionCompatibility(VERSION, this._backendVersion)) {
            // could throw match event here but currently there is no action we want to perform when it matches
            return;
        }
        LOG.warn('Versions of Frontend and Backend do not match');
        // before we send the event we say that the event hasn't been captured.
        // After caturing the event this should be set to true
        this.eventCaptured = false;
        this.versionMismatch(this._backendVersion);

    }

    private versionMismatch(backendVersion: string): void {
        this._$versionMismatch.next(backendVersion);
    }

}
