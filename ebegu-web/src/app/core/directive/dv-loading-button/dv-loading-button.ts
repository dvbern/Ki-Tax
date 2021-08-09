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

import {SimpleChanges} from '@angular/core';
import {IComponentOptions, IController} from 'angular';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {TSHTTPEvent} from '../../events/TSHTTPEvent';
import {LogFactory} from '../../logging/LogFactory';
import {BroadcastService} from '../../service/broadcast.service';
import IFormController = angular.IFormController;
import ITimeoutService = angular.ITimeoutService;

const LOG = LogFactory.createLog('DVLoadingButton');

interface IDVLoadingButtonController {
    isDisabled: boolean;
    buttonDisabled: boolean;
}

export class DVLoadingButton implements IComponentOptions {
    public transclude = true;
    public require = {dvLoadingButtonCtrl: 'dvLoadingButton', formCtrl: '^?form'};
    public template = require('./dv-loading-button.html');
    public controller = DVLoadingButtonController;
    public controllerAs = 'vm';
    public bindings = {
        type: '@',
        delay: '@',
        buttonClass: '@',
        forceWaitService: '@',
        buttonDisabled: '<',
        ariaLabel: '@',
        buttonClick: '&',
        inputId: '@',
    };
}

/**
 * Button that disables itself after clicking to prevent multiclicks. If embedded in a form-controller it will check if
 * the form is valid first. If not it will not disable itself.
 * By default the button will be disabled till the next REST servicecall returns (not neceserally the one that was
 * triggered by this button) or till 400 ms have expired
 * @example: <dv-loading-button type="submit" button-click="vm.mySaveFunction()" button-class="btn btn-sm btn-success"
 *         button-disabled="!vm.isButtonDisabled()"> <i class="glyphicon glyphicon-plus"></i> <span
 *         data-translate="SAVE"></span> </dv-loading-button>
 */
export class DVLoadingButtonController implements IDVLoadingButtonController, IController {
    public static $inject: string[] = ['$scope', '$timeout', 'BroadcastService'];

    public buttonClicked: ($event: any) => void;
    public isDisabled: boolean;
    public formCtrl: IFormController;
    public delay: string;
    public type: string;
    public forceWaitService: string;
    public buttonDisabled: boolean; // true wenn unser element programmatisch disabled wird
    public buttonClick: () => void;

    private readonly _unsubscribe: Subject<void> = new Subject<void>();

    public constructor(
        private readonly $scope: any,
        private readonly $timeout: ITimeoutService,
        private readonly broadcastService: BroadcastService
    ) {
    }

    // wird von angular aufgerufen
    public $onInit(): void {
        if (!this.type) {
            this.type = 'button'; // wenn kein expliziter type angegeben wurde nehmen wir default button
        }

        this.buttonClicked = $event => {
            // wenn der button disabled ist machen wir mal gar nichts
            if (this.buttonDisabled || this.isDisabled) {
                return;
            }
            this.buttonClick();
            $event.stopPropagation();
            // falls ein button-click callback uebergeben wurde ausfuehren

            // timeout wird gebraucht damit der request nach dem disablen ueberhaupt uebermittelt wird
            this.$timeout(() => {
                if (this.forceWaitService) {
                    // wir warten auf naechsten service return, egal wie lange es dauert
                    this.isDisabled = true;

                    return;
                }
                if (!this.formCtrl) { // wenn kein form einfach mal disablen fuer delay ms
                    this.disableForDelay();

                    return;
                }
                // button wird nur disabled wenn form valid
                if (this.formCtrl.$valid) {
                    this.disableForDelay();
                }
            }, 0);

        };

        this.$scope.$on(TSHTTPEvent.REQUEST_FINISHED, () => {
            this.isDisabled = false;
        });

        this.broadcastService.on$(TSHTTPEvent.REQUEST_FINISHED)
            .pipe(takeUntil(this._unsubscribe))
            .subscribe(() => {
            this.isDisabled = false;
        }, error => LOG.error(error));

    }

    public $onDestroy(): void {
        this._unsubscribe.next();
    }

    // beispiel wie man auf changes eines attributes von aussen reagieren kann
    public $onChanges(changes: SimpleChanges): void {
        if (changes.buttonDisabled && !changes.buttonDisabled.isFirstChange()) {
            this.buttonDisabled = changes.buttonDisabled.currentValue;
        }

    }

    private getDelay(): number {
        if (this.delay) {
            const parsedNum = parseInt(this.delay, 10);
            if (parsedNum !== undefined && parsedNum !== null) {
                return parsedNum;
            }
        }

        // tslint:disable-next-line:no-magic-numbers
        return 4000;   // default delay = 4000 MS
    }

    /**
     * disabled den Button fuer "delay" millisekunden
     */
    private disableForDelay(): void {
        this.isDisabled = true;
        this.$timeout(() => {
            this.isDisabled = false;
        }, this.getDelay());

    }
}
