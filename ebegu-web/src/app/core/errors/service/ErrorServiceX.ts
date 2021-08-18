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

import {Injectable} from '@angular/core';
import {TSMessageEvent} from '../../../../models/enums/TSErrorEvent';
import {TSErrorLevel} from '../../../../models/enums/TSErrorLevel';
import {TSErrorType} from '../../../../models/enums/TSErrorType';
import {TSExceptionReport} from '../../../../models/TSExceptionReport';
import {BroadcastService} from '../../service/broadcast.service';

@Injectable({
    providedIn: 'root'
})
export class ErrorServiceX {

    public errors: Array<TSExceptionReport> = [];

    public constructor(
        private readonly broadcastService: BroadcastService
    ) {
    }

    public getErrors(): Array<TSExceptionReport> {
        //TODO: no use angular
        return angular.copy(this.errors);
    }

    /**
     * Clears all stored errors
     */
    public clearAll(): void {
        this.errors = [];
        this.broadcastService.broadcast(TSMessageEvent[TSMessageEvent.CLEAR]);
    }

    /**
     * clear specific error
     */
    public clearError(msgKey: string): void {
        // noinspection SuspiciousTypeOfGuard
        if (typeof msgKey !== 'string') {
            return;
        }

        const cleared = this.errors.filter(e => e.msgKey !== msgKey);

        if (cleared.length !== this.errors.length) {
            this.errors = cleared;
            this.broadcastService.broadcast(TSMessageEvent[TSMessageEvent.ERROR_UPDATE], this.errors);
        }
    }

    /**
     * This can be used to add a client-siede global error
     * @param msgKey translation key
     * @param [args] message parameters
     */
    public addValidationError(msgKey: string, args?: any): void {
        const err = TSExceptionReport.createClientSideError(TSErrorLevel.SEVERE, msgKey, args);
        this.addDvbError(err);
    }

    public containsError(dvbError: TSExceptionReport): boolean {
        return this.errors.filter(e => e.msgKey === dvbError.msgKey).length > 0;
    }

    public addDvbError(dvbError: TSExceptionReport): void {
        if (!(dvbError && dvbError.isValid())) {
            console.log('could not display received TSExceptionReport', dvbError);
            return;
        }

        if (this.containsError(dvbError)) {
            return;
        }

        this.errors.push(dvbError);
        const udateEvent = (dvbError.severity === TSErrorLevel.INFO) ?
            TSMessageEvent.INFO_UPDATE :
            TSMessageEvent.ERROR_UPDATE;
        this.broadcastService.broadcast(TSMessageEvent[udateEvent], this.errors);
    }

    public addMesageAsError(msg: string): void {
        const error = new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.SEVERE, msg, null);
        this.addDvbError(error);

    }

    public addMesageAsInfo(msg: string): void {
        const error = new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.INFO, msg, null);
        this.addDvbError(error);
    }

    /**
     * when isValid FALSE a new validationError is added. Otherwise the validationError is cleared
     */
    public handleValidationError(isValid: boolean, msgKey: string, args?: any): void {
        // noinspection NegatedIfStatementJS
        if (!!isValid) {
            this.clearError(msgKey);
        } else {
            this.addValidationError(msgKey, args);
        }
    }

    /**
     * adds a DvbError to the errors
     */
    public handleError(dvbError: TSExceptionReport): void {
        this.addDvbError(dvbError);
    }

    /**
     * adds all Errors to the errors service
     */
    public handleErrors(dvbErrors: Array<TSExceptionReport>): void {
        if (!dvbErrors) {
            return;
        }

        for (const err of dvbErrors) {
            this.addDvbError(err);
        }
    }
}
