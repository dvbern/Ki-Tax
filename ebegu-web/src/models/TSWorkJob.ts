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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSBatchJobInformation from './TSBatchJobInformation';

/**
 * DTO fuer einen WorkJob
 */
export default class TSWorkJob extends TSAbstractMutableEntity {

    private _workJobType: string;
    private _startinguser: string;
    private _batchJobStatus: string;
    private _params: string;
    private _requestURI: string;
    private _resultData: string;
    private _executionId: string;
    private _execution:  TSBatchJobInformation;

    public get workJobType(): string {
        return this._workJobType;
    }

    public set workJobType(value: string) {
        this._workJobType = value;
    }

    public get startinguser(): string {
        return this._startinguser;
    }

    public set startinguser(value: string) {
        this._startinguser = value;
    }

    public get batchJobStatus(): string {
        return this._batchJobStatus;
    }

    public set batchJobStatus(value: string) {
        this._batchJobStatus = value;
    }

    public get params(): string {
        return this._params;
    }

    public set params(value: string) {
        this._params = value;
    }

    public get executionId(): string {
        return this._executionId;
    }

    public set executionId(value: string) {
        this._executionId = value;
    }

    public get requestURI(): string {
        return this._requestURI;
    }

    public set requestURI(value: string) {
        this._requestURI = value;
    }

    public get resultData(): string {
        return this._resultData;
    }

    public set resultData(value: string) {
        this._resultData = value;
    }

    public get execution(): TSBatchJobInformation {
        return this._execution;
    }

    public set execution(value: TSBatchJobInformation) {
        this._execution = value;
    }
}
