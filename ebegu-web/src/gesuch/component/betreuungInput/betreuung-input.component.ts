/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import TSBetreuungspensumContainer from '../../../models/TSBetreuungspensumContainer';
import ITranslateService = angular.translate.ITranslateService;

export class BetreuungInputComponentConfig implements IComponentOptions {
    public template = require('./betreuung-input.component.html');
    public bindings = {
        pensumContainer: '<',
        isDisabled: '<',
        id: '@inputId',
        betreuungsangebotTyp: '<',
    };
    public controller = BetreuungInputComponent;
    public controllerAs = 'vm';
}

export class BetreuungInputComponent implements IController {

    public static $inject = ['$translate'];

    private readonly LOG: Log = LogFactory.createLog(BetreuungInputComponent.name);

    // 100% = 20 days => 1% = 0.2 days
    private readonly MULTIPLIER_KITA = 0.2;
    // 100% = 220 hours => 1% = 2.2 hours
    private readonly MULTIPLIER_TAGESFAMILIEN = 2.2;

    public pensumContainer: TSBetreuungspensumContainer;
    public isDisabled: boolean = false;
    public id: string;
    public betreuungsangebotTyp: TSBetreuungsangebotTyp;

    public label: string = '';
    public switchOptions: TSPensumUnits[] = [];
    private multiplier: number = 1;

    private pensumValue: number;

    public constructor(private readonly translate: ITranslateService) {
    }

    public $onInit(): void {
        this.LOG.debug(this.betreuungsangebotTyp);

        this.setAngebotDependingVariables();
        this.parseToPensumUnit();
        this.toggle();
    }

    public setAngebotDependingVariables(): void {
        switch (this.betreuungsangebotTyp) {
            case TSBetreuungsangebotTyp.KITA:
                this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.DAYS];
                this.multiplier = this.MULTIPLIER_KITA;
                return;
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.HOURS];
                this.multiplier = this.MULTIPLIER_TAGESFAMILIEN;
                return;
            default:
                // FIXME das wird aufgerufen mit Typ TAGI (Timon Becker)
                throw new Error(`Not implemented for Angebot ${this.betreuungsangebotTyp}`);
        }
    }

    public toggle(): void {
        this.refreshContent();
    }

    private refreshContent(): void {
        this.parseToPercentage();
        this.updateLabel();
    }

    public updateLabel(): void {
        const calculatedResult = this.calculateValueForAdditionalLabel();
        if (calculatedResult === 'NaN') {
            this.label = '';
            return;
        }
        const lbl = this.pensumContainer.betreuungspensumJA.unitForDisplay === this.switchOptions[0]
            ? ` ${this.translate.instant(this.switchOptions[1])} ${this.translate.instant('PER_MONTH')}`
            : this.translate.instant(this.switchOptions[0]);

        this.label = `${this.translate.instant('OR')} ${calculatedResult}${lbl}`;
    }

    private calculateValueForAdditionalLabel(): string {
        return this.pensumContainer.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
            ? (this.pensumValue * this.multiplier).toFixed(2)
            : (this.pensumValue / this.multiplier).toFixed(2);
    }

    private parseToPensumUnit(): void {
        this.pensumValue = this.pensumContainer.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
            ? this.pensumContainer.betreuungspensumJA.pensum
            : this.pensumContainer.betreuungspensumJA.pensum * this.multiplier;
    }

    private parseToPercentage(): void {
        this.pensumContainer.betreuungspensumJA.pensum =
            this.pensumContainer.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
                ? this.pensumValue
                : this.pensumValue / this.multiplier;
    }
}
