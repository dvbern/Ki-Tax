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
import {IComponentOptions, IFormController, ILogService} from 'angular';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import AbstractAdminViewController from '../../abstractAdminView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class ParameterViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./parameterView.html');
    controller = ParameterViewController;
    controllerAs = 'vm';
}

export class ParameterViewController extends AbstractAdminViewController {
    static $inject = ['GesuchsperiodeRS', '$translate',
        '$log', '$state', '$timeout', 'AuthServiceRS'];

    form: IFormController;
    gesuchsperiodenList: Array<TSGesuchsperiode> = [];
    jahr: number;

    constructor(private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                private readonly $translate: ITranslateService,
                private readonly $log: ILogService,
                private readonly $state: StateService,
                private readonly $timeout: ITimeoutService,
                authServiceRS: AuthServiceRS) {
        super(authServiceRS);
        $timeout(() => {
            this.readGesuchsperioden();
        });
    }

    private readGesuchsperioden(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: Array<TSGesuchsperiode>) => {
            this.gesuchsperiodenList = response; //angular.copy(response);
        });
    }

    gesuchsperiodeClicked(gesuchsperiode: any) {
        if (gesuchsperiode.isSelected) {
            this.$state.go('admin.gesuchsperiode', {
                gesuchsperiodeId: gesuchsperiode.id
            });
        }
    }

    createGesuchsperiode(): void {
        this.$state.go('admin.gesuchsperiode', {
            gesuchsperiodeId: undefined
        });
    }

    getStatusTagesschulenFreischaltung(gp: TSGesuchsperiode): string {
        if (gp.hasTagesschulenAnmeldung()) {
            if (gp.isTagesschulenAnmeldungKonfiguriert()) {
                return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_KONFIGURIERT');
            } else {
                return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_NOT_YET');
            }
        } else {
            return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_NONE');
        }
    }
}
