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

import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {TSErrorLevel} from '../../../../models/enums/TSErrorLevel';
import {TSErrorType} from '../../../../models/enums/TSErrorType';
import {TSExceptionReport} from '../../../../models/TSExceptionReport';
import {HTTP_ERROR_CODES} from '../../constants/CONSTANTS';
import {LogFactory} from '../../logging/LogFactory';
import {ErrorServiceX} from './ErrorServiceX';

const LOG = LogFactory.createLog('HttpErrorInterceptorX');

@Injectable()
export class HttpErrorInterceptorX implements HttpInterceptor {

    public constructor(
        private readonly errorService: ErrorServiceX,
    ) {
    }

    public static isIgnorableHttpError<T>(request: HttpRequest<T>): boolean {
        return request?.url?.includes('notokenrefresh') ||
            request?.url?.includes('emaillogin/gui/registration/createmaillogin');
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            catchError(async (err: HttpErrorResponse) => {
                if (!(err instanceof HttpErrorResponse)) {
                    throw err;
                }

                // here we handle all errorcodes except 401 and 403, 401 is handeld in HttpAuthInterceptor
                if (err.status === HTTP_ERROR_CODES.FORBIDDEN) {
                    this.errorService.addMesageAsError('ERROR_UNAUTHORIZED');
                    throw err;
                }

                if (err.status !== HTTP_ERROR_CODES.UNAUTHORIZED) {
                    // here we could analyze the http status of the response. But instead we check if the  response has
                    // the format of a known response such as errortypes such as violationReport or ExceptionReport and
                    // transform it as such. If the response matches know expected format we create an unexpected
                    // error.
                    const errors = await this.handleErrorResponse(req, err);
                    this.errorService.handleErrors(errors);
                    throw errors;
                }

                throw err;

            }),
        );
    }

    /**
     * Tries to determine what kind of response data the error-response retunred and  handles the data object
     * of the response accordingly.
     *
     * The expected types are ViolationReport objects from JAXRS if there was a beanValidation error
     * or EbeguExceptionReports in case there was some other application exception
     */
    private async handleErrorResponse(
        request: HttpRequest<any>,
        response: HttpErrorResponse,
    ): Promise<Array<TSExceptionReport>> {
        const url = request.url || '';
        if (response.status === HTTP_ERROR_CODES.NOT_FOUND && (
            url.includes('ebegu.dvbern.ch')
            || url.includes('ebegu-test.bern.ch')
            || url.includes('ebegu.bern.ch'))) {
            return [];
        }
        let errors: Array<TSExceptionReport>;
        // Alle daten loggen um das Debuggen zu vereinfachen
        // noinspection IfStatementWithTooManyBranchesJS
        try {
            if (this.isDataViolationResponse(response.error)) {
                errors = this.convertViolationReport(response.error);

            } else if (this.isDataEbeguExceptionReport(response.error)) {
                errors = this.convertEbeguExceptionReport(response.error);

            } else if (this.isBlob(response.error)) {
                errors = [];
                const errorObject = JSON.parse(await response.error.text());
                errors.push(TSExceptionReport.createFromExceptionReport(errorObject));
            } else if (this.isFileUploadException(response.error)) {
                errors = [];
                errors.push(new TSExceptionReport(TSErrorType.INTERNAL,
                    TSErrorLevel.SEVERE,
                    'ERROR_FILE_TOO_LARGE',
                    response.message));
            } else if (this.isOptimisticLockException(response)) {
                errors = [];
                errors.push(new TSExceptionReport(TSErrorType.INTERNAL,
                    TSErrorLevel.SEVERE,
                    'ERROR_DATA_CHANGED',
                    response.message));
            } else {
                LOG.error(`ErrorStatus: "${response.status}" StatusText: "${response.statusText}"`);
                LOG.error('ResponseData:' + JSON.stringify(response.message));
                // the error objects is neither a ViolationReport nor a ExceptionReport. Create a generic error msg
                errors = [];
                errors.push(new TSExceptionReport(TSErrorType.INTERNAL,
                    TSErrorLevel.SEVERE,
                    'ERROR_UNEXPECTED',
                    response.message));
            }
            return errors;
        } catch (e) {
            LOG.error('Could not handle error');
            LOG.error(response);
            return [new TSExceptionReport(TSErrorType.INTERNAL,
                TSErrorLevel.SEVERE,
                'ERROR_UNEXPECTED',
                response.message)];
        }
    }

    private convertViolationReport(data: any): Array<TSExceptionReport> {
        return [].concat(this.convertToExceptionReport(data.parameterViolations))
        .concat(this.convertToExceptionReport(data.classViolations))
        .concat(this.convertToExceptionReport(data.fieldViolations))
        .concat(this.convertToExceptionReport(data.propertyViolations))
        .concat(this.convertToExceptionReport(data.returnValueViolations));

    }

    private convertToExceptionReport(violations: any): Array<TSExceptionReport> {
        const exceptionReports: Array<TSExceptionReport> = [];
        if (violations) {
            for (const violationEntry of violations) {
                const constraintType: string = violationEntry.constraintType;
                const message: string = violationEntry.message;
                const path: string = violationEntry.path;
                const value: string = violationEntry.value;
                const report = TSExceptionReport.createFromViolation(constraintType, message, path, value);
                exceptionReports.push(report);
            }
        }
        return exceptionReports;

    }

    private convertEbeguExceptionReport(data: any): Array<TSExceptionReport> {
        const exceptionReport = TSExceptionReport.createFromExceptionReport(data);
        const exceptionReports: Array<TSExceptionReport> = [];
        exceptionReports.push(exceptionReport);
        return exceptionReports;

    }

    /**
     *
     * checks if response data json-object has the keys required to be a violationReport (from jaxRS)
     * @param data object whose keys are checked
     * @returns true if fields of violationReport are present
     */
    private isDataViolationResponse(data: any): boolean {
        // hier pruefen wir ob wir die Felder von org.jboss.resteasy.api.validation.ViolationReport.ViolationReport()
        // bekommen
        if (data !== null && data !== undefined) {
            const hasParamViol: boolean = data.hasOwnProperty('parameterViolations');
            const hasClassViol: boolean = data.hasOwnProperty('classViolations');
            const hasfieldViol: boolean = data.hasOwnProperty('fieldViolations');
            const hasPropViol: boolean = data.hasOwnProperty('propertyViolations');
            const hasRetViol: boolean = data.hasOwnProperty('returnValueViolations');
            return hasParamViol && hasClassViol && hasfieldViol && hasPropViol && hasRetViol;
        }
        return false;

    }

    private isDataEbeguExceptionReport(data: any): boolean {
        if (data !== null && data !== undefined) {
            const hassErrorCodeEnum: boolean = data.hasOwnProperty('errorCodeEnum');
            const hasExceptionName: boolean = data.hasOwnProperty('exceptionName');
            const hasMethodName: boolean = data.hasOwnProperty('methodName');
            const hasStackTrace: boolean = data.hasOwnProperty('stackTrace');
            const hasTranslatedMessage: boolean = data.hasOwnProperty('translatedMessage');
            const hasCustomMessage: boolean = data.hasOwnProperty('customMessage');
            const hasArgumentList: boolean = data.hasOwnProperty('argumentList');
            return hassErrorCodeEnum && hasExceptionName && hasMethodName && hasStackTrace
                && hasTranslatedMessage && hasCustomMessage && hasArgumentList;
        }
        return false;

    }

    private isFileUploadException(response: string | Blob): boolean {
        if (!response) {
            return false;
        }

        if (response instanceof Blob) {
            return true;
        }

        const msg = 'java.io.IOException: UT000020: Connection terminated as request was larger than ';

        return response.indexOf(msg) > -1;
    }

    private isOptimisticLockException(data: any): boolean {
        if (!data) {
            return false;
        }
        return data.status === HTTP_ERROR_CODES.CONFLICT;
    }

    private isBlob(error: any): boolean {
        return error instanceof Blob;
    }
}
