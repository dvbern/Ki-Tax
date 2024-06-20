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
import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;

export class ThreeButtonsDialogController {
    public static $inject = [
        '$mdDialog',
        '$translate',
        'title',
        'confirmationText',
        'cancelText',
        'firstOkText',
        'secondOkText'
    ];

    public confirmationText: string;
    public title: string;
    public cancelText: string;
    public firstOkText: string;
    public secondOkText: string;

    public constructor(
        private readonly $mdDialog: IDialogService,
        $translate: ITranslateService,
        title: string,
        confirmationText: string,
        cancelText: string,
        firstOkText: string,
        secondOkText: string
    ) {
        this.title = $translate.instant(title);
        this.confirmationText = $translate.instant(confirmationText);
        this.cancelText = $translate.instant(cancelText);
        this.firstOkText = $translate.instant(firstOkText);
        this.secondOkText = $translate.instant(secondOkText);
    }

    public hide(buttonNumber: number): IPromise<any> {
        return this.$mdDialog.hide(buttonNumber);
    }

    public cancel(): void {
        this.$mdDialog.cancel();
    }
}
