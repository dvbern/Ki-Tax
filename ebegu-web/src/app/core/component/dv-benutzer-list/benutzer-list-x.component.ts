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
import {TSBenutzer} from '../../../../models/TSBenutzer';

@Component({
    selector: 'dv-benutzer-list-x',
    templateUrl: './benutzer-list-x.component.html',
    styleUrls: ['./benutzer-list-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerListXComponent implements OnInit {

    @Input()
    public tableTitle: string;

    @Input()
    public tableId: string;

    @Input()
    public totalResultCount: number;

    @Input()
    public pendenz: boolean;

    @Output()
    public readonly filterChange: EventEmitter<{tableState: any}> = new EventEmitter<{tableState: any}>();

    @Output()
    public readonly edit: EventEmitter<{user: TSBenutzer}> = new EventEmitter<{user: TSBenutzer}>();

    public constructor() {
    }

    public ngOnInit(): void {
    }

}
