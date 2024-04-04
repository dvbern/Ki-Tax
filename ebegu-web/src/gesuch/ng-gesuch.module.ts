/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {UpgradeModule} from '@angular/upgrade/static';
import {SharedModule} from '../app/shared/shared.module';
import {
    SelbstdeklarationComponent
} from './component/abstractFinanzielleSituation/luzern/selbstdeklaration/selbstdeklaration.component';
import {
    BetreuungOverrideWarningComponent
} from './component/betreuungOverrideWarning/betreuung-override-warning.component';
import {
    DvFinanzielleSituationRequireXComponent
} from './component/dv-finanzielle-situation-require/dv-finanzielle-situation-require-x.component';
import {DvSwitchComponent} from './component/dv-switch/dv-switch.component';
import {
    EinkommensverschlechterungResultateViewComponent
} from './component/einkommensverschlechterung/bern/einkommensverschlechterung-resultate-view/einkommensverschlechterung-resultate-view.component';
import {
    EinkommensverschlechterungLuzernResultateViewComponent
} from './component/einkommensverschlechterung/luzern/einkommensverschlechterung-luzern-resultate-view/einkommensverschlechterung-luzern-resultate-view.component';
import {
    EinkommensverschlechterungLuzernViewComponent
} from './component/einkommensverschlechterung/luzern/einkommensverschlechterung-luzern-view/einkommensverschlechterung-luzern-view.component';
import {
    EinkommensverschlechterungSchwyzGsComponent
} from './component/einkommensverschlechterung/schwyz/einkommensverschlechterung-schwyz-gs/einkommensverschlechterung-schwyz-gs.component';
import {
    EinkommensverschlechterungSolothurnResultateViewComponent
} from './component/einkommensverschlechterung/solothurn/einkommensverschlechterung-solothurn-resultate-view/einkommensverschlechterung-solothurn-resultate-view.component';
import {
    EinkommensverschlechterungSolothurnViewComponent
} from './component/einkommensverschlechterung/solothurn/einkommensverschlechterung-solothurn-view/einkommensverschlechterung-solothurn-view.component';
import {FallCreationViewXComponent} from './component/fall-creation-view-x/fall-creation-view-x.component';
import {FallToolbarModule} from './component/fallToolbar/fall-toolbar.module';
import {
    FamiliensituationAppenzellViewXComponent
} from './component/familiensituation/familiensituation-appenzell-view-x/familiensituation-appenzell-view-x.component';
import {
    FamiliensituationSchwyzComponent
} from './component/familiensituation/familiensituation-schwyz/familiensituation-schwyz.component';
import {
    FamiliensituationViewXComponent
} from './component/familiensituation/familiensituation-view-x/familiensituation-view-x.component';
import {
    FinSitFelderAppenzellComponent
} from './component/abstractFinanzielleSituation/appenzell/fin-sit-zusatzfelder-appenzell/fin-sit-felder-appenzell.component';
import {
    FinanzielleSituationAppenzellViewComponent
} from './component/finanzielleSituation/appenzell/finanzielle-situation-appenzell-view/finanzielle-situation-appenzell-view.component';
import {
    DialogInitZPVNummerVerknuepfenComponent
} from './component/finanzielleSituation/bern/dialog-init-zpv-nummer-verknuepfen/dialog-init-zpv-nummer-verknpuefen.component';
import {
    AufteilungComponent
} from './component/finanzielleSituation/bern/finanzielleSituationAufteilung/aufteilung/aufteilung.component';
import {
    FinanzielleSituationAufteilungComponent
} from './component/finanzielleSituation/bern/finanzielleSituationAufteilung/finanzielle-situation-aufteilung.component';
import {
    SteuerabfrageResponseHintsComponent
} from './component/finanzielleSituation/bern/steuerabfrageResponseHints/steuerabfrage-response-hints.component';
import {
    AngabenGesuchsteller2Component
} from './component/finanzielleSituation/luzern/angaben-gesuchsteller2/angaben-gesuchsteller2.component';
import {
    FinanzielleSituationStartViewLuzernComponent
} from './component/finanzielleSituation/luzern/finanzielle-situation-start-view-luzern/finanzielle-situation-start-view-luzern.component';
import {ZahlungsinformationenFieldsComponent} from './component/finanzielleSituation/luzern/zahlungsinformationen-fields/zahlungsinformationen-fields.component';
import {ResultatComponent} from './component/finanzielleSituation/luzern/resultat/resultat.component';
import {VeranlagungComponent} from './component/finanzielleSituation/luzern/veranlagung/veranlagung.component';
import {FinanzielleSituationSchwyzModule} from './component/finanzielleSituation/schwyz/finanzielle-situation-schwyz.module';
import {
    AngabenGs1Component
} from './component/finanzielleSituation/solothurn/angaben-gs/angaben-gs1/angaben-gs1.component';
import {
    AngabenGs2Component
} from './component/finanzielleSituation/solothurn/angaben-gs/angaben-gs2/angaben-gs2.component';
import {BruttolohnComponent} from './component/finanzielleSituation/solothurn/bruttolohn/bruttolohn.component';
import {
    FinanzielleSituationStartSolothurnComponent
} from './component/finanzielleSituation/solothurn/finanzielle-situation-start-solothurn/finanzielle-situation-start-solothurn.component';
import {
    MassgebendesEinkommenComponent
} from './component/abstractFinanzielleSituation/resultat/massgebendes-einkommen.component';
import {
    SteuerveranlagungErhaltenComponent
} from './component/finanzielleSituation/solothurn/steuerveranlagung-erhalten/steuerveranlagung-erhalten.component';
import {
    VeranlagungSolothurnComponent
} from './component/finanzielleSituation/solothurn/veranlagung/veranlagung-solothurn.component';
import {
    InternePendenzDialogComponent
} from './component/internePendenzenView/interne-pendenz-dialog/interne-pendenz-dialog.component';
import {
    InternePendenzenTableComponent
} from './component/internePendenzenView/interne-pendenzen-table/interne-pendenzen-table.component';
import {InternePendenzenComponent} from './component/internePendenzenView/interne-pendenzen.component';
import {FkjvKinderabzugComponent} from './component/kindView/fkjv-kinderabzug/fkjv-kinderabzug.component';
import {KindFachstelleComponent} from './component/kindView/kind-fachstelle/kind-fachstelle.component';
import {ZahlungsstatusIconComponent} from './component/zahlungsstatus-icon/zahlungsstatus-icon.component';
import {
    EinkommensverschlechterungAppenzellViewComponent
} from './component/einkommensverschlechterung/appenzell/einkommensverschlechterung-appenzell-view/einkommensverschlechterung-appenzell-view.component';
import {
    EinkommensverschlechterungAppenzellResultateViewComponent
} from './component/einkommensverschlechterung/appenzell/einkommensverschlechterung-appenzell-resultate-view/einkommensverschlechterung-appenzell-resultate-view.component';

