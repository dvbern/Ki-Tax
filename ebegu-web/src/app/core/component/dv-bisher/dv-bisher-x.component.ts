/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

/**
 * Verwendung:
 * - deklaration: erster Wert. Z.B. Gesuchsteller, Gemeinde, etc.
 * - korrektur: korrigierter Wert. Z.B. durch die Gemeinde, den Kanton, etc.
 *
 * Attribute fuer Eingaben-Blocks (zusammengehoerende Felder):
 * - specificBisherText: Falls einfach der GS-Wert als Bisher angezeigt werden soll. Z.B. Checkbox "Fachstelle": Wir
 *      wollen als Bisher-Text nicht "Ja", sondern "Fachstelle X, mit 50%, vom 01.01.2015 bis 31.12.2015"
 * - specificBisherLabel: soll ein spezielles Label gezeigt werden? Default ist "ursprünglich:". Z.B. "ursprünglich: 10"
 * - blockExisted: Gibt an, ob der Block ueberhaupt vom GS ausgefuellt wurde. Falls nein, muss *jede* Eingabe des JA
 *      als Korrektur angezeigt werden
 * - showIfBisherNone: Zeigt, ob "Keine Eingabe" angezeigt werden soll, wenn es keinen Bisher-Wert
 *      gibt. Normalerweise wollen wir das. Ausnahme sind Blocks, wo wir das "Keine Eingabe" *pro Block* anzeigen wollen
 *      und nicht unter jedem Feld.
 *
 * Weitere Attribute:
 * - showBisher: Wenn das Flag auf false gesetzt wird, wird dv-bisher nie angezeigt
 */

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Moment} from 'moment';
import {TSAbstractMutableEntity} from '../../../../models/TSAbstractMutableEntity';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {CONSTANTS} from '../../constants/CONSTANTS';

@Component({
    selector: 'dv-bisher-x',
    templateUrl: './dv-bisher-x.component.html',
    styleUrls: ['./dv-bisher-x.component.less'],
    changeDetection: ChangeDetectionStrategy.Default
})
export class DvBisherXComponent implements OnInit {

    /**
     * erster Wert. Z.B. Gesuchsteller, Gemeinde, etc.
     */
    @Input() public deklaration: any;
    /**
     * korrigierter Wert. Z.B. durch die Gemeinde, den Kanton, etc.
     */
    @Input() public korrektur: any;
    /**
     * Falls einfach der Deklaration als Bisher angezeigt werden soll. Z.B. Checkbox "Fachstelle": Wir
     *      wollen als Bisher-Text nicht "Ja", sondern "Fachstelle X, mit 50%, vom 01.01.2015 bis 31.12.2015"
     */
    @Input() public specificBisherText: string;
    /**
     * sollen die Korrekturen angezeigt werden wenn im Deklaration container kein wert ist
     */
    @Input() public showIfBisherNone = true;
    /**
     * Soll der bisherText angezeigt werden, falls keine Angabe in der Deklaration gemacht wurde?
     */
    @Input() public showSpecificBisherTextIfBisherNone = false;
    /**
     * soll ein spezielles Label gezeigt werden? Default ist "ursprünglich:". Z.B. "ursprünglich: 10"
     */
    @Input() public specificBisherLabel: string;
    /**
     * Wenn das Flag auf false gesetzt wird, wird dv-bisher nie angezeigt
     */
    @Input() public showBisher: boolean = true;

    public bisherText: Array<string>;
    public blockExisted: boolean;

    public constructor(
        private readonly $translate: TranslateService
    ) {
    }

    public ngOnInit(): void {
    }

    public getBisher(): Array<string> {
        // noinspection IfStatementWithTooManyBranchesJS
        if (this.specificBisherText) {
            this.bisherText = this.specificBisherText ? this.specificBisherText.split('\n') : undefined;
            // War es eine Loeschung, oder ein Hinzufuegen?
            if (this.hasBisher() || this.showSpecificBisherTextIfBisherNone) {
                return this.bisherText; // neue eingabe als ein einzelner block
            }
            return [this.$translate.instant('LABEL_KEINE_ANGABE')];  // vorher war keine angabe da
        }
        if (typeof(this.deklaration ) === 'number') {
            return [this.deklaration.toString()];
        }
        if (this.deklaration instanceof moment) {
            return [DateUtil.momentToLocalDateFormat(this.deklaration as Moment, CONSTANTS.DATE_FORMAT)];
        }
        if (this.deklaration === true) {
            return [this.$translate.instant('LABEL_JA')];
        }
        if (this.deklaration === false) {
            return [this.$translate.instant('LABEL_NEIN')];
        }
        if (!this.hasBisher()) {
            return [this.$translate.instant('LABEL_KEINE_ANGABE')];
        }
        return [this.$translate.instant(this.deklaration)];
    }

    public hasBisher(): boolean {
        return !this.isEmpty(this.deklaration);
    }

    public equals(deklaration: any, korrektur: any): boolean {
        if (deklaration instanceof moment) {
            return this.equals(DateUtil.momentToLocalDateFormat(deklaration as Moment, CONSTANTS.DATE_FORMAT),
                DateUtil.momentToLocalDateFormat(korrektur, CONSTANTS.DATE_FORMAT));
        }
        if (Array.isArray(deklaration)) {
            return JSON.stringify(deklaration) === JSON.stringify(korrektur);
        }
        if (deklaration instanceof TSAbstractMutableEntity) {
            return (EbeguUtil.isNotNullOrUndefined(deklaration) && EbeguUtil.isNotNullOrUndefined(korrektur))
                || (EbeguUtil.isNullOrUndefined(deklaration) && EbeguUtil.isNullOrUndefined(korrektur));
        }
        // either they are equal
        return deklaration === korrektur
            // or both are a form of empty
            || (this.isEmpty(deklaration) && this.isEmpty(korrektur))
            // or one is a string and the other one of another type with the same string representation
            || deklaration.toString() === korrektur.toString();
    }

    private isEmpty(val: any): boolean {
        return val === undefined || val === null || val === '';
    }

    public getBisherLabel(): string {
        if (this.specificBisherLabel) {
            return this.specificBisherLabel;
        }
        return this.$translate.instant('DV_BISHER_TEXT');
    }
}
