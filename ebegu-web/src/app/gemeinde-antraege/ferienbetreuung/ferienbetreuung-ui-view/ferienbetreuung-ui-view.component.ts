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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';

@Component({
  selector: 'dv-ferienbetreuung-ui-view',
  templateUrl: './ferienbetreuung-ui-view.component.html',
  styleUrls: ['./ferienbetreuung-ui-view.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungUiViewComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
