/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Observable} from 'rxjs';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';

@Component({
    selector: 'dv-edit-gemeinde-stammdaten',
    templateUrl: './edit-gemeinde-stammdaten.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditGemeindeComponentStammdaten implements OnInit {

    @Input() public form: NgForm;
    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() private gemeindeId: string;
    @Input() public keineBeschwerdeAdresse: boolean;

    public constructor(
    ) {
    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
    }
}
