/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {NgModule} from '@angular/core';
import {SharedModule} from '../app/shared/shared.module';
import {BetreuungOverrideWarningComponent} from './component/betreuungOverrideWarning/betreuung-override-warning.component';
import {DvEingabeBasisjahrComponent} from './component/dv-eingabe-basisjahr/dv-eingabe-basisjahr.component';
import {DvSwitchComponent} from './component/dv-switch/dv-switch.component';
import {FallToolbarComponent} from './component/fallToolbar/fallToolbar.component';

@NgModule({
    imports: [
        SharedModule,
    ],
    declarations: [
        FallToolbarComponent,
        DvSwitchComponent,
        BetreuungOverrideWarningComponent,
        DvEingabeBasisjahrComponent
    ],
    entryComponents: [
        FallToolbarComponent,
        DvSwitchComponent,
        BetreuungOverrideWarningComponent,
        DvEingabeBasisjahrComponent
    ],
})

export class NgGesuchModule {
}
