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

import {downgradeComponent, downgradeInjectable} from '@angular/upgrade/static';
import * as angular from 'angular';
import {CORE_JS_MODULE} from '../app/core/core.angularjs.module';
import {PersonensucheComponent} from '../app/personensuche/personensuche.component';
import {MultipleFileUploadComponent} from '../app/shared/component/multpile-file-upload/multiple-file-upload.component';
import {SelbstdeklarationComponent} from './component/abstractFinanzielleSituation/luzern/selbstdeklaration/selbstdeklaration.component';
import {AbwesenheitViewComponentConfig} from './component/abwesenheitView/abwesenheitView';
import {BetreuungAbweichungenViewComponentConfig} from './component/betreuungAbweichungenView/betreuungAbweichungenView';
import {BetreuungFerieninselViewComponentConfig} from './component/betreuungFerieninselView/betreuungFerieninselView';
import {BetreuungInputConfig} from './component/betreuungInput/betreuung-input';
import {BetreuungListViewComponentConfig} from './component/betreuungListView/betreuungListView';
import {BetreuungMitteilungViewComponentConfig} from './component/betreuungMitteilungView/betreuungMitteilungView';
import {BetreuungOverrideWarningComponent} from './component/betreuungOverrideWarning/betreuung-override-warning.component';
import {BetreuungTagesschuleViewComponentConfig} from './component/betreuungTagesschuleView/betreuungTagesschuleView';
import {BetreuungViewComponentConfig} from './component/betreuungView/betreuungView';
import {DokumenteViewComponentConfig} from './component/DokumenteView/dokumenteView';
import {
    DossierToolbarComponentConfig,
    DossierToolbarGesuchstellerComponentConfig
} from './component/dossierToolbar/dossierToolbar';
import {DvEingabeHintComponent} from './component/dv-eingabe-hint/dv-eingabe-hint.component';
import {DvFinanzielleSituationRequire} from './component/dv-finanzielle-situation-require/dv-finanzielle-situation-require';
import {DvSwitchComponent} from './component/dv-switch/dv-switch.component';
import {EinkommensverschlechterungResultateViewComponent} from './component/einkommensverschlechterung/bern/einkommensverschlechterung-resultate-view/einkommensverschlechterung-resultate-view.component';
import {EinkommensverschlechterungViewComponentConfig} from './component/einkommensverschlechterung/bern/einkommensverschlechterungView/einkommensverschlechterungView';
import {EinkommensverschlechterungInfoViewComponentConfig} from './component/einkommensverschlechterung/einkommensverschlechterungInfoView/einkommensverschlechterungInfoView';
import {EinkommensverschlechterungLuzernResultateViewComponent} from './component/einkommensverschlechterung/luzern/einkommensverschlechterung-luzern-resultate-view/einkommensverschlechterung-luzern-resultate-view.component';
import {EinkommensverschlechterungLuzernViewComponent} from './component/einkommensverschlechterung/luzern/einkommensverschlechterung-luzern-view/einkommensverschlechterung-luzern-view.component';
import {EinkommensverschlechterungSolothurnResultateViewComponent} from './component/einkommensverschlechterung/solothurn/einkommensverschlechterung-solothurn-resultate-view/einkommensverschlechterung-solothurn-resultate-view.component';
import {EinkommensverschlechterungSolothurnViewComponent} from './component/einkommensverschlechterung/solothurn/einkommensverschlechterung-solothurn-view/einkommensverschlechterung-solothurn-view.component';
import {ErwerbspensumListViewComponentConfig} from './component/erwerbspensumListView/erwerbspensumListView';
import {ErwerbspensumViewComponentConfig} from './component/erwerbspensumView/erwerbspensumView';
import {FallCreationViewXComponent} from './component/fall-creation-view-x/fall-creation-view-x.component';
import {FallToolbarComponent} from './component/fallToolbar/fallToolbar.component';
import {FamiliensituationAppenzellViewXComponent} from './component/familiensituation/familiensituation-appenzell-view-x/familiensituation-appenzell-view-x.component';
import {FamiliensituationSchwyzComponent} from './component/familiensituation/familiensituation-schwyz/familiensituation-schwyz.component';
import {FamiliensituationViewXComponent} from './component/familiensituation/familiensituation-view-x/familiensituation-view-x.component';
import {FinSitFelderAppenzellComponent} from './component/abstractFinanzielleSituation/appenzell/fin-sit-zusatzfelder-appenzell/fin-sit-felder-appenzell.component';
import {familiensituationRun} from './component/familiensituation/familiensituation.route';
import {FinanzielleSituationAppenzellViewComponent} from './component/finanzielleSituation/appenzell/finanzielle-situation-appenzell-view/finanzielle-situation-appenzell-view.component';
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
import {VeranlagungComponent} from './component/finanzielleSituation/luzern/veranlagung/veranlagung.component';
import {finSitSchwyzRun} from './component/finanzielleSituation/schwyz/fin-sit-schwyz.route';
import {FinanzielleSituationStartSolothurnComponent} from './component/finanzielleSituation/solothurn/finanzielle-situation-start-solothurn/finanzielle-situation-start-solothurn.component';
import {FreigabeViewComponentConfig} from './freigabe/component/freigabeView/freigabeView';
import {InternePendenzDialogComponent} from './component/internePendenzenView/interne-pendenz-dialog/interne-pendenz-dialog.component';
import {InternePendenzenComponent} from './component/internePendenzenView/interne-pendenzen.component';
import {KinderListViewComponentConfig} from './component/kinderListView/kinderListView';
import {FkjvKinderabzugComponent} from './component/kindView/fkjv-kinderabzug/fkjv-kinderabzug.component';
import {HoehereBetraegeBeeintraechtigungComponent} from './component/kindView/hoehere-betraege-beeintraechtigung/hoehere-betraege-beeintraechtigung.component';
import {SchwyzKinderabzugComponent} from './component/kindView/schwyz-kinderabzug/schwyz-kinderabzug.component';
import {FreigabeService} from './freigabe/freigabe.service';
import {OnlineFreigabeComponent} from './freigabe/component/onlineFreigabe/online-freigabe.component';
import {HybridFormBridgeService} from './service/hybrid-form-bridge.service';
import {KindFachstelleComponent} from './component/kindView/kind-fachstelle/kind-fachstelle.component';
import {KindViewComponentConfig} from './component/kindView/kindView';
import {KommentarViewComponentConfig} from './component/kommentarView/kommentarView';
import {SozialdienstFallCreationViewComponentConfig} from './component/sozialdienstFallCreationView/sozialdienstFallCreationView';
import {StammdatenViewComponentConfig} from './component/stammdatenView/stammdatenView';
import {UmzugViewComponentConfig} from './component/umzugView/umzugView';
import {VerfuegenListViewComponentConfig} from './component/verfuegenListView/verfuegenListView';
import {VerfuegenViewComponentConfig} from './component/verfuegenView/verfuegenView';
import {ZahlungsstatusIconComponent} from './component/zahlungsstatus-icon/zahlungsstatus-icon.component';
import {gesuchRun} from './gesuch.route';
import {EinkommensverschlechterungAppenzellResultateViewComponent} from './component/einkommensverschlechterung/appenzell/einkommensverschlechterung-appenzell-resultate-view/einkommensverschlechterung-appenzell-resultate-view.component';
import {EinkommensverschlechterungAppenzellViewComponent} from './component/einkommensverschlechterung/appenzell/einkommensverschlechterung-appenzell-view/einkommensverschlechterung-appenzell-view.component';
import {abweichungenEnabledHook} from './state-hooks/abweichungen-enabled.hook';
import {TransitionService} from '@uirouter/core';
import {ApplicationPropertyRS} from '../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../authentication/service/AuthServiceRS.rest';

