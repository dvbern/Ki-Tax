/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import TSBetreuungspensumContainer from '../../../models/TSBetreuungspensumContainer';

const LOG = LogFactory.createLog('BetreuungInputComponent');

@Component({
    selector: 'dv-betreuung-input',
    templateUrl: './betreuungInput.template.html',
    styleUrls: ['./betreuungInput.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BetreuungInputComponent implements OnInit {

    private readonly LOG: Log = LogFactory.createLog(BetreuungInputComponent.name);

    // 100% = 20 days => 1% = 0.2 days
    private readonly MULTIPLIER_KITA = 0.2;
    // 100% = 220 hours => 1% = 2.2 hours
    private readonly MULTIPLIER_TAGESFAMILIEN = 2.2;

    @Input()
    public pensum: TSBetreuungspensumContainer;

    @Input()
    public disabled: boolean = false;

    @Input()
    public id: string;

    @Input()
    public betreuungsangebottyp: TSBetreuungsangebotTyp;

    private value: number;

    public label: string = '';
    public switchOptions: Array<TSPensumUnits> = [];
    public multiplier: number = 1;

    public constructor(private readonly translate: TranslateService) {
    }

    public ngOnInit(): void {
        this.LOG.debug(this.betreuungsangebottyp);

        this.setAngebotDependingVariables();
        this.parseToPensumUnit();
    }

    public setAngebotDependingVariables(): void {
        switch (this.betreuungsangebottyp) {
            case TSBetreuungsangebotTyp.KITA:
                this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.DAYS];
                this.multiplier = this.MULTIPLIER_KITA;
                break;
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                this.switchOptions = [TSPensumUnits.PERCENTAGE, TSPensumUnits.HOURS];
                this.multiplier = this.MULTIPLIER_TAGESFAMILIEN;
                break;
            default:
                LOG.error('could not set Switchoptions');
        }
    }

    public toggle(): void {
        this.parseToPercentage();
        this.updateLabel();
    }

    public updateLabel(): void {
        const calculatedResult = this.calculateValueForAdditionalLabel();
        if (calculatedResult === 'NaN') {
            this.label = '';
            return;
        }
        const lbl: string = this.pensum.betreuungspensumJA.unitForDisplay === this.switchOptions[0]
            ? ` ${this.translate.instant(this.switchOptions[1])} ${this.translate.instant('PER_MONTH')}`
            : this.translate.instant(this.switchOptions[0]);

        this.label = `${this.translate.instant('OR')} ${calculatedResult}${lbl}`;
    }

    private calculateValueForAdditionalLabel(): string {
        return this.pensum.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
            ? (this.value * this.multiplier).toFixed(2)
            : (this.value / this.multiplier).toFixed(2);
    }

    private parseToPensumUnit(): void {
        this.value = this.pensum.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
            ? this.pensum.betreuungspensumJA.pensum
            : this.pensum.betreuungspensumJA.pensum * this.multiplier;
    }

    private parseToPercentage(): void {
        this.pensum.betreuungspensumJA.pensum = this.pensum.betreuungspensumJA.unitForDisplay === TSPensumUnits.PERCENTAGE
            ? this.value
            : this.value / this.multiplier;
    }
}
