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

import {StateService, UIRouterGlobals} from '@uirouter/core';
import {IComponentOptions, IController, IIntervalService} from 'angular';
import * as moment from 'moment';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {OkDialogController} from '../../../../gesuch/dialog/OkDialogController';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {TSHTTPEvent} from '../../events/TSHTTPEvent';
import IPromise = angular.IPromise;
import IRootScopeService = angular.IRootScopeService;

const dialogTemplate = require('../../../../gesuch/dialog/okDialogTemplate.html');

export class DvCountdownComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-countdown.html');
    public controller = DvCountdownController;
    public controllerAs = 'vm';
}

export class DvCountdownController implements IController {

    public static $inject: ReadonlyArray<string> = [
        'AuthServiceRS',
        '$state',
        '$interval',
        '$rootScope',
        'DvDialog',
        'GesuchModelManager',
    ];

    public readonly TSRoleUtil = TSRoleUtil;
    public timer: moment.Duration;
    public timerInterval: IPromise<any>;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly $state: StateService,
        private readonly $interval: IIntervalService,
        private readonly $rootScope: IRootScopeService,
        private readonly dvDialog: DvDialog,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly uiRouterGlobals: UIRouterGlobals,
    ) {
    }

    public $onInit(): void {
        this.$rootScope.$on(TSHTTPEvent[TSHTTPEvent.REQUEST_FINISHED], () => {
            if (!this.authServiceRS.isRole(TSRole.GESUCHSTELLER)
                || !this.isGesuchAvailableAndWritable()
                || !this.isOnGesuchView()) {

                this.cancelInterval();
                return;
            }

            if (this.timerInterval === undefined) {
                this.startTimer();
            } else {
                this.resetTimer();
            }
        });

    }

    public getTimeLeft(): string {
        if (this.timer && this.timer.asMinutes() < 5) {
            const seconds = this.timer.seconds() < 10 ? '0' + String(this.timer.seconds()) : this.timer.seconds();
            return `${this.timer.minutes()} : ${seconds}`;
        }
        return '';
    }

    public decrease(): void {
        if (this.timer.asMilliseconds() <= 0) {
            this.stopTimer();
        } else {
            this.timer = moment.duration(this.timer.asSeconds() - 1, 'seconds');
        }
    }

    // Fuer Testzwecke hier auf 5 setzen, ab dann erscheint der Countdown
    public resetTimer(): void {
        this.timer = moment.duration(10, 'minutes');
    }

    public stopTimer(): void {
        this.cancelInterval();
        this.dvDialog.showDialog(dialogTemplate, OkDialogController, {
            title: 'Bitte fahren Sie mit der Bearbeitung fort',
        });
    }

    public cancelInterval(): void {
        if (this.timerInterval === undefined) {
            return;
        }

        this.$interval.cancel(this.timerInterval);
        this.timerInterval = undefined;
        this.timer = undefined;
    }

    public startTimer(): void {
        this.resetTimer();
        // Fuer Testzwecke hier auf 10 oder 100 setzen
        this.timerInterval = this.$interval(this.decrease.bind(this), 1000);
    }

    public isOnGesuchView(): boolean {
        return (this.uiRouterGlobals?.current && this.uiRouterGlobals.current.name.substring(0, 7) === 'gesuch.');
    }

    public isGesuchAvailableAndWritable(): boolean {
        // verursacht login problem wenn man nicht schon eingelogged ist (gesuch model manager versucht services
        // aufzurufen)
        if (this.gesuchModelManager.getGesuch()) {
            return !this.gesuchModelManager.isGesuchReadonly();
        }
        return false;
    }
}
