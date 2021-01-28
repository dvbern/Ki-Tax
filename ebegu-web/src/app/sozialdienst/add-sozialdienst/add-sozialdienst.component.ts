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
import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService} from '@uirouter/core';
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';

@Component({
    selector: 'dv-add-sozialdienst',
    templateUrl: './add-sozialdienst.component.html',
    styleUrls: ['./add-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddSozialdienstComponent implements OnInit {

    public sozialdienst: TSSozialdienst = undefined;
    public adminEmail: string = undefined;

    public constructor(private readonly $state: StateService) {
    }

    public ngOnInit(): void {
    }

    public socialdienstEinladen(): void {

    }

    public cancel(): void {
        this.$state.go('sozialdienst.list');
    }
}
