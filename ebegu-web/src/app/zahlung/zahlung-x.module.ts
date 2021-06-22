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

import {CurrencyPipe} from '@angular/common';
import {NgModule} from '@angular/core';
import {NgGesuchModule} from '../../gesuch/ng-gesuch.module';
import {CoreModule} from '../core/core.module';
import {SharedModule} from '../shared/shared.module';
import {ZahlungRoutingModule} from './zahlung-routing/zahlung-routing.module';
import {ZahlungsauftragViewXComponent} from './zahlungsauftrag-view-x/zahlungsauftrag-view-x.component';
import { ZahlungviewXComponent } from './zahlungview-x/zahlungview-x.component';

@NgModule({
    imports: [SharedModule, NgGesuchModule, CoreModule, ZahlungRoutingModule],
    declarations: [ZahlungsauftragViewXComponent, ZahlungviewXComponent],
    providers: [CurrencyPipe]
})
export class ZahlungXModule {

}
