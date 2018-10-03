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
import {IComponentOptions, IFormController} from 'angular';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import AbstractAdminViewController from '../../abstractAdminView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class ParameterViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./parameterView.html');
    public controller = ParameterViewController;
    public controllerAs = 'vm';
}

export class ParameterViewController extends AbstractAdminViewController {
    public static $inject = ['GesuchsperiodeRS', '$translate', '$state', '$timeout', 'AuthServiceRS'];

    public form: IFormController;
    public gesuchsperiodenList: Array<TSGesuchsperiode> = [];
    public jahr: number;

    public constructor(
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly $translate: ITranslateService,
        private readonly $state: StateService,
        $timeout: ITimeoutService,
        authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
        $timeout(() => {
            this.readGesuchsperioden();
        });
    }

    private readGesuchsperioden(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: Array<TSGesuchsperiode>) => {
            this.gesuchsperiodenList = response;
        });
    }

    public gesuchsperiodeClicked(gesuchsperiode: any): void {
        if (!gesuchsperiode.isSelected) {
            return;
        }

        this.$state.go('admin.gesuchsperiode', {
            gesuchsperiodeId: gesuchsperiode.id,
        });
    }

    public createGesuchsperiode(): void {
        this.$state.go('admin.gesuchsperiode', {
            gesuchsperiodeId: undefined,
        });
    }

    public getStatusTagesschulenFreischaltung(gp: TSGesuchsperiode): string {
        if (gp.hasTagesschulenAnmeldung()) {
            return gp.isTagesschulenAnmeldungKonfiguriert() ?
                this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_KONFIGURIERT') :
                this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_NOT_YET');
        }

        return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_NONE');
    }
}
