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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output
} from '@angular/core';

@Component({
    selector: 'dv-input-label-field',
    templateUrl: './dv-input-label-field.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvInputLabelFieldComponent {

    @Input() public inputId: string = 'inputFieldId';
    @Input() public labelMessageKey: string;
    @Input() public labelMessageKeyArgs: {};
    @Input() public tooltipMessageKey: string;
    @Input() public tooltipMessageKeyArgs: {};
    @Input() public model: any;
    @Input() public inputType: string  = 'text';
    @Input() public inputRequired: boolean = false;

    @Output() public readonly modelChange: EventEmitter<any> = new EventEmitter();

    public valueHasChange(event: any): void {
        this.modelChange.emit(event);
    }

    public getRequiredCssClass(): string {
        if (this.inputRequired) {
            return 'required';
        }

        return '';
    }
}
