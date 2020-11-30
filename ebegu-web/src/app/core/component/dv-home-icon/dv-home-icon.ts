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
import {IComponentOptions} from 'angular';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';

export class DvHomeIconComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-home-icon.html');
    public controller = DvHomeIconController;
    public controllerAs = 'vm';
}

export class DvHomeIconController {

    public static $inject: ReadonlyArray<string> = ['$state', 'GesuchModelManager'];

    public readonly TSRoleUtil = TSRoleUtil;
    private readonly stateDashboard = 'gesuchsteller.dashboard';

    public constructor(
        private readonly $state: StateService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly routerGlobals: UIRouterGlobals) {
    }

    public goBackHome(): void {
        const dossier = this.gesuchModelManager.getDossier();
        if (dossier) {
            this.$state.go(this.stateDashboard, {dossierId: dossier.id});
        } else {
            this.$state.go(this.stateDashboard);
        }
    }

    public isCurrentPageGSDashboard(): boolean {
        return (this.routerGlobals?.current && this.routerGlobals.current.name === this.stateDashboard);
    }
}
