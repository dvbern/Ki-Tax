/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {downgradeComponent} from '@angular/upgrade/static';
import * as angular from 'angular';
import {CORE_JS_MODULE} from '../app/core/core.angularjs.module';
import {FileUploadComponent} from '../app/shared/component/file-upload/file-upload.component';
import {AbwesenheitViewComponentConfig} from './component/abwesenheitView/abwesenheitView';
import {BetreuungAbweichungenViewComponentConfig} from './component/betreuungAbweichungenView/betreuungAbweichungenView';
import {BetreuungFerieninselViewComponentConfig} from './component/betreuungFerieninselView/betreuungFerieninselView';
import {BetreuungInputComponentConfig} from './component/betreuungInput/betreuung-input.component';
import {BetreuungListViewComponentConfig} from './component/betreuungListView/betreuungListView';
import {BetreuungMitteilungViewComponentConfig} from './component/betreuungMitteilungView/betreuungMitteilungView';
import {BetreuungOverrideWarningComponent} from './component/betreuungOverrideWarning/betreuung-override-warning.component';
import {BetreuungTagesschuleViewComponentConfig} from './component/betreuungTagesschuleView/betreuungTagesschuleView';
import {BetreuungViewComponentConfig} from './component/betreuungView/betreuungView';
import {DokumenteViewComponentConfig} from './component/DokumenteView/dokumenteView';
import {
    DossierToolbarComponentConfig,
    DossierToolbarGesuchstellerComponentConfig,
} from './component/dossierToolbar/dossierToolbar';
import {DvEingabeBasisjahrComponent} from './component/dv-eingabe-basisjahr/dv-eingabe-basisjahr.component';
import {DvFinanzielleSituationRequire} from './component/dv-finanzielle-situation-require/dv-finanzielle-situation-require';
import {DvSwitchComponent} from './component/dv-switch/dv-switch.component';
import {EinkommensverschlechterungInfoViewComponentConfig} from './component/einkommensverschlechterungInfoView/einkommensverschlechterungInfoView';
import {EinkommensverschlechterungResultateViewComponentConfig} from './component/einkommensverschlechterungResultateView/einkommensverschlechterungResultateView';
import {EinkommensverschlechterungViewComponentConfig} from './component/einkommensverschlechterungView/einkommensverschlechterungView';
import {ErwerbspensumListViewComponentConfig} from './component/erwerbspensumListView/erwerbspensumListView';
import {ErwerbspensumViewComponentConfig} from './component/erwerbspensumView/erwerbspensumView';
import {FallCreationViewComponentConfig} from './component/fallCreationView/fallCreationView';
import {FallToolbarComponent} from './component/fallToolbar/fallToolbar.component';
import {FamiliensituationViewComponentConfig} from './component/familiensituationView/familiensituationView';
import {FinanzielleSituationAufteilungComponent} from './component/finanzielleSituation/bern/finanzielleSituationAufteilung/finanzielle-situation-aufteilung.component';
import {FinanzielleSituationResultateViewComponentConfig} from './component/finanzielleSituation/bern/finanzielleSituationResultateView/finanzielleSituationResultateView';
import {FinanzielleSituationStartViewComponentConfig} from './component/finanzielleSituation/bern/finanzielleSituationStartView/finanzielleSituationStartView';
import {FinanzielleSituationViewComponentConfig} from './component/finanzielleSituation/bern/finanzielleSituationView/finanzielleSituationView';
import {SozialhilfeZeitraumListViewComponentConfig} from './component/finanzielleSituation/bern/sozialhilfeZeitraumListView/sozialhilfeZeitraumListView';
import {SozialhilfeZeitraumViewComponentConfig} from './component/finanzielleSituation/bern/sozialhilfeZeitraumView/sozialhilfeZeitraumView';
import {SteuerabfrageResponseHintsComponent} from './component/finanzielleSituation/bern/steuerabfrageResponseHints/steuerabfrage-response-hints.component';
import {AngabenGesuchsteller2Component} from './component/finanzielleSituation/luzern/angaben-gesuchsteller2/angaben-gesuchsteller2.component';
import {FinanzielleSituationStartViewLuzernComponent} from './component/finanzielleSituation/luzern/finanzielle-situation-start-view-luzern/finanzielle-situation-start-view-luzern.component';
import {ResultatComponent} from './component/finanzielleSituation/luzern/resultat/resultat.component';
import {SelbstdeklarationComponent} from './component/finanzielleSituation/luzern/selbstdeklaration/selbstdeklaration.component';
import {VeranlagungComponent} from './component/finanzielleSituation/luzern/veranlagung/veranlagung.component';
import {FinanzielleSituationStartSolothurnComponent} from './component/finanzielleSituation/solothurn/finanzielle-situation-start-solothurn/finanzielle-situation-start-solothurn.component';
import {FreigabeViewComponentConfig} from './component/freigabeView/freigabeView';
import {InternePendenzDialogComponent} from './component/internePendenzenView/interne-pendenz-dialog/interne-pendenz-dialog.component';
import {InternePendenzenComponent} from './component/internePendenzenView/interne-pendenzen.component';
import {KinderListViewComponentConfig} from './component/kinderListView/kinderListView';
import {KindViewComponentConfig} from './component/kindView/kindView';
import {KommentarViewComponentConfig} from './component/kommentarView/kommentarView';
import {SozialdienstFallCreationViewComponentConfig} from './component/sozialdienstFallCreationView/sozialdienstFallCreationView';
import {StammdatenViewComponentConfig} from './component/stammdatenView/stammdatenView';
import {UmzugViewComponentConfig} from './component/umzugView/umzugView';
import {VerfuegenListViewComponentConfig} from './component/verfuegenListView/verfuegenListView';
import {VerfuegenViewComponentConfig} from './component/verfuegenView/verfuegenView';
import {gesuchRun} from './gesuch.route';

