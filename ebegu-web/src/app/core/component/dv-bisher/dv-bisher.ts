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

import {IComponentOptions, IController} from 'angular';
import * as moment from 'moment';
import {Moment} from 'moment';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSAbstractMutableEntity} from '../../../../models/TSAbstractMutableEntity';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import ITranslateService = angular.translate.ITranslateService;

/**
 * Verwendung:
 * - gs: Wert, den der Gesuchsteller eingegeben hat
 * - ja: Wert, den das Jugendamt korrigiert
 *
 * Attribute fuer Eingaben-Blocks (zusammengehoerende Felder):
 * - specificBisherText: Falls einfach der GS-Wert als Bisher angezeigt werden soll. Z.B. Checkbox "Fachstelle": Wir
 *      wollen als Bisher-Text nicht "Ja", sondern "Fachstelle X, mit 50%, vom 01.01.2015 bis 31.12.2015"
 * - blockExisted: Gibt an, ob der Block ueberhaupt vom GS ausgefuellt wurde. Falls nein, muss *jede* Eingabe des JA
 *      als Korrektur angezeigt werden
 * - showIfBisherNone: Zeigt, ob "Keine Eingabe" angezeigt werden soll, wenn es keinen Bisher-Wert
 *      gibt. Normalerweise wollen wir das. Ausnahme sind Blocks, wo wir das "Keine Eingabe" *pro Block* anzeigen wollen
 *      und nicht unter jedem Feld.
 */
export class DvBisherComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        gs: '<',
        ja: '<',
        specificBisherText: '<',
        blockExisted: '<',
        showIfBisherNone: '<',
        showSpecificBisherTextIfBisherNone: '<'
    };
    public template = require('./dv-bisher.html');
    public controller = DvBisher;
    public controllerAs = 'vm';
}

const defaultDateFormat = 'DD.MM.YYYY';

export class DvBisher implements IController {
    public static $inject = ['GesuchModelManager', '$translate'];

    public gs: any;
    public ja: any;
    // sollen die korrekturen des jugendamts angezeigt werden wenn im GS container kein wert ist
    public showIfBisherNone: boolean;
    // Soll ein spezifischer Text angezeigt werden, wenn das JA einen Eintrag erfasst hat?
    public showSpecificBisherTextIfBisherNone: boolean;
    public specificBisherText: string;
    public bisherText: Array<string>;
    public blockExisted: boolean;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService
    ) {}

    public $onInit(): void {
        if (this.showIfBisherNone === undefined) {
            // wenn nicht von aussen gesetzt auf true
            this.showIfBisherNone = true;
        }
        if (
            EbeguUtil.isNullOrUndefined(this.showSpecificBisherTextIfBisherNone)
        ) {
            this.showSpecificBisherTextIfBisherNone = false;
        }
    }

    public getBisher(): Array<string> {
        // noinspection IfStatementWithTooManyBranchesJS
        if (this.specificBisherText) {
            this.bisherText = this.specificBisherText
                ? this.specificBisherText.split('\n')
                : undefined;
            // War es eine Loeschung, oder ein Hinzufuegen?
            if (this.hasBisher() || this.showSpecificBisherTextIfBisherNone) {
                return this.bisherText; // neue eingabe als ein einzelner block
            }
            return [this.$translate.instant('LABEL_KEINE_ANGABE')]; // vorher war keine angabe da
        }
        if (this.gs instanceof moment) {
            return [
                DateUtil.momentToLocalDateFormat(
                    this.gs as Moment,
                    defaultDateFormat
                )
            ];
        }
        if (this.gs === true) {
            return [this.$translate.instant('LABEL_JA')];
        }
        if (this.gs === false) {
            return [this.$translate.instant('LABEL_NEIN')];
        }
        if (!this.hasBisher()) {
            return [this.$translate.instant('LABEL_KEINE_ANGABE')];
        }
        return [this.$translate.instant(this.gs)];
    }

    public hasBisher(): boolean {
        return !this.isEmpty(this.gs);
    }

    public showBisher(): boolean {
        return (
            (this.showIfBisherNone || this.blockExisted || this.hasBisher()) &&
            this.isKorrekturModusJugendamtOrFreigegeben()
        );
    }

    private isKorrekturModusJugendamtOrFreigegeben(): boolean {
        return (
            this.gesuchModelManager.getGesuch() &&
            isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status) &&
            TSEingangsart.ONLINE ===
                this.gesuchModelManager.getGesuch().eingangsart
        );
    }

    public equals(gs: any, ja: any): boolean {
        if (gs instanceof moment) {
            return this.equals(
                DateUtil.momentToLocalDateFormat(
                    gs as Moment,
                    defaultDateFormat
                ),
                DateUtil.momentToLocalDateFormat(ja, defaultDateFormat)
            );
        }
        if (Array.isArray(gs)) {
            return JSON.stringify(gs) === JSON.stringify(ja);
        }
        if (gs instanceof TSAbstractMutableEntity) {
            return (
                (EbeguUtil.isNotNullOrUndefined(gs) &&
                    EbeguUtil.isNotNullOrUndefined(ja)) ||
                (EbeguUtil.isNullOrUndefined(gs) &&
                    EbeguUtil.isNullOrUndefined(ja))
            );
        }
        return gs === ja || (this.isEmpty(gs) && this.isEmpty(ja)); // either they are equal or both are a form of empty
    }

    private isEmpty(val: any): boolean {
        return val === undefined || val === null || val === '';
    }
}
