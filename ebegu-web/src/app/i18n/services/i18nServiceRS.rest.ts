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

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {TSLanguage} from '../../../models/enums/TSLanguage';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root',
})
export class I18nServiceRSRest {

    public serviceURL: string;

    public constructor(
        private readonly http: HttpClient,
        private readonly translate: TranslateService,
    ) {
        this.serviceURL =  `${CONSTANTS.REST_API}i18n`;
    }

    /**
     * Calling this method we change the language that the server is using
     */
    public changeServerLanguage(selectedLanguage: TSLanguage): Observable<any> {
        return this.http.put<TSLanguage>(this.serviceURL, selectedLanguage);
    }

    /**
     * This method will change the language that the plugin of angular5 uses.
     * This method must exist in order to be able to change the language from within an angularjs component
     */
    public changeClientLanguage(selectedLanguage: TSLanguage): void {
        this.translate.use(selectedLanguage);
    }
}
