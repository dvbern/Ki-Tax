/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions} from 'angular';
import {ShowTooltipController} from '../../../../gesuch/dialog/ShowTooltipController';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {IDVFocusableController} from '../IDVFocusableController';

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
    public static $inject: ReadonlyArray<string> = ['DvDialog'];

    private readonly inputId: string;

    public constructor(private readonly dvDialog: DvDialog) {}

    public showTooltip(info: any, $event: any): void {
        $event.preventDefault();
        this.dvDialog.showDialogFullscreen(
            showTooltipTemplate,
            ShowTooltipController,
            {
                title: '',
                text: info,
                parentController: this
            }
        );
    }

    public isTextEmpty(text: string): boolean {
        return !text || text.length === 0;
    }

    /**
     * Sets the focus back to the tooltip icon.
     */
    public setFocusBack(_elementID: string): void {
        angular.element(`#${this.inputId}.fa.fa-info-circle`).first().focus();
    }
}