@NgModule({
    imports: [
        FormsModule,
        ReactiveFormsModule,
        UpgradeModule,
        SharedModule,
        FallToolbarModule,
        FinanzielleSituationSchwyzModule
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    declarations: [
        DvSwitchComponent,
        BetreuungOverrideWarningComponent,
        InternePendenzenComponent,
        InternePendenzDialogComponent,
        InternePendenzenTableComponent,
        FinanzielleSituationStartViewLuzernComponent,
        FinanzielleSituationStartSolothurnComponent,
        EinkommensverschlechterungLuzernViewComponent,
        EinkommensverschlechterungSolothurnViewComponent,
        EinkommensverschlechterungLuzernResultateViewComponent,
        EinkommensverschlechterungSolothurnResultateViewComponent,
        EinkommensverschlechterungResultateViewComponent,
        EinkommensverschlechterungAppenzellViewComponent,
        EinkommensverschlechterungAppenzellResultateViewComponent,
        DvFinanzielleSituationRequireXComponent,
        AngabenGesuchsteller2Component,
        SelbstdeklarationComponent,
        VeranlagungComponent,
        FinSitFelderAppenzellComponent,
        FinanzielleSituationAppenzellViewComponent,
        ZahlungsinformationenFieldsComponent,
        VeranlagungComponent,
        ResultatComponent,
        VeranlagungSolothurnComponent,
        SteuerveranlagungErhaltenComponent,
        BruttolohnComponent,
        AngabenGs1Component,
        AngabenGs2Component,
        MassgebendesEinkommenComponent,
        FkjvKinderabzugComponent,
        MassgebendesEinkommenComponent,
        FinanzielleSituationAufteilungComponent,
        AufteilungComponent,
        SteuerabfrageResponseHintsComponent,
        DialogInitZPVNummerVerknuepfenComponent,
        FallCreationViewXComponent,
        FamiliensituationViewXComponent,
        FamiliensituationAppenzellViewXComponent,
        FamiliensituationSchwyzComponent,
        ZahlungsstatusIconComponent,
        KindFachstelleComponent
    ],
    exports: [
        DvSwitchComponent,
        ZahlungsstatusIconComponent
    ],
})

export class NgGesuchModule {
}
