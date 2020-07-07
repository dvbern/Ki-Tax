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
import {MULTIPLIER_KITA, MULTIPLIER_TAGESFAMILIEN} from '../../../app/core/constants/CONSTANTS';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import {TSBetreuungspensumContainer} from '../../../models/TSBetreuungspensumContainer';
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
    private _betreuungsangebotTyp: TSBetreuungsangebotTyp;

    public pensumContainer: TSBetreuungspensumContainer;
    public isDisabled: boolean = false;
    public id: string;
    public step: number = 0.01;

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

    public get betreuungsangebotTyp(): TSBetreuungsangebotTyp {
        return this._betreuungsangebotTyp;
    }

    public set betreuungsangebotTyp(value: TSBetreuungsangebotTyp) {
        this._betreuungsangebotTyp = value;
        this.setAngebotDependingVariables();
        this.parseToPensumUnit();
        this.toggle();
    }

    public getPlaceholder(): string {
        if (this.pensumContainer
                && this.pensumContainer.betreuungspensumJA.unitForDisplay === this.switchOptions[1]) {
            if (this.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN) {
                return this.translate.instant('STUNDEN_PLACEHOLDER');
            }
            return this.translate.instant('TAGE_PLACEHOLDER');
        }
        return this.translate.instant('PERCENTAGE_PLACEHOLDER');
    }

    public setAngebotDependingVariables(): void {
        if (this.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN) {
            this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.HOURS];
            this.multiplier = MULTIPLIER_TAGESFAMILIEN;
        } else {
            this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.DAYS];
            this.multiplier = MULTIPLIER_KITA;
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
        this.pensumValue =
            (this.pensumContainer && this.pensumContainer.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE)
                ? this.pensumContainer.betreuungspensumJA.pensum
                : Number((this.pensumContainer.betreuungspensumJA.pensum * this.multiplier).toFixed(2));
    }

    private parseToPercentage(): void {
        if (!(this.pensumContainer && this.pensumContainer.betreuungspensumJA)) {
            return;
        }
        this.pensumContainer.betreuungspensumJA.pensum =
            this.pensumContainer.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
                ? this.pensumValue
                : this.pensumValue / this.multiplier;
    }
}
