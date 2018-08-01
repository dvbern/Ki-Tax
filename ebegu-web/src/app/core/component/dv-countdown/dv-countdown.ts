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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IIntervalService} from 'angular';
import * as moment from 'moment';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {OkDialogController} from '../../../../gesuch/dialog/OkDialogController';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {TSHTTPEvent} from '../../events/TSHTTPEvent';
import IPromise = angular.IPromise;
import IRootScopeService = angular.IRootScopeService;

const template = require('./dv-countdown.html');
const dialogTemplate = require('../../../../gesuch/dialog/okDialogTemplate.html');

export class DvCountdownComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {};
    template = template;
    controller = DvCountdownController;
    controllerAs = 'vm';
}

export class DvCountdownController {

    static $inject: ReadonlyArray<string> = ['AuthServiceRS', '$state', '$interval', '$rootScope', 'DvDialog', 'GesuchModelManager'];

    TSRoleUtil: any;
    timer: moment.Duration;
    timerInterval: IPromise<any>;

    constructor(private readonly authServiceRS: AuthServiceRS,
                private readonly $state: StateService,
                private readonly $interval: IIntervalService,
                private readonly $rootScope: IRootScopeService,
                private readonly DvDialog: DvDialog,
                private readonly gesuchModelManager: GesuchModelManager) {
    }

    $onInit() {
        this.TSRoleUtil = TSRoleUtil;
        this.$rootScope.$on(TSHTTPEvent[TSHTTPEvent.REQUEST_FINISHED], () => {
            if (this.authServiceRS.isRole(TSRole.GESUCHSTELLER) && this.isGesuchAvailableAndWritable() && this.isOnGesuchView()) {
                if (this.timerInterval === undefined) {
                    this.startTimer();
                } else {
                    this.resetTimer();
                }
            } else {
                this.cancelInterval();
            }

        });

    }

    public getTimeLeft(): string {
        if (this.timer) {
            if (this.timer.asMinutes() < 5) {
                return this.timer.minutes() + ' : ' + (this.timer.seconds() < 10 ? '0' + this.timer.seconds() : this.timer.seconds());
            }
        }
        return '';
    }

    public decrease() {
        if (this.timer.asMilliseconds() <= 0) {
            this.stopTimer();
        } else {
            this.timer = moment.duration(this.timer.asSeconds() - 1, 'seconds');
        }
    }

    //Fuer Testzwecke hier auf 5 setzen, ab dann erscheint der Countdown
    public resetTimer(): void {
        this.timer = moment.duration(10, 'minutes');
    }

    public stopTimer(): void {
        this.cancelInterval();
        this.DvDialog.showDialog(dialogTemplate, OkDialogController, {
            title: 'Bitte fahren Sie mit der Bearbeitung fort',
        });
    }

    public cancelInterval(): void {
        if (this.timerInterval !== undefined) {
            this.$interval.cancel(this.timerInterval);
            this.timerInterval = undefined;
            this.timer = undefined;
        }
    }

    public startTimer(): void {
        this.resetTimer();
        //Fuer Testzwecke hier auf 10 oder 100 setzen
        this.timerInterval = this.$interval(this.decrease.bind(this), 1000);
    }

    public isOnGesuchView(): boolean {
        return (this.$state.current && this.$state.current.name.substring(0, 7) === 'gesuch.');
    }

    public isGesuchAvailableAndWritable(): boolean {
        // verursacht login problem wenn man nicht schon eingelogged ist (gesuch model manager versucht services aufzurufen)
        if (this.gesuchModelManager.getGesuch()) {
            return !this.gesuchModelManager.isGesuchReadonly();
        }
        return false;
    }
}
