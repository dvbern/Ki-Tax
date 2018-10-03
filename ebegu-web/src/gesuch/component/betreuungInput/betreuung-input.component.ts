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

import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import TSBetreuungspensumContainer from '../../../models/TSBetreuungspensumContainer';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';

const LOG = LogFactory.createLog('BetreuungInputComponent');

@Component({
    selector: 'dv-betreuung-input',
    templateUrl: './betreuungInput.template.html',
    styleUrls: ['./betreuungInput.less'],
})
export class BetreuungInputComponent implements OnInit, OnChanges {

    private readonly LOG: Log = LogFactory.createLog(BetreuungInputComponent.name);

    @Input()
    pensum: TSBetreuungspensumContainer;

    @Input()
    disabled: boolean = false;

    @Input()
    id: string;

    @Input()
    betreuungsangebottyp: TSBetreuungsangebotTyp;


    label: string = '';
    switchOptions: Array<string> = [];
    calcNumber: number = 1;

    constructor() {
    }

    ngOnInit(): void {
        console.log(this.betreuungsangebottyp);

        this.setVariables();
        this.pensum.betreuungspensumJA.pensum = this.initialCalculation();

        // this.loadObjects();  --> it is called in ngOnChanges anyway. otherwise it gets called twice
    }

    ngOnChanges(): void {
        this.setVariables();
    }

    public setVariables() {
        this.switchOptions = [];
        this.switchOptions.push('%');

        switch (this.betreuungsangebottyp) {
            case TSBetreuungsangebotTyp.KITA:
                this.switchOptions.push('Tage');
                this.calcNumber = 5;
                break;
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                this.switchOptions.push('Stunden');
                this.calcNumber = 2.2;
                break;
            default:
                LOG.error('could not set Switchoptions');

        }
    }

    public toggle(event: any): void {
        this.pensum.betreuungspensumJA.doNotUsePercentage = event;
        this.updateLabel();
    }

    public updateLabel(): void {
        if (this.calculate() !== 'NaN') {
            const lbl: string = this.pensum.betreuungspensumJA.doNotUsePercentage ? '%' : ` ${this.switchOptions[1]} pro Monat`;
            this.label = `oder ${this.calculate()}${lbl}`;
        } else {
            this.label = '';
        }
    }

    private calculate(): string {
        const pensum = this.pensum.betreuungspensumJA.pensum;
        switch (this.betreuungsangebottyp) {
            case TSBetreuungsangebotTyp.KITA:
                return this.pensum.betreuungspensumJA.doNotUsePercentage ? (pensum * this.calcNumber).toFixed(2) : (pensum / this.calcNumber).toFixed(2);
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                return this.pensum.betreuungspensumJA.doNotUsePercentage ? (pensum / this.calcNumber).toFixed(2) : (pensum * this.calcNumber).toFixed(2);
            default:
                LOG.error('could not calc');
                return 'NaN';
        }
    }

    private initialCalculation(): number {
        const pensum = this.pensum.betreuungspensumJA.pensum;
        switch (this.betreuungsangebottyp) {
            case TSBetreuungsangebotTyp.KITA:
                return this.pensum.betreuungspensumJA.doNotUsePercentage ? pensum / this.calcNumber : pensum;
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                return this.pensum.betreuungspensumJA.doNotUsePercentage ? pensum * this.calcNumber : pensum;
            default:
                LOG.error('could not calc');
                return 0;
        }
    }
}
