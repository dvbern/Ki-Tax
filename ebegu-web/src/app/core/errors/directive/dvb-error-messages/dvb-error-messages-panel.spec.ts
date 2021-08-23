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

import {TSErrorAction} from '../../../../../models/enums/TSErrorAction';
import {TSExceptionReport} from '../../../../../models/TSExceptionReport';
import {TestDataUtil} from '../../../../../utils/TestDataUtil.spec';
import {DvErrorMessagesPanelComponent} from './dvb-error-messages-panel';

describe('dvbErrorMessages', () => {

    let controller: DvErrorMessagesPanelComponent;
    let exceptionReport: any;

    beforeEach(angular.mock.module('dvbAngular.errors'));

    beforeEach(angular.mock.inject($injector => {
        controller = new DvErrorMessagesPanelComponent($injector.get('$rootScope'), $injector.get('ErrorService'),
            undefined, undefined, undefined, undefined);
        // mock 'show' method, because in unit tests, jqLite is used instead of jQuery, but jqLite does not support
        // selectors
        spyOn(controller, 'show').and.returnValue();
        exceptionReport = TestDataUtil.createExceptionReport();
    }));

    describe('displayMessages', () => {
        it('should not add any action', () => {
            exceptionReport.errorCodeEnum = 'OTHER_TYPE';
            const error = TSExceptionReport.createFromExceptionReport(exceptionReport);
            const errors = [error];
            controller.displayMessages(undefined, errors);

            expect(error.action).toBeUndefined();
        });
        it('should add an action to ERROR_EXISTING_ONLINE_MUTATION', () => {
            exceptionReport.errorCodeEnum = 'ERROR_EXISTING_ONLINE_MUTATION';
            const error = TSExceptionReport.createFromExceptionReport(exceptionReport);
            const errors = [error];
            controller.displayMessages(undefined, errors);

            expect(error.action).toBe(TSErrorAction.REMOVE_ONLINE_MUTATION);
        });
    });
});
