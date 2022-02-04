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

import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {UpgradeModule} from '@angular/upgrade/static';
import {SharedModule} from '../app/shared/shared.module';
import {BetreuungOverrideWarningComponent} from './component/betreuungOverrideWarning/betreuung-override-warning.component';
import {DvEingabeBasisjahrComponent} from './component/dv-eingabe-basisjahr/dv-eingabe-basisjahr.component';
import {DvFinanzielleSituationRequireX} from './component/dv-finanzielle-situation-require/dv-finanzielle-situation-require-x.component';
import {DvSwitchComponent} from './component/dv-switch/dv-switch.component';
import {FallToolbarModule} from './component/fallToolbar/fall-toolbar.module';
import {AngabenGesuchsteller2Component} from './component/finanzielleSituation/luzern/angaben-gesuchsteller2/angaben-gesuchsteller2.component';
import {FinanzielleSituationStartViewLuzernComponent} from './component/finanzielleSituation/luzern/finanzielle-situation-start-view-luzern/finanzielle-situation-start-view-luzern.component';
import {InfomaFieldsComponent} from './component/finanzielleSituation/luzern/infoma-fields/infoma-fields.component';
import {ResultatComponent} from './component/finanzielleSituation/luzern/resultat/resultat.component';
import {SelbstdeklarationComponent} from './component/finanzielleSituation/luzern/selbstdeklaration/selbstdeklaration.component';
import {VeranlagungComponent} from './component/finanzielleSituation/luzern/veranlagung/veranlagung.component';
import {AngabenGs1Component} from './component/finanzielleSituation/solothurn/angaben-gs/angaben-gs1/angaben-gs1.component';
import {AngabenGs2Component} from './component/finanzielleSituation/solothurn/angaben-gs/angaben-gs2/angaben-gs2.component';
import {BruttolohnComponent} from './component/finanzielleSituation/solothurn/bruttolohn/bruttolohn.component';
import {FinanzielleSituationStartSolothurnComponent} from './component/finanzielleSituation/solothurn/finanzielle-situation-start-solothurn/finanzielle-situation-start-solothurn.component';
import {SteuerveranlagungErhaltenComponent} from './component/finanzielleSituation/solothurn/steuerveranlagung-erhalten/steuerveranlagung-erhalten.component';
import {SteuerveranlagungGemeinsamComponent} from './component/finanzielleSituation/solothurn/steuerveranlagung-gemeinsam/steuerveranlagung-gemeinsam.component';
import {VeranlagungSolothurnComponent} from './component/finanzielleSituation/solothurn/veranlagung/veranlagung-solothurn.component';
import {InternePendenzDialogComponent} from './component/internePendenzenView/interne-pendenz-dialog/interne-pendenz-dialog.component';
import {InternePendenzenTableComponent} from './component/internePendenzenView/interne-pendenzen-table/interne-pendenzen-table.component';
import {InternePendenzenComponent} from './component/internePendenzenView/interne-pendenzen.component';

@NgModule({
    imports: [
        FormsModule,
        ReactiveFormsModule,
        UpgradeModule,
        SharedModule,
        FallToolbarModule
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    declarations: [
        DvSwitchComponent,
        BetreuungOverrideWarningComponent,
        DvEingabeBasisjahrComponent,
        InternePendenzenComponent,
        InternePendenzDialogComponent,
        InternePendenzenTableComponent,
        FinanzielleSituationStartViewLuzernComponent,
        FinanzielleSituationStartSolothurnComponent,
        DvFinanzielleSituationRequireX,
        AngabenGesuchsteller2Component,
        SelbstdeklarationComponent,
        VeranlagungComponent,
        InfomaFieldsComponent,
        VeranlagungComponent,
        ResultatComponent,
        VeranlagungSolothurnComponent,
        SteuerveranlagungErhaltenComponent,
        BruttolohnComponent,
        SteuerveranlagungGemeinsamComponent,
        AngabenGs1Component,
        AngabenGs2Component
    ],
    exports: [
        DvSwitchComponent,
    ],
})

export class NgGesuchModule {
}
