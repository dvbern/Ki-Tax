/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

import {IPromise} from 'angular';
import {FreigabeViewController} from '../component/freigabeView/freigabeView';
import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;

export class FreigabeDialogController {

    public static $inject = ['$mdDialog', '$translate', 'parentController'];

    public deleteText: string;
    public title: string;
    public cancelText: string;
    public confirmText: string;

    public constructor(
        private readonly $mdDialog: IDialogService,
        $translate: ITranslateService,
        private readonly parentController: FreigabeViewController
    ) {
        this.title = $translate.instant('CONFIRM_GESUCH_FREIGEBEN');
        this.deleteText = $translate.instant('CONFIRM_GESUCH_FREIGEBEN_DESCRIPTION');
        this.cancelText = $translate.instant('LABEL_NEIN');
        this.confirmText = $translate.instant('LABEL_JA');
    }

    public hide(): IPromise<any> {
        this.parentController.confirmationCallback();
        return this.$mdDialog.hide();
    }

    public cancel(): void {
        this.$mdDialog.cancel();
    }
}
