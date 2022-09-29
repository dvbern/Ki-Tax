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

import {state, style, trigger} from '@angular/animations';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    HostBinding,
    HostListener,
    Input,
    OnChanges,
    Output,
    SimpleChanges
} from '@angular/core';

/**
 * This switch will display 2 boxes with the 2 given values.
 */
@Component({
    selector: 'dv-switch',
    templateUrl: './dv-switch.component.html',
    styleUrls: ['./dv-switch.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: [
        trigger('switchValue', [
            // ...
            state('0', style({
                'margin-left': '0'
            })),
            state('1', style({
                'margin-left': '50%'
            }))
        ])
    ]
})
export class DvSwitchComponent<T> implements OnChanges {

    // It is allowed to set any values as switchOption. switchValue will then have the select (<any>) option
    @Input() public switchValue: T;
    @Input() public readonly switchOptionLeft: T;
    @Input() public readonly switchOptionRight: T;
    // labels: if empty, the switch options are used
    @Input() public readonly switchOptionLabelLeft: string;
    @Input() public readonly switchOptionLabelRight: string;

    @HostBinding('class.disabled')
    @Input() public disabled: boolean = false;

    @Output()
    public readonly switchValueChange: EventEmitter<T> = new EventEmitter<T>();

    @HostBinding('attr.tabindex')
    public tabindex: number;

    @HostListener('keydown.ArrowRight', ['$event'])
    @HostListener('keydown.ArrowLeft', ['$event'])
    public handleKeyboardEvent(event: KeyboardEvent): void {
        if (this.disabled) {
            return;
        }

        if (event.key === 'ArrowRight' && this.switchValue !== this.switchOptionRight) {
            this.emitAndSetValue(this.switchOptionRight);

            return;
        }

        if (event.key === 'ArrowLeft' && this.switchValue !== this.switchOptionLeft) {
            this.emitAndSetValue(this.switchOptionLeft);
        }
    }

    @HostListener('keydown.space', [])
    @HostListener('click', [])
    public toggle(): void {
        if (this.disabled) {
            return;
        }

        const value = this.switchValue === this.switchOptionLeft ? this.switchOptionRight : this.switchOptionLeft;

        this.emitAndSetValue(value);
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.disabled) {
            this.tabindex = changes.disabled.currentValue ? -1 : 0;
        }
    }

    private emitAndSetValue(value: T): void {
        this.switchValue = value;
        this.switchValueChange.emit(value);
    }

    public getSwitchLabelLeft(): string {
        if (!!this.switchOptionLabelLeft) {
            return this.switchOptionLabelLeft;
        }
        return this.switchOptionLeft.toString();
    }

    public getSwitchLabelRight(): string {
        if (!!this.switchOptionLabelRight) {
            return this.switchOptionLabelRight;
        }
        return this.switchOptionRight.toString();
    }

    public getActiveSwitchLabel(switchValue: T): string {
        if (switchValue === this.switchOptionLeft) {
            return this.getSwitchLabelLeft();
        }
        return this.getSwitchLabelRight();
    }
}
