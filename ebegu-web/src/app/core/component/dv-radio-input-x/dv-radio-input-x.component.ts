/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';

@Component({
    selector: 'dv-radio-input-x',
    templateUrl: './dv-radio-input-x.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    styleUrls: ['./dv-radio-input-x.component.less']
})
export class DvRadioInputXComponent implements OnInit {

    @Input()
    public label: string;

    @Input()
    public readonly: boolean = false;

    @Input()
    public model: boolean;

    @Input()
    public dvBisherValue: boolean;

    @Output()
    public readonly modelChange: EventEmitter<boolean> = new EventEmitter<boolean>();

    @ViewChild(NgForm)
    public readonly form: NgForm;

    // unique name for this radio
    public uniqueName: string;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager
    ) {
    }

    public ngOnInit(): void {
        // tslint:disable-next-line:no-magic-numbers
        this.uniqueName = `radio_${Math.round(Math.random() * 10000)}`;
    }

    public change($event: MatRadioChange): void {
        this.modelChange.emit($event.value);
    }

    public showBisher(): boolean {
        return isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status);
    }
}
