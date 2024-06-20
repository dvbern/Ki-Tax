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
import {Observable} from 'rxjs';
import {mergeMap} from 'rxjs/operators';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {LogFactory} from '../../app/core/logging/LogFactory';
import {TSFamiliensituationContainer} from '../../models/TSFamiliensituationContainer';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {WizardStepManager} from './wizardStepManager';

const LOG = LogFactory.createLog('FamiliensituationRS');

@Injectable({
    providedIn: 'root'
})
export class FamiliensituationRS {
    private readonly serviceURL = `${CONSTANTS.REST_API}familiensituation`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        public $http: HttpClient,
        private readonly wizardStepManager: WizardStepManager
    ) {}

    public saveFamiliensituation(
        familiensituation: TSFamiliensituationContainer,
        gesuchId: string
    ): Observable<TSFamiliensituationContainer> {
        let returnedFamiliensituation = {};
        returnedFamiliensituation =
            this.ebeguRestUtil.familiensituationContainerToRestObject(
                returnedFamiliensituation,
                familiensituation
            );
        return this.$http
            .put(
                `${this.serviceURL}/${encodeURIComponent(gesuchId)}`,
                returnedFamiliensituation,
                {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            )
            .pipe(
                mergeMap((response: any) =>
                    this.wizardStepManager
                        .findStepsFromGesuch(gesuchId)
                        .then(() => {
                            LOG.debug(
                                'PARSING Familiensituation REST object ',
                                response
                            );
                            return this.ebeguRestUtil.parseFamiliensituationContainer(
                                new TSFamiliensituationContainer(),
                                response
                            );
                        })
                )
            );
    }
}
