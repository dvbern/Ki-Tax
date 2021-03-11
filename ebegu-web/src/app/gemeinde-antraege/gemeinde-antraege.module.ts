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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FerienbetreuungAngebotComponent} from './ferienbetreuung/ferienbetreuung-angebot/ferienbetreuung-angebot.component';
import {FerienbetreuungFreigabeComponent} from './ferienbetreuung/ferienbetreuung-freigabe/ferienbetreuung-freigabe.component';
import {FerienbetreuungKostenEinnahmenComponent} from './ferienbetreuung/ferienbetreuung-kosten-einnahmen/ferienbetreuung-kosten-einnahmen.component';
import {FerienbetreuungNutzungComponent} from './ferienbetreuung/ferienbetreuung-nutzung/ferienbetreuung-nutzung.component';
import {FerienbetreuungStammdatenGemeindeComponent} from './ferienbetreuung/ferienbetreuung-stammdaten-gemeinde/ferienbetreuung-stammdaten-gemeinde.component';
import {FerienbetreuungUploadComponent} from './ferienbetreuung/ferienbetreuung-upload/ferienbetreuung-upload.component';
import {FerienbetreuungVerfuegungComponent} from './ferienbetreuung/ferienbetreuung-verfuegung/ferienbetreuung-verfuegung.component';
import {FerienbetreuungModule} from './ferienbetreuung/ferienbetreuung.module';
import {GemeindeAntraegeRoutingModule} from './gemeinde-antraege-routing/gemeinde-antraege-routing.module';
import {LastenausgleichTSModule} from './lastenausgleich-ts/lastenausgleich-ts.module';

@NgModule({
    declarations: [FerienbetreuungStammdatenGemeindeComponent, FerienbetreuungAngebotComponent, FerienbetreuungNutzungComponent, FerienbetreuungKostenEinnahmenComponent, FerienbetreuungUploadComponent, FerienbetreuungFreigabeComponent, FerienbetreuungVerfuegungComponent],
    imports: [
        CommonModule,
        LastenausgleichTSModule,
        FerienbetreuungModule,
        GemeindeAntraegeRoutingModule,
    ],
    exports: [
        LastenausgleichTSModule,
        FerienbetreuungModule,
    ]
})
export class GemeindeAntraegeModule {
}
