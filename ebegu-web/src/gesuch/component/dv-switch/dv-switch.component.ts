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

import {state, style, trigger} from '@angular/animations';
import {ChangeDetectionStrategy, Component, EventEmitter, HostBinding, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';

/**
 * This switch will display 2 boxes with the 2 given values.
 */
@Component({
    selector: 'dv-switch',
    templateUrl: './dv-switch.template.html',
    styleUrls: ['./dv-switch.less'],
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
        ]),
    ]
})
export class DvSwitchComponent<T> implements OnInit, OnChanges {

    // It is allowed to set any values as switchOption. switchValue will then have the select (<any>) option
    @Input() private switchValue: T;
    @Input() public readonly switchOptionLeft: T;
    @Input() public readonly switchOptionRight: T;

    @HostBinding('class.disabled')
    @Input() public disabled: boolean = false;

    @Output()
    public readonly switchValueChange: EventEmitter<T> = new EventEmitter<T>();

    @HostBinding('attr.tabindex')
    public tabindex: number;

    public constructor() {
    }

    @HostListener('keydown.ArrowRight', ['$event'])
    @HostListener('keydown.ArrowLeft', ['$event'])
    public handleKeyboardEvent(event: KeyboardEvent): void {
        if (this.disabled) {
            return;
        }
        if (event.key === 'ArrowRight' && this.switchValue !== this.switchOptionRight) {
            this.switchValue = this.switchOptionRight;

        } else if (event.key === 'ArrowLeft' && this.switchValue !== this.switchOptionLeft) {
            this.switchValue = this.switchOptionLeft;
        }

        this.emitValue();
    }

    @HostListener('keydown.space', ['$event'])
    @HostListener('click', ['$event'])
    public toggle(): void {
        if (this.disabled) {
            return;
        }
        this.switchValue = (this.switchValue === this.switchOptionLeft
            ? this.switchOptionRight
            : this.switchOptionLeft);

        this.emitValue();
    }

    public ngOnInit(): void {
        this.tabindex = this.disabled ? -1 : 0;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes && changes.switchValue) {
            this.switchValueChange.emit(this.switchValue);
        }
    }

    private emitValue(): void {
        this.switchValueChange.emit(this.switchValue);
    }
}