export const GESUCH_JS_MODULE =
    angular.module('ebeguWeb.gesuch', [CORE_JS_MODULE.name])
        .run(gesuchRun)
        .component('familiensituationView', new FamiliensituationViewComponentConfig())
        .component('stammdatenView', new StammdatenViewComponentConfig())
        .component('umzugView', new UmzugViewComponentConfig())
        .component('kinderListView', new KinderListViewComponentConfig())
        .component('finanzielleSituationView', new FinanzielleSituationViewComponentConfig())
        .component('finanzielleSituationStartView', new FinanzielleSituationStartViewComponentConfig())
        .component('finanzielleSituationResultateView', new FinanzielleSituationResultateViewComponentConfig())
        .component('dvFinanzielleSituationRequire', new DvFinanzielleSituationRequire())
        .component('finanzielleSituationStartLuzern', downgradeComponent({component: FinanzielleSituationStartViewLuzernComponent}))
        .component('finanzielleSituationStartSolothurn', downgradeComponent({component: FinanzielleSituationStartSolothurnComponent}))
        .component('finanzielleSituationAngabenGS2Luzern', downgradeComponent({component: AngabenGesuchsteller2Component}))
        .component('finanzielleSituationSelbstdeklarationLuzern', downgradeComponent({component: SelbstdeklarationComponent}))
        .component('finanzielleSituationVeranlagungLuzern', downgradeComponent({component: VeranlagungComponent}))
        .component('finanzielleSituationResultatLuzern', downgradeComponent({component: ResultatComponent}))
        .component('kindView', new KindViewComponentConfig())
        .component('betreuungListView', new BetreuungListViewComponentConfig())
        .component('betreuungView', new BetreuungViewComponentConfig())
        .component('betreuungAbweichungenView', new BetreuungAbweichungenViewComponentConfig())
        .component('betreuungTagesschuleView', new BetreuungTagesschuleViewComponentConfig())
        .component('abwesenheitView', new AbwesenheitViewComponentConfig())
        .component('erwerbspensumListView', new ErwerbspensumListViewComponentConfig())
        .component('erwerbspensumView', new ErwerbspensumViewComponentConfig())
        .component('fallCreationView', new FallCreationViewComponentConfig())
        .component('verfuegenListView', new VerfuegenListViewComponentConfig())
        .component('verfuegenView', new VerfuegenViewComponentConfig())
        .component('dossierToolbar', new DossierToolbarComponentConfig())
        .component('dossierToolbarGesuchsteller', new DossierToolbarGesuchstellerComponentConfig())
        .component('einkommensverschlechterungInfoView', new EinkommensverschlechterungInfoViewComponentConfig())
        .component('einkommensverschlechterungView', new EinkommensverschlechterungViewComponentConfig())
        .component('einkommensverschlechterungResultateView',
            new EinkommensverschlechterungResultateViewComponentConfig())
        .component('freigabeView', new FreigabeViewComponentConfig())
        .component('dokumenteView', new DokumenteViewComponentConfig())
        .component('kommentarView', new KommentarViewComponentConfig())
        .component('betreuungMitteilungView', new BetreuungMitteilungViewComponentConfig())
        .component('betreuungFerieninselView', new BetreuungFerieninselViewComponentConfig())
        .component('sozialhilfeZeitraumListView', new SozialhilfeZeitraumListViewComponentConfig())
        .component('sozialhilfeZeitraumView', new SozialhilfeZeitraumViewComponentConfig())
        .directive('dvFallToolbar', downgradeComponent({component: FallToolbarComponent}))
        .component('dvBetreuungInput', new BetreuungInputComponentConfig())
        .directive('dvEingabeBasisjahr', downgradeComponent({component: DvEingabeBasisjahrComponent}))
        .directive('dvSwitch', downgradeComponent({component: DvSwitchComponent}))
        .directive('betreuungOverrideWarning', downgradeComponent({component: BetreuungOverrideWarningComponent}))
        .directive('dvFileUpload',
            downgradeComponent({
                component: FileUploadComponent,
                inputs: ['title', 'files', 'readOnly', 'readOnlyDelete'],
                outputs: ['download', 'delete', 'uploadFile']
            }))
        .component('sozialdienstFallCreationView', new SozialdienstFallCreationViewComponentConfig())
        .directive('internePendenzenView', downgradeComponent({component: InternePendenzenComponent}))
        .directive('internePendenzenDialog', downgradeComponent({component: InternePendenzDialogComponent}))
        .directive('dvFinanzielleSituationAufteilung', downgradeComponent({
            component: FinanzielleSituationAufteilungComponent,
            outputs: ['closeEvent']
        }))
        .directive('dvSteuerabfrageResponseHints',
            downgradeComponent({
                component: SteuerabfrageResponseHintsComponent,
                inputs: ['status'],
                outputs: ['tryAgainEvent']
            }))
;
