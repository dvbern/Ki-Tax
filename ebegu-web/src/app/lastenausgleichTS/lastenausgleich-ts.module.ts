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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {LastenausgleichTsKommentarComponent} from './lastenausgleich-ts-kommentar/lastenausgleich-ts-kommentar.component';
import {LastenausgleichTsRoutingModule} from './lastenausgleich-ts-routing/lastenausgleich-ts-routing.module';
import {LastenausgleichTsSideNavComponent} from './lastenausgleich-ts-side-nav/lastenausgleich-ts-side-nav.component';
import {LastenausgleichTsToolbarComponent} from './lastenausgleich-ts-toolbar/lastenausgleich-ts-toolbar.component';
import {LastenausgleichTSComponent} from './lastenausgleich-ts/lastenausgleich-ts.component';

@NgModule({
    declarations: [
        LastenausgleichTSComponent,
        LastenausgleichTsSideNavComponent,
        LastenausgleichTsKommentarComponent,
        LastenausgleichTsToolbarComponent
    ],
    imports: [
        CommonModule,
        LastenausgleichTsRoutingModule
    ]
})
export class LastenausgleichTSModule {
}
