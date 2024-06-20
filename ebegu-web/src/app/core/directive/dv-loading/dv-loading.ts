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

import {
    IAttributes,
    IAugmentedJQuery,
    IDirective,
    IDirectiveFactory,
    IHttpService,
    IPromise,
    IScope,
    ITimeoutService
} from 'angular';
import {HttpPendingService} from '../../../shared/services/http-pending.service';

export class DVLoading implements IDirective {
    public restrict = 'A';
    public controller = DVLoadingController;
    public controllerAs = 'vm';

    public static factory(): IDirectiveFactory {
        const directive = () => new DVLoading();
        directive.$inject = [] as string[];
        return directive;
    }

    public link = (
        scope: IScope,
        element: IAugmentedJQuery,
        _attributes: IAttributes,
        controller: DVLoadingController
    ) => {
        let promise: IPromise<any>;
        scope.$watch(controller.isLoading, v => {
            if (v) {
                controller.$timeout.cancel(promise);
                element.show();
            } else {
                const delay = 500;
                promise = controller.$timeout(() => {
                    if (element) {
                        element.hide();
                    }
                }, delay);
            }
        });
    };
}

/**
 * Direktive  die ein Element ein oder ausblendet jenachdem ob ein http request pending ist
 */
export class DVLoadingController {
    public static $inject: string[] = [
        '$http',
        '$timeout',
        'HttpPendingService'
    ];

    public isLoading: () => boolean;

    public constructor(
        private readonly $http: IHttpService,
        public $timeout: ITimeoutService,
        private readonly httpPendingService: HttpPendingService
    ) {
        this.isLoading = (): boolean =>
            this.$http.pendingRequests.length > 0 ||
            this.httpPendingService.hasPendingRequests();
    }
}
