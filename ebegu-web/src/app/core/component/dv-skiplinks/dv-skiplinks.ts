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
import {IComponentOptions} from 'angular';
import {ShowTooltipController} from '../../../../gesuch/dialog/ShowTooltipController';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {IDVFocusableController} from '../IDVFocusableController';

const showKontaktTemplate = require('../../../../gesuch/dialog/showKontaktTemplate.html');

export class DvSkiplinksComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-skiplinks.html');
    public controller = DvSkiplinksController;
    public controllerAs = 'vm';
}

const gesuchstellerDashboard = 'gesuchsteller.dashboard';

export class DvSkiplinksController implements IDVFocusableController {

    public static $inject: ReadonlyArray<string> = ['$state', 'DvDialog', 'EbeguUtil'];

    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly $state: StateService,
        private readonly dvDialog: DvDialog,
        private readonly ebeguUtil: EbeguUtil,
    ) {
    }

    public goBackHome(): void {
        this.$state.go(gesuchstellerDashboard);
    }

    public isCurrentPageGSDashboard(): boolean {
        return (this.$state.current && this.$state.current.name === gesuchstellerDashboard);
    }

    public isCurrentPageGesuch(): boolean {
        return (this.$state.current &&
            this.$state.current.name !== gesuchstellerDashboard &&
            this.$state.current.name !== 'alleVerfuegungen.view' &&
            this.$state.current.name !== 'mitteilungen.view');
    }

    public focusLink(a: string): void {
        angular.element(a).focus();
    }

    public focusToolbar(): void {
        angular.element('.dossier-toolbar-gesuchsteller.desktop button').first().focus();
    }

    public focusSidenav(): void {
        angular.element('.sidenav.gesuchMenu button').first().focus();
    }

    /**
     * Sets the focus back to the Kontakt icon.
     */
    public setFocusBack(_elementID: string): void {
        angular.element('#SKIP_4').first().focus();
    }
}
