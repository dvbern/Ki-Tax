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

import {TSErrorType} from './enums/TSErrorType';
import {TSErrorLevel} from './enums/TSErrorLevel';
import {TSErrorAction} from './enums/TSErrorAction';

export default class TSExceptionReport {

    get type(): TSErrorType {
        return this._type;
    }

    set type(value: TSErrorType) {
        this._type = value;
    }

    get severity(): TSErrorLevel {
        return this._severity;
    }

    set severity(value: TSErrorLevel) {
        this._severity = value;
    }

    get msgKey(): string {
        return this._msgKey;
    }

    set msgKey(value: string) {
        this._msgKey = value;
    }

    get exceptionName(): string {
        return this._exceptionName;
    }

    set exceptionName(value: string) {
        this._exceptionName = value;
    }

    get methodName(): string {
        return this._methodName;
    }

    set methodName(value: string) {
        this._methodName = value;
    }

    get translatedMessage(): string {
        return this._translatedMessage;
    }

    set translatedMessage(value: string) {
        this._translatedMessage = value;
    }

    get customMessage(): string {
        return this._customMessage;
    }

    set customMessage(value: string) {
        this._customMessage = value;
    }

    get errorCodeEnum(): string {
        return this._errorCodeEnum;
    }

    set errorCodeEnum(value: string) {
        this._errorCodeEnum = value;
    }

    get stackTrace(): string {
        return this._stackTrace;
    }

    set stackTrace(value: string) {
        this._stackTrace = value;
    }

    get objectId(): string {
        return this._objectId;
    }

    set objectId(value: string) {
        this._objectId = value;
    }

    get argumentList(): any {
        return this._argumentList;
    }

    set argumentList(value: any) {
        this._argumentList = value;
    }

    get path(): string {
        return this._path;
    }

    set path(value: string) {
        this._path = value;
    }

    get action(): TSErrorAction {
        return this._action;
    }

    set action(value: TSErrorAction) {
        this._action = value;
    }

    private _type: TSErrorType;
    private _severity: TSErrorLevel;
    private _msgKey: string;

    //fields for ExceptionReport entity
    private _exceptionName: string;
    private _methodName: string;
    private _translatedMessage: string;
    private _customMessage: string;
    private _errorCodeEnum: string;
    private _stackTrace: string;
    private _objectId: string;
    private _argumentList: any;

    // fields for ViolationReports
    private _path: string;

    private _action: TSErrorAction = undefined;

    /**
     *
     * @param type Type of the Error
     * @param severity Severity of the Error
     * @param msgKey This is the message key of the error. can also be the message itself
     * @param args
     */
    constructor(type: TSErrorType, severity: TSErrorLevel, msgKey: string, args: any) {
        this._type = type || null;
        this._severity = severity || null;
        this._msgKey = msgKey || null;
        this._argumentList = args || [];
        this._action = undefined;
    }

    public static createFromViolation(constraintType: string, message: string, path: string, value: string): TSExceptionReport {
        const report: TSExceptionReport = new TSExceptionReport(TSErrorType.VALIDATION, TSErrorLevel.SEVERE, message, value);
        report.path = path;
        //hint: here we could also pass along the path to the Exception Report
        return report;
    }

    public static createClientSideError(severity: TSErrorLevel, msgKey: string, args: any): TSExceptionReport {
        return new TSExceptionReport(TSErrorType.CLIENT_SIDE, severity, msgKey, args);
    }

    /**
     * takes a data Object that matches the fields of a EbeguExceptionReport and transforms them to a TSExceptionReport.
     * @param data
     * @returns {TSExceptionReport}
     */
    public static createFromExceptionReport(data: any) {
        const msgToDisp = data.translatedMessage || data.customMessage || 'ERROR_UNEXPECTED_EBEGU_RUNTIME';
        const exceptionReport: TSExceptionReport = new TSExceptionReport(TSErrorType.BADREQUEST, TSErrorLevel.SEVERE, msgToDisp, data.argumentList);
        exceptionReport.errorCodeEnum = data.errorCodeEnum;
        exceptionReport.exceptionName = data.exceptionName;
        exceptionReport.methodName = data.methodName;
        exceptionReport.stackTrace = data.stackTrace;
        exceptionReport.translatedMessage = msgToDisp;
        exceptionReport.customMessage = data.customMessage;
        exceptionReport.objectId = data.objectId;
        exceptionReport.argumentList = data.argumentList;
        exceptionReport.addActionToMessage();
        return exceptionReport;

    }

    isConstantValue(constant: any, value: any) {
        const keys = Object.keys(constant);
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < keys.length; i++) {
            if (value === constant[keys[i]]) {
                return true;
            }
        }

        return false;
    }

    isValid() {
        const validType = this.isConstantValue(TSErrorType, this.type);
        const validSeverity = this.isConstantValue(TSErrorLevel, this.severity);
        const validMsgKey = typeof this.msgKey === 'string' && this.msgKey.length > 0;

        return validType && validSeverity && validMsgKey;
    }

    isInternal() {
        return this.type === TSErrorType.INTERNAL;
    }

    private addActionToMessage(): void {
        if (this.errorCodeEnum === 'ERROR_EXISTING_ONLINE_MUTATION') {
            this.action = TSErrorAction.REMOVE_ONLINE_MUTATION;

        } else if (this.errorCodeEnum === 'ERROR_EXISTING_ERNEUERUNGSGESUCH') {
            this.action = TSErrorAction.REMOVE_ONLINE_ERNEUERUNGSGESUCH;
        }
    }
}
