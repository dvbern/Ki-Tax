/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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
import {map} from 'rxjs/operators';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {TSFerienname} from '../../models/enums/TSFerienname';
import {TSFerieninselStammdaten} from '../../models/TSFerieninselStammdaten';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root',
})
export class FerieninselStammdatenRS {

    public readonly serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor(
        public http: HttpClient
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}ferieninselStammdaten`;
    }

    public saveFerieninselStammdaten(stammdaten: TSFerieninselStammdaten): Promise<TSFerieninselStammdaten> {
        let stammdatenObj = {};
        stammdatenObj = this.ebeguRestUtil.ferieninselStammdatenToRestObject(stammdatenObj, stammdaten);

        return this.http.put(this.serviceURL, stammdatenObj)
            .pipe(map((response: any) => this.ebeguRestUtil.parseFerieninselStammdaten(new TSFerieninselStammdaten(), response)))
            .toPromise();
    }

    public findFerieninselStammdaten(fachstelleID: string): Promise<TSFerieninselStammdaten> {
        return this.http.get(`${this.serviceURL}/id/${encodeURIComponent(fachstelleID)}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseFerieninselStammdaten(new TSFerieninselStammdaten(), response)))
            .toPromise();
    }

    public findFerieninselStammdatenByGesuchsperiode(gesuchsperiodeId: string): Promise<TSFerieninselStammdaten[]> {
        return this.http.get(`${this.serviceURL}/gesuchsperiode/${encodeURIComponent(gesuchsperiodeId)}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseFerieninselStammdatenList(response)))
            .toPromise();
    }

    public findFerieninselStammdatenByGesuchsperiodeAndFerien(
        gesuchsperiodeId: string,
        gemeindeId: string,
        ferienname: TSFerienname
    ): Promise<TSFerieninselStammdaten> {
        const url = `${encodeURIComponent(gesuchsperiodeId)}/${encodeURIComponent(gemeindeId)}/${ferienname}`;
        return this.http.get(`${this.serviceURL}/gesuchsperiode/${url}`)
            .pipe(map((response: any) => this.ebeguRestUtil.parseFerieninselStammdaten(new TSFerieninselStammdaten(), response)))
            .toPromise();
    }

    public getServiceName(): string {
        return 'FerieninselStammdatenRS';
    }
}
