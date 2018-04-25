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

import {IDirective, IDirectiveFactory} from 'angular';
import {TSHTTPEvent} from '../../events/TSHTTPEvent';
import IHttpService = angular.IHttpService;
import ITimeoutService = angular.ITimeoutService;
import IFormController = angular.IFormController;
import IAttributes = angular.IAttributes;
import ILogService = angular.ILogService;

let template = require('./dv-loading-button.html');

interface IDVLoadingButtonController {
    isDisabled: boolean;
    buttonDisabled: boolean;
}

export class DVLoadingButton implements IDirective {
    transclude = true;
    restrict = 'E';
    require: any = {dvLoadingButtonCtrl: 'dvLoadingButton', formCtrl: '^?form'};
    scope = {};
    template = template;
    controller = DVLoadingButtonController;
    controllerAs = 'vm';
    bindToController = {
        type: '@',
        delay: '@',
        buttonClass: '@',
        forceWaitService: '@',
        buttonDisabled: '<',
        ariaLabel: '@',
        buttonClick: '&',
        inputId: '@'
    };

    static factory(): IDirectiveFactory {
        const directive = () => new DVLoadingButton();
        directive.$inject = [];
        return directive;
    }
}

/**
 * Button that disables itself after clicking to prevent multiclicks. If embedded in a form-controller it will check if
 * the form is valid first. If not it will not disable itself.
 * By default the button will be disabled till the next REST servicecall returns (not neceserally the one that was
 * triggered by this button) or till 400 ms have expired
 * @example:
 *
 <dv-loading-button type="submit"
 button-click="vm.mySaveFunction()"
 button-class="btn btn-sm btn-success"
 button-disabled="!vm.isButtonDisabled()">
 <i class="glyphicon glyphicon-plus"></i>
 <span data-translate="SAVE"></span>
 </dv-loading-button>
 *
 */
export class DVLoadingButtonController implements IDVLoadingButtonController {
    static $inject: string[] = ['$http', '$scope', '$timeout', '$attrs', '$log'];

    buttonClicked: ($event: any) => void;
    isDisabled: boolean;
    formCtrl: IFormController;
    delay: string;
    type: string;
    forceWaitService: string;
    buttonDisabled: boolean; //true wenn unser element programmatisch disabled wird
    buttonClick: () => void;

    /* @ngInject */
    constructor(private $http: IHttpService, private $scope: any, private $timeout: ITimeoutService,
                private $attrs: IAttributes, private $log: ILogService) {
    }

    //wird von angular aufgerufen
    $onInit() {
        if ('ngClick' in this.$attrs) {
            this.$log.error('must not use ng-click on dv-loading-button', this);
        }
        if ('ngDisabled' in this.$attrs) {
            this.$log.error('must not use ng-disabled on dv-loading-button', this);
        }
        if (!this.type) {
            this.type = 'button'; //wenn kein expliziter type angegeben wurde nehmen wir default button
        }

        this.buttonClicked = ($event: any) => {
            //wenn der button disabled ist machen wir mal gar nichts
            if (this.buttonDisabled || this.isDisabled) {
                return;
            }
            this.buttonClick();
            $event.stopPropagation();
            //falls ein button-click callback uebergeben wurde ausfuehren

            //timeout wird gebraucht damit der request nach dem disablen ueberhaupt uebermittelt wird
            this.$timeout(() => {
                if (!this.forceWaitService) {
                    if (this.formCtrl) {  //wenn form-controller existiert
                        //button wird nur disabled wenn form valid
                        if (this.formCtrl.$valid) {
                            this.disableForDelay();
                        }
                    } else { //wenn kein form einfach mal disablen fuer delay ms

                        this.disableForDelay();
                    }
                } else {
                    //wir warten auf naechsten service return, egal wie lange es dauert
                    this.isDisabled = true;
                }
            }, 0);

        };

        this.$scope.$on(TSHTTPEvent.REQUEST_FINISHED, (event: any) => {
            this.isDisabled = false;
        });

    }

    // beispiel wie man auf changes eines attributes von aussen reagieren kann
    $onChanges(changes: any) {
        if (changes.buttonDisabled && !changes.buttonDisabled.isFirstChange()) {
            this.buttonDisabled = changes.buttonDisabled.currentValue;
        }

    }

    private getDelay(): number {
        if (this.delay) {
            let parsedNum = parseInt(this.delay);
            if (parsedNum !== undefined && parsedNum !== null) {
                return parsedNum;
            }
        }
        return 4000;   //default delay = 4000 MS
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
