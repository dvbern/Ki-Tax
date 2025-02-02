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

import {IHttpBackendService} from 'angular';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragStatusHistory} from '../../../models/TSAntragStatusHistory';
import {TSGesuch} from '../../../models/TSGesuch';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {AntragStatusHistoryRS} from './antragStatusHistoryRS.rest';

describe('antragStatusHistoryRS', () => {
    let antragStatusHistoryRS: AntragStatusHistoryRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            antragStatusHistoryRS = $injector.get('AntragStatusHistoryRS');
            $httpBackend = $injector.get('$httpBackend');
            ebeguRestUtil = $injector.get('EbeguRestUtil');

            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        })
    );

    describe('Public API', () => {
        it('check URI', () => {
            expect(antragStatusHistoryRS.serviceURL).toContain(
                'antragStatusHistory'
            );
        });
    });

    describe('loadLastStatusChange', () => {
        it('should return the last status change for the given gesuch', () => {
            const gesuch = new TSGesuch();
            gesuch.id = '123456';
            const antragStatusHistory = new TSAntragStatusHistory(
                gesuch.id,
                undefined,
                DateUtil.today(),
                undefined,
                TSAntragStatus.VERFUEGEN
            );
            TestDataUtil.setAbstractMutableFieldsUndefined(antragStatusHistory);
            const restAntStatusHistory =
                ebeguRestUtil.antragStatusHistoryToRestObject(
                    {},
                    antragStatusHistory
                );
            $httpBackend
                .expectGET(
                    `${antragStatusHistoryRS.serviceURL}/${encodeURIComponent(gesuch.id)}`
                )
                .respond(restAntStatusHistory);

            let lastStatusChange: TSAntragStatusHistory;
            antragStatusHistoryRS
                .loadLastStatusChange(gesuch)
                .then(response => {
                    lastStatusChange = response;
                });
            $httpBackend.flush();

            expect(lastStatusChange).toBeDefined();
            expect(
                lastStatusChange.timestampVon.isSame(
                    antragStatusHistory.timestampVon
                )
            ).toBe(true);
            lastStatusChange.timestampVon = antragStatusHistory.timestampVon;
            expect(lastStatusChange).toEqual(antragStatusHistory);
        });
        it('should return undefined if the gesuch is undefined', () => {
            antragStatusHistoryRS.loadLastStatusChange(undefined);
            antragStatusHistoryRS.lastChange$.subscribe(
                value => expect(value).toBeUndefined(),
                fail
            );
        });
        it('should return undefined if the gesuch id is undefined', () => {
            const gesuch = new TSGesuch();
            gesuch.id = undefined;
            antragStatusHistoryRS.loadLastStatusChange(gesuch);
            antragStatusHistoryRS.lastChange$.subscribe(
                value => expect(value).toBeUndefined(),
                fail
            );
        });
    });
});
