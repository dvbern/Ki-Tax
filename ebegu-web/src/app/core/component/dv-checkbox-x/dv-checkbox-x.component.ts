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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';

@Component({
    selector: 'dv-checkbox-x',
    templateUrl: './dv-checkbox-x.component.html',
    styleUrls: ['./dv-checkbox-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvCheckboxXComponent implements OnInit {

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

    public uniqueName: string;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager
    ) {
    }

    public ngOnInit(): void {
        // eslint-disable-next-line no-magic-numbers
        this.uniqueName = `checkbox_${Math.round(Math.random() * 10000)}`;
    }

    public change(): void {
        this.modelChange.emit(this.model);
    }

    public showBisher(): boolean {
        return this.gesuchModelManager.getGesuch()
            && isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status)
            && (TSEingangsart.ONLINE === this.gesuchModelManager.getGesuch().eingangsart);
    }
}
