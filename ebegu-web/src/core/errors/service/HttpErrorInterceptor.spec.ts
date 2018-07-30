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

import TSExceptionReport from '../../../models/TSExceptionReport';
import TestDataUtil from '../../../utils/TestDataUtil';
import HttpErrorInterceptor from './HttpErrorInterceptor';

describe('httpErrorInterceptor', () => {

    let httpErrorInterceptor: HttpErrorInterceptor, $rootScope: angular.IRootScopeService, $q: angular.IQService;

    beforeEach(angular.mock.module('dvbAngular.errors'));

    beforeEach(angular.mock.inject($injector => {
        httpErrorInterceptor = $injector.get('HttpErrorInterceptor');
        $rootScope = $injector.get('$rootScope');
        $q = $injector.get('$q');
    }));

    describe('Public API', () => {
        it('should include a responseError() function', () => {
            expect(httpErrorInterceptor.responseError).toBeDefined();
        });
    });

    describe('API usage', () => {
        let deferred: angular.IDeferred<any>, successHandler: any, errorHandler: any;
        beforeEach(() => {
            deferred = $q.defer();
            successHandler = jasmine.createSpy('successHandler');
            errorHandler = jasmine.createSpy('errorHanlder');
            deferred.promise.then(successHandler, errorHandler);
        });

        it('should reject the response with a validation report', () => {
            const validationResponse: any = TestDataUtil.createValidationReport();
            httpErrorInterceptor.responseError(validationResponse).then(() => {
                deferred.resolve();
            }, errors => {
                deferred.reject(errors);
            });

            const errors: Array<TSExceptionReport> = [(TSExceptionReport.createFromViolation('PARAMETER',
                'Die Länge des Feldes muss zwischen 36 und 36 sein', 'markAsRead.arg1', '8a146418-ab12-456f-9b17-aad6990f51'))];
            $rootScope.$digest();
            expect(errorHandler).toHaveBeenCalledWith(errors);
        });

        it('should reject the response containing an exceptionReport', () => {
            const exceptionReportResponse: any = TestDataUtil.createExceptionReport();
            httpErrorInterceptor.responseError(exceptionReportResponse).then(() => {
                deferred.resolve();
            }, error => {
                deferred.reject(error);
            });

            const errors: Array<TSExceptionReport> = [(TSExceptionReport.createFromExceptionReport(exceptionReportResponse.data))];
            $rootScope.$digest();
            expect(errorHandler).toHaveBeenCalledWith(errors);
        });
    });
});
