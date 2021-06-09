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
import {ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../../shared/shared.module';
import {WizardstepXModule} from '../../wizardstepX/wizardstep-x.module';
import {FerienbetreuungAbschlussComponent} from './ferienbetreuung-abschluss/ferienbetreuung-abschluss.component';
import {FerienbetreuungAngebotComponent} from './ferienbetreuung-angebot/ferienbetreuung-angebot.component';
import {FerienbetreuungFreigabeComponent} from './ferienbetreuung-freigabe/ferienbetreuung-freigabe.component';
import {FerienbetreuungKommantarComponent} from './ferienbetreuung-kommantar/ferienbetreuung-kommantar.component';
import {FerienbetreuungBerechnungComponent} from './ferienbetreuung-kosten-einnahmen/ferienbetreuung-berechnung/ferienbetreuung-berechnung.component';
import {FerienbetreuungKostenEinnahmenComponent} from './ferienbetreuung-kosten-einnahmen/ferienbetreuung-kosten-einnahmen.component';
import {FerienbetreuungNutzungComponent} from './ferienbetreuung-nutzung/ferienbetreuung-nutzung.component';
import {FerienbetreuungRoutingModule} from './ferienbetreuung-routing/ferienbetreuung-routing.module';
import {FerienbetreuungStammdatenGemeindeComponent} from './ferienbetreuung-stammdaten-gemeinde/ferienbetreuung-stammdaten-gemeinde.component';
import {FerienbetreuungUploadComponent} from './ferienbetreuung-upload/ferienbetreuung-upload.component';
import {FerienbetreuungComponent} from './ferienbetreuung/ferienbetreuung.component';
import {FerienbetreuungDokumentService} from './services/ferienbetreuung-dokument.service';
import {FerienbetreuungService} from './services/ferienbetreuung.service';

@NgModule({
    declarations: [
        FerienbetreuungComponent,
        FerienbetreuungKommantarComponent,
        FerienbetreuungStammdatenGemeindeComponent,
        FerienbetreuungAngebotComponent,
        FerienbetreuungNutzungComponent,
        FerienbetreuungKostenEinnahmenComponent,
        FerienbetreuungUploadComponent,
        FerienbetreuungFreigabeComponent,
        FerienbetreuungAbschlussComponent,
        FerienbetreuungBerechnungComponent,
    ],
    imports: [
        CommonModule,
        FerienbetreuungRoutingModule,
        WizardstepXModule,
        SharedModule,
        ReactiveFormsModule,
    ],
    providers: [
        FerienbetreuungService,
        FerienbetreuungDokumentService
    ]
})
export class FerienbetreuungModule {
}
