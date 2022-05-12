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
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import {TSBetreuungspensumContainer} from '../../../models/TSBetreuungspensumContainer';
import {GesuchModelManager} from '../../service/gesuchModelManager';
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

    public static $inject = ['$translate', 'GesuchModelManager', 'EinstellungRS'];

    private readonly LOG: Log = LogFactory.createLog(BetreuungInputComponent.name);
    private _betreuungsangebotTyp: TSBetreuungsangebotTyp;

    public pensumContainer: TSBetreuungspensumContainer;
    public isDisabled: boolean = false;
    public id: string;
    public step: number = 0.01;

    public label: string = '';
    public switchOptions: TSPensumUnits[] = [];
    private multiplier: number = 1;
    private multiplierKita: number;
    private multiplierTFO: number;

    private pensumValue: number;

    public constructor(private readonly translate: ITranslateService,
                       public readonly gesuchModelManager: GesuchModelManager,
                       public readonly einstellungRS: EinstellungRS
    ) {
    }

    public $onInit(): void {
        this.LOG.debug(this.betreuungsangebotTyp);
        this.loadKonfigurationen()
            .then(() => {
                this.setAngebotDependingVariables();
                this.parseToPensumUnit();
                this.toggle();
            });
    }

    public loadKonfigurationen(): Promise<void> {
        const p1 = this.einstellungRS.findEinstellung(
            TSEinstellungKey.OEFFNUNGSTAGE_KITA,
            this.gesuchModelManager.getGemeinde().id,
            this.gesuchModelManager.getGesuchsperiode().id
        );
        const p2 = this.einstellungRS.findEinstellung(
            TSEinstellungKey.OEFFNUNGSTAGE_TFO,
            this.gesuchModelManager.getGemeinde().id,
            this.gesuchModelManager.getGesuchsperiode().id
        );
        const p3 = this.einstellungRS.findEinstellung(
            TSEinstellungKey.OEFFNUNGSSTUNDEN_TFO,
            this.gesuchModelManager.getGemeinde().id,
            this.gesuchModelManager.getGesuchsperiode().id
        );
        return Promise.all([p1, p2, p3])
            .then(res => {
                const oeffnungstageKita = parseInt(res[0].value, 10);
                const oeffnungstageTFO = parseInt(res[1].value, 10);
                const oeffnungsstundenTFO = parseInt(res[2].value, 10);
                // Beispiel: 240 Tage Pro Jahr: 240 / 12 = 20 Tage Pro Monat. 100% = 20 days => 1% = 0.2 tage
                this.multiplierKita = oeffnungstageKita / 12 / 100;
                // Beispiel: 240 Tage Pro Jahr, 11 Stunden pro Tag: 240 * 11 / 12 = 220 Stunden Pro Monat.
                // 100% = 220 stunden => 1% = 2.2 stunden
                this.multiplierTFO = oeffnungstageTFO * oeffnungsstundenTFO / 12 / 100;
            });
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
            this.multiplier = this.multiplierTFO;
        } else {
            this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.DAYS];
            this.multiplier = this.multiplierKita;
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
