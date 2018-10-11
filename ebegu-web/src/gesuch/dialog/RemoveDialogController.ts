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

import {IPromise} from 'angular';
import {IDVFocusableController} from '../../app/core/component/IDVFocusableController';
import EbeguUtil from '../../utils/EbeguUtil';
import ILogService = angular.ILogService;
import IQService = angular.IQService;
import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;

export class RemoveDialogController {

    public static $inject = [
        '$mdDialog',
        '$translate',
        '$q',
        '$log',
        'title',
        'deleteText',
        'parentController',
        'elementID',
        'form',
    ];

    public deleteText: string;
    public title: string;

    public constructor(
        private readonly $mdDialog: IDialogService,
        $translate: ITranslateService,
        private readonly $q: IQService,
        private readonly $log: ILogService,
        title: string,
        deleteText: string,
        private readonly parentController: IDVFocusableController,
        private readonly elementID: string,
        private readonly form: any,
    ) {

        this.deleteText = EbeguUtil.isNotNullOrUndefined(deleteText) ?
            $translate.instant(deleteText) :
            $translate.instant('LOESCHEN_DIALOG_TEXT');

        this.title = EbeguUtil.isNotNullOrUndefined(title) ?
            $translate.instant(title) :
            $translate.instant('LOESCHEN_DIALOG_TITLE');
    }

    public hide(): IPromise<any> {
        return this.$mdDialog.hide();
    }

    public cancel(): void {
        if (this.parentController) {
            this.parentController.setFocusBack(this.elementID);
        }

        /*Es kann sein, dass die DialogBox durch einen Button mit Type submit ausgelösst wird. Wenn wir in der DialogBox jedoch auf
         * cancel drücken, müssen wir die form wieder auf dirty setzen, um Randeffekte zu umgehen. See EBEGU-1557*/
        if (this.form) {
            this.form.$setDirty();
        } else {
            this.$log.info('Cancel DialogController without setting form back to dirty may produce errors');
        }

        this.$mdDialog.cancel(this.$q.reject());
    }
}
