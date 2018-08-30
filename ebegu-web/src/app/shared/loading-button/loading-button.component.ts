/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {IAttributes, IFormController, IHttpService, ILogService, ITimeoutService} from 'angular';
import {DEFAULT_BUTTON_DELAY} from '../../core/constants/CONSTANTS';

@Component({
    selector: 'dv-loading-button',
    templateUrl: './loading-button.component.html',
    styleUrls: ['./loading-button.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoadingButtonComponent implements OnInit, OnChanges {

    @Input() type: string;
    @Input() delay?: string;
    @Input() buttonClass: string;
    @Input() forceWaitService?: string;
    @Input() buttonDisabled?: boolean;
    @Input() ariaLabel?: string;
    @Input() buttonClick: () => void;
    @Input() inputId?: string;

    buttonClicked: ($event: any) => void;
    formCtrl: IFormController;
    isDisabled: boolean;

    constructor(private readonly $http: IHttpService,
                private readonly $timeout: ITimeoutService) {
    }

    ngOnInit() {
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
                if (this.forceWaitService) {
                    //wir warten auf naechsten service return, egal wie lange es dauert
                    this.isDisabled = true;
                } else {
                    if (this.formCtrl) {  //wenn form-controller existiert
                        //button wird nur disabled wenn form valid
                        if (this.formCtrl.$valid) {
                            this.disableForDelay();
                        }
                    } else { //wenn kein form einfach mal disablen fuer delay ms
                        this.disableForDelay();
                    }
                }
            }, 0);

        };
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.buttonDisabled && !changes.buttonDisabled.isFirstChange()) {
            this.buttonDisabled = changes.buttonDisabled.currentValue;
        }
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

    private getDelay(): number {
        if (this.delay) {
            const parsedNum = parseInt(this.delay);
            if (parsedNum !== undefined && parsedNum !== null) {
                return parsedNum;
            }
        }

        return DEFAULT_BUTTON_DELAY;
    }
}
