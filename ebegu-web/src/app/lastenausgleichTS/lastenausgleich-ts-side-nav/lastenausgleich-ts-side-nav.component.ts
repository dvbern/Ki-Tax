/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'dv-lastenausgleich-ts-side-nav',
  templateUrl: './lastenausgleich-ts-side-nav.component.html',
  styleUrls: ['./lastenausgleich-ts-side-nav.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsSideNavComponent implements OnInit {

    // TODO: replace with real values
    public readonly lastenausgleichTsId = '1234';
    public readonly status = 'Status';

    public constructor() { }

    public ngOnInit(): void {
  }

}
