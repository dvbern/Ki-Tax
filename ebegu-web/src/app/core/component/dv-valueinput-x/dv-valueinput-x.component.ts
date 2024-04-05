/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {EbeguNumberPipe} from '../../../shared/pipe/ebegu-number.pipe';

@Component({
    selector: 'dv-valueinput-x',
    templateUrl: './dv-valueinput-x.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class DvValueinputXComponent {

    private readonly patternBetrag: string = '([0-9]{1,3}\')?([0-9]{3}\'){0,2}([0-9]{1,3})';
    private readonly patternBetragWithDecimals: string =  `${this.patternBetrag  }(\\.[0-9]{1,2})?`;
    private readonly patternBetragNegativ: string = `(-?)${  this.patternBetrag}`;
    private readonly patternBetragNegativWithDecimals: string = `(-?)${  this.patternBetragWithDecimals}`;

    @Input() public inputId: string = 'inputFieldId';
    @Input() public model: any;
    @Input() public name: string = EbeguUtil.generateRandomName(12);
    @Input() public float: boolean = false;
    @Input() public allowNegative: boolean = false;
    @Input() public required: boolean = false;
    @Input() public disabled: boolean = false;
    @Input() public dvOnBlur: (event: any) => void;
    @Input() public neuerWert: number;
    @Input() public vergleichswert: number;
    @Input() public vergleichswertLabel: string;
    @Input() public deklaration: any;
    @Input() public korrektur: any;
    @Input() public showBisher: boolean = false;

    @Output() public readonly modelChange: EventEmitter<any> = new EventEmitter();

    public valueHasChange(event: any): void {
        const sanitizedString = EbeguNumberPipe.sanitizeInputString(event.target.value, this.float);
        // view value muss überschrieben werden. Wenn Benutzer ein Buchstaben ins Input-Feld speichert wird dieser
        // in der sanitizeInputString Methode entfernt, es wird keine Change erkannt und die View wird nicht aktualisiert
        event.target.value = EbeguNumberPipe.formatToNumberString(sanitizedString);
        this.modelChange.emit(sanitizedString);
    }

    public onBlur(event: any): void {
        if (this.dvOnBlur) { // userdefined onBlue event
            this.dvOnBlur(event);
        }
    }

    public getInputValidationPattern(): string {
        if (this.allowNegative) {
            return this.float ? this.patternBetragNegativWithDecimals : this.patternBetragNegativ;
        }

        return this.float ? this.patternBetragWithDecimals : this.patternBetrag;
    }

}
