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

import {IComponentOptions} from 'angular';
import {ShowTooltipController} from '../../../../gesuch/dialog/ShowTooltipController';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {IDVFocusableController} from '../IDVFocusableController';
import ITranslateService = angular.translate.ITranslateService;

const showTooltipTemplate = require('../../../../gesuch/dialog/showTooltipTemplate.html');

export class DvTooltipComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./dv-tooltip.html');
    public controller = DvTooltipController;
    public controllerAs = 'vm';
    public bindings = {
        text: '<',
        inputId: '@'
    };
}

export class DvTooltipController implements IDVFocusableController {

    public static $inject: ReadonlyArray<string> = ['$translate', 'DvDialog'];

    private readonly inputId: string;

    public constructor(private readonly $translate: ITranslateService,
                       private readonly DvDialog: DvDialog) {
    }

    public showTooltip(info: any): void {
        this.DvDialog.showDialogFullscreen(showTooltipTemplate, ShowTooltipController, {
            title: '',
            text: info,
            parentController: this
        });
    }

    /**
     * Sets the focus back to the tooltip icon.
     */
    public setFocusBack(elementID: string): void {
        angular.element('#' + this.inputId + '.fa.fa-info-circle').first().focus();
    }
}
