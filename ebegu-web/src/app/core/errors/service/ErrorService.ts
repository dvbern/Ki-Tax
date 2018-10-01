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

import IRootScopeService = angular.IRootScopeService;
import {TSMessageEvent} from '../../../../models/enums/TSErrorEvent';
import TSExceptionReport from '../../../../models/TSExceptionReport';
import {TSErrorLevel} from '../../../../models/enums/TSErrorLevel';
import {TSErrorType} from '../../../../models/enums/TSErrorType';

export default class ErrorService {

    public static $inject = ['$rootScope'];

    public errors: Array<TSExceptionReport> = [];

    public constructor(private readonly $rootScope: IRootScopeService) {
    }

    public getErrors(): Array<TSExceptionReport> {
        return angular.copy(this.errors);
    }

    /**
     * Clears all stored errors
     */
    public clearAll() {
        this.errors = [];
        this.$rootScope.$broadcast(TSMessageEvent[TSMessageEvent.CLEAR]);
    }

    /** clear specific error
     * @param {string} msgKey
     */
    public clearError(msgKey: string) {
        // noinspection SuspiciousTypeOfGuard
        if (typeof msgKey !== 'string') {
            return;
        }

        const cleared = this.errors.filter(e => e.msgKey !== msgKey);

        if (cleared.length !== this.errors.length) {
            this.errors = cleared;
            this.$rootScope.$broadcast(TSMessageEvent[TSMessageEvent.ERROR_UPDATE], this.errors);
        }
    }

    /**
     * This can be used to add a client-siede global error
     * @param {string} msgKey translation key
     * @param {Object} [args] message parameters
     */
    public addValidationError(msgKey: string, args?: any) {
        const err = TSExceptionReport.createClientSideError(TSErrorLevel.SEVERE, msgKey, args);
        this.addDvbError(err);
    }

    public containsError(dvbError: TSExceptionReport) {
        return this.errors.filter(e => e.msgKey === dvbError.msgKey).length > 0;
    }

    public addDvbError(dvbError: TSExceptionReport) {
        if (dvbError && dvbError.isValid()) {
            if (!this.containsError(dvbError)) {
                this.errors.push(dvbError);
                const udateEvent = (dvbError.severity === TSErrorLevel.INFO ) ? TSMessageEvent.INFO_UPDATE : TSMessageEvent.ERROR_UPDATE;
                this.$rootScope.$broadcast(TSMessageEvent[udateEvent], this.errors);
            }
        } else {
            console.log('could not display received TSExceptionReport ' + dvbError);
        }
    }

    public addMesageAsError(msg: string) {
        const error = new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.SEVERE, msg, null);
        this.addDvbError(error);

    }

    public addMesageAsInfo(msg: string) {
        const error = new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.INFO, msg, null);
        this.addDvbError(error);
    }

    /**
     * @param {boolean} isValid when FALSE a new validationError is added. Otherwise the validationError is cleared
     * @param {string} msgKey
     * @param {Object} [args]
     */
    public handleValidationError(isValid: boolean, msgKey: string, args?: any) {
        // noinspection PointlessBooleanExpressionJS
        if (!!isValid) {
            this.clearError(msgKey);
        } else {
            this.addValidationError(msgKey, args);
        }
    }

    /**
     * adds a DvbError to the errors
     */
    public handleError(dvbError: TSExceptionReport) {
        this.addDvbError(dvbError);
    }

    /**
     * adds all Errors to the errors service
     */
    public handleErrors(dvbErrors: Array<TSExceptionReport>) {
        if (dvbErrors) {
            for (const err of dvbErrors) {
                this.addDvbError(err);
            }
        }

    }
}
