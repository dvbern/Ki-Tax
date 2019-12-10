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
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import TSLastenausgleich from '../../../models/TSLastenausgleich';
import {DvDialog} from '../../core/directive/dv-dialog/dv-dialog';
import {LogFactory} from '../../core/logging/LogFactory';
import LastenausgleichRS from '../../core/service/lastenausgleichRS.rest';
import IFormController = angular.IFormController;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

const LOG = LogFactory.createLog('LastenausgleichViewController');

export class LastenausgleichViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./lastenausgleichView.html');
    public controller = LastenausgleichViewController;
    public controllerAs = 'vm';
}

export class LastenausgleichViewController implements IController {

    public static $inject: string[] = [
        'LastenausgleichRS',
        'DvDialog',
        '$translate',
    ];

    public jahr: number;
    public selbstbehaltPro100ProzentPlatz: number;
    public lastenausgleiche: TSLastenausgleich[] = [];

    public form: IFormController;

    public constructor(
        private readonly lastenausgleichRS: LastenausgleichRS,
        private readonly dvDialog: DvDialog,
        private readonly $translate: ITranslateService,
    ) {
    }

    public $onInit(): void {
        this.lastenausgleichRS.getAllLastenausgleiche() .then((response: TSLastenausgleich[]) => {
            this.lastenausgleiche = response;
        });
    }

    public createLastenausgleich(): void {
        if (!this.form.$valid) {
            return;
        }
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: this.$translate.instant('LASTENAUSGLEICH_ERSTELLEN_TITLE'),
            deleteText: this.$translate.instant('LASTENAUSGLEICH_ERSTELLEN_INFO'),
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.lastenausgleichRS.createLastenausgleich(this.jahr, this.selbstbehaltPro100ProzentPlatz)
                .then((response: TSLastenausgleich) => {
                    this.lastenausgleiche.push(response);
                });
        }, err => {
            LOG.error(err);
        });
    }

    // todo: remove disable-next, when implemented.
    // tslint:disable-next-line
    public downloadExcel(lastenausgleich: TSLastenausgleich): angular.IPromise<void | never> {
        window.alert('not yet implemented');
        return undefined;
    }

    // todo: remove disable-next, when implemented.
    // tslint:disable-next-line
    public downloadCsv(lastenausgleich: TSLastenausgleich): void {
        window.alert('not yet implemented');
    }
}
