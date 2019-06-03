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

import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;
import {IPromise} from 'angular';

export class StepDialogController {

    public static $inject = [
        '$mdDialog',
        '$translate',
        'title',
        'questionText',
        'cancelText',
        'firstOkText',
        'radioYes',
        'radioYesHint',
        'radioNo',
        'radioNoHint',
        'checkboxLabel',
        'titleStep2',
        'warning',
        'backText',
        'nextText',
        'finishText',
    ];

    public title: string;
    public questionText: string;
    public cancelText: string;
    public firstOkText: string;
    public radioYes: string;
    public radioYesHint: string;
    public radioNo: string;
    public radioNoHint: string;
    public selected: number = 0;
    public confirmed: boolean = false;
    public stepStateStore: Array<boolean> = [true, false];
    public checkboxLabel: string;
    public titleStep2: string;
    public warning: string;
    public backText: string;
    public nextText: string;
    public finishText: string;

    public constructor(
        private readonly $mdDialog: IDialogService,
        $translate: ITranslateService,
        title: string,
        questionText: string,
        cancelText: string,
        firstOkText: string,
        radioYes: string,
        radioYesHint: string,
        radioNo: string,
        radioNoHint: string,
        checkboxLabel: string,
        titleStep2: string,
        warning: string,
        backText: string,
        nextText: string,
        finishText: string,
    ) {
        this.title = $translate.instant(title);
        this.questionText = $translate.instant(questionText);
        this.cancelText = $translate.instant(cancelText);
        this.firstOkText = $translate.instant(firstOkText);
        this.radioYes = $translate.instant(radioYes);
        this.radioYesHint = $translate.instant(radioYesHint);
        this.radioNo = $translate.instant(radioNo);
        this.radioNoHint = $translate.instant(radioNoHint);
        this.checkboxLabel = $translate.instant(checkboxLabel);
        this.titleStep2 = $translate.instant(titleStep2);
        this.warning = $translate.instant(warning);
        this.backText = $translate.instant(backText);
        this.nextText = $translate.instant(nextText);
        this.finishText = $translate.instant(finishText);
    }

    public next(): void {
        if (!this.stepStateStore[1] && this.selected === 2) {
            this.stepStateStore = [false, true];
        } else {
            this.hide();
        }
    }

    public back(): void {
        this.stepStateStore = [true, false];
    }

    public hide(): IPromise<any> {
        return this.$mdDialog.hide(this.selected);
    }

    public cancel(): void {
        this.$mdDialog.cancel();
    }

    public isStepDisplayed(step: number): boolean {
        return this.stepStateStore[step];
    }
}
