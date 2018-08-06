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

import {EbeguWebCore} from '../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import GesuchRS from './gesuchRS.rest';

describe('gesuch', () => {

    let gesuchRS: GesuchRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let REST_API: string;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchRS = $injector.get('GesuchRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        REST_API = $injector.get('REST_API');
    }));

    describe('Public API', () => {

        it('should include a createGesuch() function', () => {
            expect(gesuchRS.createGesuch).toBeDefined();
        });

        it('should include a findGesuch() function', () => {
            expect(gesuchRS.findGesuch).toBeDefined();
        });

        it('should include a updateGesuch() function', () => {
            expect(gesuchRS.updateGesuch).toBeDefined();
        });

    });

    describe('API Usage', () => {

    });
});
