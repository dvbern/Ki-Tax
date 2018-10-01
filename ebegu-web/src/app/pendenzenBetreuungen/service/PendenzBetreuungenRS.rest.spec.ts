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

import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSPendenzBetreuung from '../../../models/TSPendenzBetreuung';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {EbeguWebPendenzenBetreuungen} from '../pendenzenBetreuungen.module';
import PendenzBetreuungenRS from './PendenzBetreuungenRS.rest';

describe('pendenzBetreuungenRS', () => {

    let pendenzBetreuungenRS: PendenzBetreuungenRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockPendenzBetreuungen: TSPendenzBetreuung;
    let mockPendenzBetreuungenRest: any;

    beforeEach(angular.mock.module(EbeguWebPendenzenBetreuungen.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        pendenzBetreuungenRS = $injector.get('PendenzBetreuungenRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        mockPendenzBetreuungen = new TSPendenzBetreuung('123.12.12',
            '123',
            '123',
            '123',
            'Kind',
            'Kilian',
            undefined,
            'Platzbestaetigung',
            undefined,
            undefined,
            undefined,
            TSBetreuungsangebotTyp.KITA,
            undefined);
        mockPendenzBetreuungenRest = ebeguRestUtil.pendenzBetreuungenToRestObject({}, mockPendenzBetreuungen);
    });

    describe('Public API', () => {
        it('should include a getPendenzenBetreuungenList() function', () => {
            expect(pendenzBetreuungenRS.getPendenzenBetreuungenList).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('findBetreuung', () => {
            it('should return all pending Betreuungen', () => {
                const arrayResult: Array<any> = [mockPendenzBetreuungenRest];
                $httpBackend.expectGET(pendenzBetreuungenRS.serviceURL).respond(arrayResult);

                let foundPendenzen: Array<TSPendenzBetreuung>;
                pendenzBetreuungenRS.getPendenzenBetreuungenList().then((result) => {
                    foundPendenzen = result;
                });
                $httpBackend.flush();
                expect(foundPendenzen).toBeDefined();
                expect(foundPendenzen.length).toBe(1);
                expect(foundPendenzen[0]).toEqual(mockPendenzBetreuungen);
            });
        });
    });
});
