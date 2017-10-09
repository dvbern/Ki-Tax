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
import {IDVFocusableController} from '../../core/component/IDVFocusableController';
import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;

export class RemoveDialogController {

    static $inject = ['$mdDialog', '$translate', 'title', 'deleteText', 'parentController', 'elementID'];

    deleteText: string;
    title: string;

    constructor(private $mdDialog: IDialogService, $translate: ITranslateService, title: string, deleteText: string,
                private parentController: IDVFocusableController, private elementID: string) {
        if (deleteText !== undefined && deleteText !== null) {
            this.deleteText = $translate.instant(deleteText);
        } else {
            this.deleteText = $translate.instant('LOESCHEN_DIALOG_TEXT');
        }

        if (title !== undefined && title !== null) {
            this.title = $translate.instant(title);

        } else {
            this.title = $translate.instant('LOESCHEN_DIALOG_TITLE');
        }
    }

    public hide(): IPromise<any> {
        return this.$mdDialog.hide();
    }

    public cancel(): void {
        if (this.parentController) {
            this.parentController.setFocusBack(this.elementID);
        }
        this.$mdDialog.cancel();
    }
}
