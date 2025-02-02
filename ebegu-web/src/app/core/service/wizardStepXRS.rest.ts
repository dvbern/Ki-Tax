/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import {Observable, ReplaySubject} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSWizardStepXTyp} from '../../../models/enums/TSWizardStepXTyp';
import {TSWizardStepX} from '../../../models/TSWizardStepX';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';
import {LogFactory} from '../logging/LogFactory';

const LOG = LogFactory.createLog('WizardStepXRS');

@Injectable({
    providedIn: 'root'
})
export class WizardStepXRS {
    public serviceURL: string = `${CONSTANTS.REST_API}wizardstepX`;
    public wizardSteps = new ReplaySubject<TSWizardStepX[]>(1);
    private readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor(public http: HttpClient) {}

    public getServiceName(): string {
        return 'WizardStepXRS';
    }

    public updateSteps(wizardStepTyp: TSWizardStepXTyp, id: string): void {
        this.http
            .get(
                `${this.serviceURL}/getAllSteps/${encodeURIComponent(wizardStepTyp)}/${encodeURIComponent(id)}`
            )
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseWizardStepXList(response)
                )
            )
            .subscribe(
                obj => {
                    this.wizardSteps.next(obj);
                },
                err => LOG.error(err)
            );
    }

    public getAllSteps(): Observable<TSWizardStepX[]> {
        return this.wizardSteps.asObservable();
    }

    public initFirstStep(
        wizardStepTyp: TSWizardStepXTyp,
        id: string
    ): Observable<TSWizardStepX> {
        return this.http
            .get(
                `${this.serviceURL}/initFirstStep/${encodeURIComponent(wizardStepTyp)}/${encodeURIComponent(id)}`
            )
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseWizardStepX(response)
                )
            );
    }

    public getNextStep(
        wizardStepTyp: TSWizardStepXTyp,
        wizardStep: string,
        id: string
    ): Observable<TSWizardStepX> {
        return this.http
            .get(
                `${this.serviceURL}/getNextStep/${encodeURIComponent(wizardStepTyp)}/${encodeURIComponent(
                    wizardStep
                )}/${encodeURIComponent(id)}`
            )
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseWizardStepX(response)
                )
            );
    }

    public getPreviousStep(
        wizardStepTyp: TSWizardStepXTyp,
        wizardStep: string,
        id: string
    ): Observable<TSWizardStepX> {
        return this.http
            .get(
                `${this.serviceURL}/getPreviousStep/${encodeURIComponent(wizardStepTyp)}/${encodeURIComponent(
                    wizardStep
                )}/${encodeURIComponent(id)}`
            )
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseWizardStepX(response)
                )
            );
    }
}
