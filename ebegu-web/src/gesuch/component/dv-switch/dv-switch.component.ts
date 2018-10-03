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

import {Component, EventEmitter, HostBinding, HostListener, Input, OnChanges, OnInit, Output} from '@angular/core';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import {state, style, trigger} from '@angular/animations';

const LOG = LogFactory.createLog('DvSwitchComponent');

@Component({
    selector: 'dv-switch',
    templateUrl: './dv-switch.template.html',
    styleUrls: ['./dv-switch.less'],
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
export class DvSwitchComponent implements OnInit, OnChanges {

    private readonly LOG: Log = LogFactory.createLog(DvSwitchComponent.name);

    // private listenToEvents: boolean = false;

    @Input() switchValue: boolean = false;
    @Input() switchOptions: Array<string> = [];
    @HostBinding('class.disabled')
    @Input() disabled: boolean = false;

    @Output()
    switchChange: EventEmitter<boolean> = new EventEmitter<boolean>();

    @HostBinding('attr.tabindex')
    tabindex: number;

    constructor() {
    }

    @HostListener('keydown.ArrowRight', ['$event'])
    @HostListener('keydown.ArrowLeft', ['$event'])
    public handleKeyboardEvent(event: KeyboardEvent): void {
        if (this.switchValue !== (event.key === 'ArrowRight') && !this.disabled) {
            this.switchValue = event.key === 'ArrowRight';
            this.ngOnChanges();
        }
    }

    @HostListener('keydown.space', ['$event'])
    @HostListener('click', ['$event'])
    public toggle(): void {
        if (!this.disabled) {
            this.switchValue = !this.switchValue;
            this.ngOnChanges();
        }
    }

    ngOnInit(): void {
        this.switchChange.emit(this.switchValue);
        this.tabindex = this.disabled ? -1 : 0;
    }

    ngOnChanges(): void {
        this.switchChange.emit(this.switchValue);
    }
}
