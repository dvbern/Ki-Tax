/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {IComponentOptions, IController} from 'angular';
import ITranslateService = angular.translate.ITranslateService;
import {ShowTooltipController} from '../../../../gesuch/dialog/ShowTooltipController';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';

const showTooltipTemplate = require('../../../../gesuch/dialog/showTooltipTemplate.html');

export class DvFooterComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./dv-footer.html');
    public controller = DvFooterComponent;
    public controllerAs = 'vm';
}

export class DvFooterComponent implements IController {

    public static $inject: ReadonlyArray<string> = [
        '$translate',
        'DvDialog'
    ];

    public constructor( private readonly $translate: ITranslateService,
                        private readonly dvDialog: DvDialog) {
    }

    public showText(info: string): void {
        this.dvDialog.showDialogFullscreen(showTooltipTemplate, ShowTooltipController, {
            title: '',
            text: this.$translate.instant(info),
            parentController: this
        });
    }

    // we dont need a fallback here
    public setFocusBack(_elementID: string): void {
    }
}
