/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {CoreModule} from '../../app/core/core.module';

@Injectable({
    providedIn: CoreModule,
})
export class DailyBatchRS {

    public readonly serviceURL: string;

    public constructor(public $http: HttpClient) {
        this.serviceURL = CONSTANTS.REST_API + 'dailybatch';
    }

    public getServiceName(): string {
        return 'DailyBatchRS';
    }

    public runBatchCleanDownloadFiles(): Observable<string> {
        return this.callServer(this.serviceURL + '/cleanDownloadFiles');
    }

    public runBatchMahnungFristablauf(): Observable<string> {
        return this.callServer(this.serviceURL + '/mahnungFristAblauf');
    }

    public runBatchUpdateGemeindeForBGInstitutionen(): Observable<string> {
        return this.callServer(this.serviceURL + '/updateGemeindeForBGInstitutionen');
    }

    private callServer(url: string): Observable<string> {
        return this.$http.get(url, {responseType: 'text'});
    }
}
