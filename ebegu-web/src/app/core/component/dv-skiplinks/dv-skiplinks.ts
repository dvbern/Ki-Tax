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

const template = require('./dv-skiplinks.html');
const showKontaktTemplate = require('../../../../gesuch/dialog/showKontaktTemplate.html');

export class DvSkiplinksComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {};
    template = template;
    controller = DvSkiplinksController;
    controllerAs = 'vm';
}

export class DvSkiplinksController implements IDVFocusableController {

    static $inject: ReadonlyArray<string> = ['$state', 'DvDialog', 'EbeguUtil'];

    TSRoleUtil: any;

    constructor(private readonly $state: StateService, private readonly DvDialog: DvDialog, private readonly ebeguUtil: EbeguUtil) {
        this.TSRoleUtil = TSRoleUtil;
    }

    public goBackHome(): void {
        this.$state.go('gesuchsteller.dashboard');
    }

    public isCurrentPageGSDashboard(): boolean {
        return (this.$state.current && this.$state.current.name === 'gesuchsteller.dashboard');
    }

    public isCurrentPageGesuch(): boolean {
        return (this.$state.current &&
            this.$state.current.name !== 'gesuchsteller.dashboard' &&
            this.$state.current.name !== 'alleVerfuegungen.view' &&
            this.$state.current.name !== 'mitteilungen.view');
    }

    public focusLink(a: string): void {
        angular.element(a).focus();
    }

    public focusToolbar(): void {
        angular.element('.gesuch-toolbar-gesuchsteller.desktop button').first().focus();
    }

    public focusSidenav(): void {
        angular.element('.sidenav.gesuchMenu button').first().focus();
    }

    public showKontakt(): void {
        this.DvDialog.showDialog(showKontaktTemplate, ShowTooltipController, {
            title: '',
            text: this.ebeguUtil.getKontaktJugendamt(),
            parentController: this
        });
    }

    /**
     * Sets the focus back to the Kontakt icon.
     */
    public setFocusBack(elementID: string): void {
        angular.element('#SKIP_4').first().focus();
    }
}
