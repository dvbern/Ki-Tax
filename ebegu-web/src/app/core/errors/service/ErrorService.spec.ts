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

import {IRootScopeService} from 'angular';
import {TSMessageEvent} from '../../../../models/enums/TSErrorEvent';
import {TSErrorLevel} from '../../../../models/enums/TSErrorLevel';
import {TSErrorType} from '../../../../models/enums/TSErrorType';
import TSExceptionReport from '../../../../models/TSExceptionReport';
import ErrorService from './ErrorService';

describe('errorService', () => {

    let errorService: ErrorService;
    let $rootScope: IRootScopeService;

    beforeEach(angular.mock.module('dvbAngular.errors'));

    beforeEach(angular.mock.inject($injector => {
        $rootScope = $injector.get('$rootScope');
        errorService = $injector.get('ErrorService');
    }));

    beforeEach(inject(() => {
        spyOn($rootScope, '$broadcast').and.callThrough();
    }));

    describe('Public API', () => {
        it('should include a getErrors() function', () => {
            expect(errorService.getErrors).toBeDefined();
        });
        it('should include a addValidationError() function', () => {
            expect(errorService.addValidationError).toBeDefined();
        });
        it('should include a clearAll() function', () => {
            expect(errorService.clearAll).toBeDefined();
        });
        it('should include a clearError() function', () => {
            expect(errorService.clearError).toBeDefined();
        });
        it('should include a handleError() function', () => {
            expect(errorService.handleError).toBeDefined();
        });
        it('should include a handleValidationError() function', () => {
            expect(errorService.handleValidationError).toBeDefined();
        });
    });

    describe('Public API usage', () => {
        describe('getErrors()', () => {
            it('should return an array', () => {
                expect(errorService.getErrors()).toEqual([]);
            });
            it('the internal error array should be immutable', () => {
                const errors = errorService.getErrors();
                const length = errors.length;

                errors.push(TSExceptionReport.createClientSideError(TSErrorLevel.INFO, 'custom test', null));
                expect(errorService.getErrors().length).toEqual(length);
            });
        });

        describe('addValidationError()', () => {
            it('should add a validation error to errors', () => {
                const msgKey = 'TEST';
                const args = {
                    fieldName: 'field',
                    minlenght: '10'
                };
                expect(args).toBeTruthy();
                errorService.addValidationError(msgKey, args);

                const errors = errorService.getErrors();
                expect(errors.length === 1).toBeTruthy();
                const error = errors[0];
                expect(error.severity === TSErrorLevel.SEVERE);
                expect(error.msgKey).toBe(msgKey);
                expect(error.argumentList).toEqual(args);
                expect(error.type).toBe(TSErrorType.CLIENT_SIDE);
            });

            it('should ignore duplicated errors', () => {
                errorService.addValidationError('TEST');
                const length = errorService.getErrors().length;
                errorService.addValidationError('TEST');
                expect(errorService.getErrors().length === length).toBeTruthy();
            });

            it('should broadcast an UPDATE event', () => {
                errorService.addValidationError('TEST');
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.ERROR_UPDATE,
                    errorService.getErrors());
            });
        });

        describe('handleValidationError', () => {
            beforeEach(() => {
                errorService.handleValidationError(false, 'TEST');
            });

            it('should add a validation error on FALSE', () => {
                expect(errorService.getErrors()[0].msgKey).toBe('TEST');
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.ERROR_UPDATE,
                    errorService.getErrors());
            });

            it('should remove a validation error on TRUE', () => {
                errorService.handleValidationError(false, 'TEST');
                expect(errorService.getErrors().length === 0);
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.ERROR_UPDATE,
                    errorService.getErrors());
            });
        });

        describe('clearAll()', () => {
            it('should clear all errors', () => {
                errorService.addValidationError('foo');
                expect(errorService.getErrors().length === 1).toBeTruthy();
                expect($rootScope.$broadcast).not.toHaveBeenCalledWith(TSMessageEvent.CLEAR);

                errorService.clearAll();
                expect(errorService.getErrors().length === 0).toBeTruthy();
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.CLEAR);
            });
        });

        describe('clearError()', () => {
            it('should remove the specified error', () => {
                errorService.addValidationError('KEEP');
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.ERROR_UPDATE,
                    errorService.getErrors());
                errorService.addValidationError('REMOVE');
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.ERROR_UPDATE,
                    errorService.getErrors());
                errorService.clearError('REMOVE');
                expect($rootScope.$broadcast).toHaveBeenCalledWith(TSMessageEvent.ERROR_UPDATE,
                    errorService.getErrors());
                const errors = errorService.getErrors();
                expect(errors.length === 1).toBeTruthy();
                expect(errors[0].msgKey).toBe('KEEP');
            });
        });
    });
});