export const GESUCH_JS_MODULE = angular
    .module('ebeguWeb.gesuch', [CORE_JS_MODULE.name])
    .run(gesuchRun)
    .run(finSitSchwyzRun)
    .run(familiensituationRun)
    .run([
        '$transitions',
        'ApplicationPropertyRS',
        'AuthServiceRS',
        (
            $transitions: TransitionService,
            applicationPropertyRS: ApplicationPropertyRS,
            authService: AuthServiceRS
        ) =>
            abweichungenEnabledHook(
                $transitions,
                applicationPropertyRS,
                authService
            )
    ])
    .component(
        'familiensituationView',
        downgradeComponent({component: FamiliensituationViewXComponent})
    )
    .component(
        'familiensituationAppenzellView',
        downgradeComponent({
            component: FamiliensituationAppenzellViewXComponent
        })
    )
    .component(
        'familiensituationSchwyzView',
        downgradeComponent({component: FamiliensituationSchwyzComponent})
    )
    .component('stammdatenView', new StammdatenViewComponentConfig())
    .component('umzugView', new UmzugViewComponentConfig())
    .component('kinderListView', new KinderListViewComponentConfig())
    .component(
        'finanzielleSituationView',
        new FinanzielleSituationViewComponentConfig()
    )
    .component(
        'finanzielleSituationStartView',
        new FinanzielleSituationStartViewComponentConfig()
    )
    .component(
        'finanzielleSituationResultateView',
        new FinanzielleSituationResultateViewComponentConfig()
    )
    .component(
        'dvFinanzielleSituationRequire',
        new DvFinanzielleSituationRequire()
    )
    .component(
        'finanzielleSituationStartLuzern',
        downgradeComponent({
            component: FinanzielleSituationStartViewLuzernComponent
        })
    )
    .component(
        'finanzielleSituationStartSolothurn',
        downgradeComponent({
            component: FinanzielleSituationStartSolothurnComponent
        })
    )
    .component(
        'finanzielleSituationAngabenGS2Luzern',
        downgradeComponent({component: AngabenGesuchsteller2Component})
    )
    .component(
        'finanzielleSituationSelbstdeklarationLuzern',
        downgradeComponent({component: SelbstdeklarationComponent})
    )
    .component(
        'finanzielleVerhaeltnisseAppenzell',
        downgradeComponent({component: FinSitFelderAppenzellComponent})
    )
    .component(
        'finanzielleSituationVeranlagungLuzern',
        downgradeComponent({component: VeranlagungComponent})
    )
    .component(
        'finanzielleSituationResultatLuzern',
        downgradeComponent({component: ResultatComponent})
    )
    .component(
        'finanzielleSituationAppenzell',
        downgradeComponent({
            component: FinanzielleSituationAppenzellViewComponent
        })
    )
    .component(
        'einkommensverschlechterungLuzernView,',
        downgradeComponent({
            component: EinkommensverschlechterungLuzernViewComponent
        })
    )
    .component(
        'einkommensverschlechterungSolothurnView,',
        downgradeComponent({
            component: EinkommensverschlechterungSolothurnViewComponent
        })
    )
    .component(
        'einkommensverschlechterungLuzernResultateView',
        downgradeComponent({
            component: EinkommensverschlechterungLuzernResultateViewComponent
        })
    )
    .component(
        'einkommensverschlechterungSolothurnResultateView',
        downgradeComponent({
            component: EinkommensverschlechterungSolothurnResultateViewComponent
        })
    )
    .component(
        'einkommensverschlechterungAppenzellResultateView',
        downgradeComponent({
            component: EinkommensverschlechterungAppenzellResultateViewComponent
        })
    )
    .component(
        'einkommensverschlechterungAppenzellViewComponent',
        downgradeComponent({
            component: EinkommensverschlechterungAppenzellViewComponent
        })
    )
    .component(
        'dvFallCreationViewX',
        downgradeComponent({component: FallCreationViewXComponent})
    )
    .component('kindView', new KindViewComponentConfig())
    .component('betreuungListView', new BetreuungListViewComponentConfig())
    .component('betreuungView', new BetreuungViewComponentConfig())
    .component(
        'betreuungAbweichungenView',
        new BetreuungAbweichungenViewComponentConfig()
    )
    .component(
        'betreuungTagesschuleView',
        new BetreuungTagesschuleViewComponentConfig()
    )
    .component('abwesenheitView', new AbwesenheitViewComponentConfig())
    .component(
        'erwerbspensumListView',
        new ErwerbspensumListViewComponentConfig()
    )
    .component('erwerbspensumView', new ErwerbspensumViewComponentConfig())
    .component('verfuegenListView', new VerfuegenListViewComponentConfig())
    .component('verfuegenView', new VerfuegenViewComponentConfig())
    .component('dossierToolbar', new DossierToolbarComponentConfig())
    .component(
        'dossierToolbarGesuchsteller',
        new DossierToolbarGesuchstellerComponentConfig()
    )
    .component(
        'einkommensverschlechterungInfoView',
        new EinkommensverschlechterungInfoViewComponentConfig()
    )
    .component(
        'einkommensverschlechterungView',
        new EinkommensverschlechterungViewComponentConfig()
    )
    .component(
        'einkommensverschlechterungResultateView',
        downgradeComponent({
            component: EinkommensverschlechterungResultateViewComponent
        })
    )
    .component('freigabeView', new FreigabeViewComponentConfig())
    .component('dokumenteView', new DokumenteViewComponentConfig())
    .component('kommentarView', new KommentarViewComponentConfig())
    .component(
        'betreuungMitteilungView',
        new BetreuungMitteilungViewComponentConfig()
    )
    .component(
        'betreuungFerieninselView',
        new BetreuungFerieninselViewComponentConfig()
    )
    .component(
        'sozialhilfeZeitraumListView',
        new SozialhilfeZeitraumListViewComponentConfig()
    )
    .component(
        'sozialhilfeZeitraumView',
        new SozialhilfeZeitraumViewComponentConfig()
    )
    .factory(
        'HybridFormBridgeService',
        downgradeInjectable(HybridFormBridgeService as any)
    )
    .directive(
        'dvFallToolbar',
        downgradeComponent({component: FallToolbarComponent})
    )
    .component('dvBetreuungInput', new BetreuungInputConfig())
    .directive(
        'dvEingabeHint',
        downgradeComponent({component: DvEingabeHintComponent})
    )
    .directive('dvSwitch', downgradeComponent({component: DvSwitchComponent}))
    .directive(
        'betreuungOverrideWarning',
        downgradeComponent({component: BetreuungOverrideWarningComponent})
    )
    .directive(
        'dvMultipleFileUpload',
        downgradeComponent({
            component: MultipleFileUploadComponent,
            inputs: [
                'title',
                'files',
                'readOnly',
                'readOnlyDelete',
                'tooltipText'
            ],
            outputs: ['download', 'delete', 'uploadFile']
        })
    )
    .component(
        'sozialdienstFallCreationView',
        new SozialdienstFallCreationViewComponentConfig()
    )
    .directive(
        'internePendenzenView',
        downgradeComponent({component: InternePendenzenComponent})
    )
    .directive(
        'internePendenzenDialog',
        downgradeComponent({component: InternePendenzDialogComponent})
    )
    .directive(
        'dvFkjvKinderabzug',
        downgradeComponent({
            component: FkjvKinderabzugComponent,
            inputs: ['kindContainer']
        })
    )
    .directive(
        'hoehereBetraegeBeeintraechtigung',
        downgradeComponent({
            component: HoehereBetraegeBeeintraechtigungComponent,
            inputs: ['kindContainer', 'readOnly']
        })
    )
    .directive(
        'dvSchwyzKinderabzug',
        downgradeComponent({
            component: SchwyzKinderabzugComponent,
            inputs: ['kindContainer']
        })
    )
    .directive(
        'dvKindFachstelle',
        downgradeComponent({
            component: KindFachstelleComponent,
            inputs: ['pensumFachstelle', 'submitted', 'index']
        })
    )
    .directive(
        'dvFinanzielleSituationAufteilung',
        downgradeComponent({
            component: FinanzielleSituationAufteilungComponent,
            outputs: ['closeEvent']
        })
    )
    .directive(
        'dvSteuerabfrageResponseHints',
        downgradeComponent({
            component: SteuerabfrageResponseHintsComponent,
            inputs: ['status', 'finSitRequestState', 'finSitRequestRunning'],
            outputs: ['tryAgainEvent']
        })
    )
    .directive(
        'dvZahlungsstatusIcon',
        downgradeComponent({component: ZahlungsstatusIconComponent})
    )
    .component(
        'dvOnlineFreigabe',
        downgradeComponent({component: OnlineFreigabeComponent})
    )
    .factory('FreigabeService', downgradeInjectable(FreigabeService))
    .directive(
        'dvPersonensuche',
        downgradeComponent({component: PersonensucheComponent})
    );
