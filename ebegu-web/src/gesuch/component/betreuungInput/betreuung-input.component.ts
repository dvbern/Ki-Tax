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

import {Component, Input, OnInit} from '@angular/core';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSBetreuungspensumContainer from '../../../models/TSBetreuungspensumContainer';

const LOG = LogFactory.createLog('BetreuungInputComponent');

@Component({
    selector: 'dv-betreuung-input',
    templateUrl: './betreuungInput.template.html',
    styleUrls: ['./betreuungInput.less'],
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

    public label: string = '';
    public switchOptions: Array<string> = [];
    public multiplier: number = 1;

    public constructor() {
    }

    public ngOnInit(): void {
        this.LOG.debug(this.betreuungsangebottyp);

        this.setAngebotDependingVariables();
        this.pensum.betreuungspensumJA.pensum = this.initialCalculation();
    }

    public setAngebotDependingVariables(): void {
        switch (this.betreuungsangebottyp) {
            case TSBetreuungsangebotTyp.KITA:
                this.switchOptions = ['%', 'Tage'];
                this.multiplier = this.MULTIPLIER_KITA;
                break;
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                this.switchOptions = ['%', 'Stunden'];
                this.multiplier = this.MULTIPLIER_TAGESFAMILIEN;
                break;
            default:
                LOG.error('could not set Switchoptions');
        }
    }

    public toggle(newValue: any): void {
        this.pensum.betreuungspensumJA.doNotUsePercentage = newValue;
        this.updateLabel();
    }

    public updateLabel(): void {
        if (this.calculate() === 'NaN') {
            this.label = '';
        } else {
            const lbl: string = this.pensum.betreuungspensumJA.doNotUsePercentage ? '%' : ` ${this.switchOptions[1]} pro Monat`;
            this.label = `oder ${this.calculate()}${lbl}`;
        }
    }

    private calculate(): string {
        const pensum = this.pensum.betreuungspensumJA.pensum;
        return this.pensum.betreuungspensumJA.doNotUsePercentage
            ? (pensum / this.multiplier).toFixed(2)
            : (pensum * this.multiplier).toFixed(2);
    }

    private initialCalculation(): number {
        const pensum = this.pensum.betreuungspensumJA.pensum;
        return this.pensum.betreuungspensumJA.doNotUsePercentage
            ? pensum * this.multiplier
            : pensum;
    }
}
