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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {LogFactory} from '../../../core/logging/LogFactory';

const LOG = LogFactory.createLog('FerienbetreuungService');

@Injectable({
    providedIn: 'root',
})
export class FerienbetreuungService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}ferienbetreuung`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(private readonly http: HttpClient) {
    }
}
