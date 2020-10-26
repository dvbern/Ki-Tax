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
import {TSZahlungslaufTyp} from '../../models/enums/TSZahlungslaufTyp';

export class StepDialogController {

    public static $inject = [
        '$mdDialog',
        '$translate',
        'institutionName',
        'institutionPhone',
        'zahlungslaufTyp',
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
    public radioYesCasesInfo: string;
    public radioYesCases: Array<string>;
    public radioNoCasesInfo: string;
    public radioNoCases: Array<string>;
    public institutionHint: string;

    public constructor(
        private readonly $mdDialog: IDialogService,
        $translate: ITranslateService,
        institutionName: string,
        institutionPhone: string,
        zahlungslaufTyp: TSZahlungslaufTyp,
    ) {
        const isInstitutionszahlung = TSZahlungslaufTyp.GEMEINDE_INSTITUTION === zahlungslaufTyp;

        // "Mutaton fuehrt zu Korrekturen von bereits ausbezahlten.."
        const titleKey = isInstitutionszahlung
            ? 'CONFIRM_SAVE_MUTIERTE_VERFUEGUNG'
            : 'CONFIRM_SAVE_MUTIERTE_VERFUEGUNG_MAHLZEITEN';
        this.title = $translate.instant(titleKey);

        this.questionText = $translate.instant('KORREKTURZAHLUNG_DIALOG_FRAGE');
        this.cancelText = $translate.instant('LABEL_NEIN');
        this.firstOkText = $translate.instant('WEITER_ONLY');
        this.radioYes = $translate.instant('KORREKTURZAHLUNG_DIALOG_OPTION_JA');

        // In diesem Fall erfolgt der Abgleich automatisch und wie bis anhin über kiBon direkt an ...
        const radioYesHintKey = isInstitutionszahlung
            ? 'KORREKTURZAHLUNG_DIALOG_OPTION_JA_DESCRIPTION'
            : 'KORREKTURZAHLUNG_DIALOG_OPTION_JA_DESCRIPTION_MAHLZEITEN';
        this.radioYesHint = $translate.instant(radioYesHintKey);

        this.radioNo = $translate.instant('KORREKTURZAHLUNG_DIALOG_OPTION_NEIN');
        this.radioNoHint = $translate.instant('KORREKTURZAHLUNG_DIALOG_OPTION_NEIN_DESCRIPTION');
        this.checkboxLabel = $translate.instant('KORREKTURZAHLUNG_DIALOG_CHECKBOX_LABEL');
        this.titleStep2 = $translate.instant('KORREKTURZAHLUNG_DIALOG_STEP2_TITLE');
        this.warning = $translate.instant('KORREKTURZAHLUNG_DIALOG_IMMUTABLE');
        this.backText = $translate.instant('KORREKTURZAHLUNG_DIALOG_BACK');
        this.nextText = $translate.instant('WEITER_ONLY');
        this.finishText = $translate.instant('KORREKTURZAHLUNG_DIALOG_FINISH');

        // Erklaerungen, nur fuer Institutiosnzahlungen relevant
        // tslint:disable-next-line:early-exit
        if (isInstitutionszahlung) {
            this.radioYesCasesInfo = $translate.instant('KORREKTURZAHLUNG_YES_CASE_INFO');
            this.radioYesCases = [$translate.instant('KORREKTURZAHLUNG_YES_CASE_1')];
            this.radioNoCasesInfo = $translate.instant('KORREKTURZAHLUNG_NO_CASE_INFO');
            this.radioNoCases = [
                $translate.instant('KORREKTURZAHLUNG_NO_CASE_1'),
                $translate.instant('KORREKTURZAHLUNG_NO_CASE_2'),
                $translate.instant('KORREKTURZAHLUNG_NO_CASE_3')];
            this.institutionHint = $translate.instant('KORREKTURZAHLUNG_INSTITUTION_HINT');
            if (institutionName) {
                this.institutionHint += ` ${institutionName}`;
            }
            if (institutionPhone) {
                this.institutionHint += ` (${$translate.instant('TELEFON')} ${institutionPhone})`;
            }
        }
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
