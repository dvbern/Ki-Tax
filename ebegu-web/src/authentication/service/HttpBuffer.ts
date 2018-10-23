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

import IInjectorService = angular.auto.IInjectorService;
import {IDeferred, IHttpService, IRequestConfig} from 'angular';

/**
 * Code from Kita-Projekt, den wir in Typescript geschrieben haben
 * Adapted from https://github.com/witoldsz/angular-http-auth
 */
export default class HttpBuffer {

    public static $inject = ['$injector'];

    /** Holds all the requests, so they can be re-requested in future. */
    public buffer: Array<any> = [];

    /** Service initialized later because of circular dependency problem. */
    public $http: IHttpService;

    public constructor(private readonly $injector: IInjectorService) {
    }

    private retryHttpRequest(config: IRequestConfig, deferred: IDeferred<any>): void {
        function successCallback(response: any): void {
            deferred.resolve(response);
        }

        function errorCallback(response: any): void {
            deferred.reject(response);
        }

        this.$http = this.$http || this.$injector.get('$http');
        this.$http(config).then(successCallback, errorCallback);
    }

    /**
     * Appends HTTP request configuration object with deferred response attached to buffer.
     */
    public append(config: IRequestConfig, deferred: IDeferred<any>): void {
        this.buffer.push({
            config,
            deferred,
        });
    }

    /**
     * Abandon or reject (if reason provided) all the buffered requests.
     */
    public rejectAll(reason: any): void {
        if (reason) {
            this.buffer.forEach(b => b.deferred.reject(reason));
        }
        this.buffer = [];
    }

    /**
     * Retries all the buffered requests clears the buffer.
     */
    public retryAll(updater: any): void {
        this.buffer.forEach(b => this.retryHttpRequest(updater(b.config), b.deferred));
        this.buffer = [];
    }
}
