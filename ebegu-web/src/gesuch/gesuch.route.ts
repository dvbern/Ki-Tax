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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import {KindRS} from '../app/core/service/kindRS.rest';
import {AuthServiceRS} from '../authentication/service/AuthServiceRS.rest';
import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import {TSCreationAction} from '../models/enums/TSCreationAction';
import {TSEingangsart} from '../models/enums/TSEingangsart';
import {TSGesuch} from '../models/TSGesuch';
import {TSKindDublette} from '../models/TSKindDublette';
import {TSMahnung} from '../models/TSMahnung';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import {EinkommensverschlechterungResultateViewComponent} from './component/einkommensverschlechterung/bern/einkommensverschlechterung-resultate-view/einkommensverschlechterung-resultate-view.component';
import {EinkommensverschlechterungLuzernResultateViewComponent} from './component/einkommensverschlechterung/luzern/einkommensverschlechterung-luzern-resultate-view/einkommensverschlechterung-luzern-resultate-view.component';
import {EinkommensverschlechterungLuzernViewComponent} from './component/einkommensverschlechterung/luzern/einkommensverschlechterung-luzern-view/einkommensverschlechterung-luzern-view.component';
import {EinkommensverschlechterungSolothurnResultateViewComponent} from './component/einkommensverschlechterung/solothurn/einkommensverschlechterung-solothurn-resultate-view/einkommensverschlechterung-solothurn-resultate-view.component';
import {EinkommensverschlechterungSolothurnViewComponent} from './component/einkommensverschlechterung/solothurn/einkommensverschlechterung-solothurn-view/einkommensverschlechterung-solothurn-view.component';
import {FallCreationViewXComponent} from './component/fall-creation-view-x/fall-creation-view-x.component';
import {FamiliensituationViewXComponent} from './component/familiensituation-view-x/familiensituation-view-x.component';
import {FinanzielleSituationAppenzellViewComponent} from './component/finanzielleSituation/appenzell/finanzielle-situation-appenzell-view/finanzielle-situation-appenzell-view.component';
import {AngabenGesuchsteller2Component} from './component/finanzielleSituation/luzern/angaben-gesuchsteller2/angaben-gesuchsteller2.component';
import {FinanzielleSituationStartViewLuzernComponent} from './component/finanzielleSituation/luzern/finanzielle-situation-start-view-luzern/finanzielle-situation-start-view-luzern.component';
import {AngabenGs1Component} from './component/finanzielleSituation/solothurn/angaben-gs/angaben-gs1/angaben-gs1.component';
import {AngabenGs2Component} from './component/finanzielleSituation/solothurn/angaben-gs/angaben-gs2/angaben-gs2.component';
import {FinanzielleSituationStartSolothurnComponent} from './component/finanzielleSituation/solothurn/finanzielle-situation-start-solothurn/finanzielle-situation-start-solothurn.component';
import {GesuchRouteController} from './gesuch';
import {BerechnungsManager} from './service/berechnungsManager';
import {GesuchModelManager} from './service/gesuchModelManager';
import {MahnungRS} from './service/mahnungRS.rest';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;

/* eslint-disable */

const gesuchTpl = require('./gesuch.html');

gesuchRun.$inject = ['RouterHelper'];

export function gesuchRun(routerHelper: RouterHelper): void {
    routerHelper.configureStates(ng1States);
}

// STATES

export class EbeguGesuchState implements Ng1StateDeclaration {
    public parent = 'app';
    public abstract = true;
    public name = 'gesuch';
    public template = gesuchTpl;
    public url = '/gesuch';
    public controller = GesuchRouteController;
    public controllerAs = 'vm';
}

const sozialdienstfallCreationView = '<sozialdienst-fall-creation-view>';

const kommentarView = '<kommentar-view>';

export class EbeguNewFallState implements Ng1StateDeclaration {
    public name = 'gesuch.fallcreation';
    public url = '/fall/:creationAction/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId/:gemeindeId';
    public params = {
        creationAction: '',
        eingangsart: '',
        gesuchsperiodeId: '',
        gesuchId: '',
        dossierId: '',
        gemeindeId: '',
    };

    public views: any = {
        gesuchViewPort: {
            component: FallCreationViewXComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: reloadGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt(),
    };
}

export class EbeguNewSozialdienstFallState implements Ng1StateDeclaration {
    public name = 'gesuch.sozialdienstfallcreation';
    public url =
        '/unterstuetzungsdienstfall/:creationAction/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId/:gemeindeId/:sozialdienstId/:fallId';
    public params = {
        creationAction: '',
        eingangsart: '',
        gesuchsperiodeId: '',
        gesuchId: '',
        dossierId: '',
        gemeindeId: '',
        sozialdienstId: '',
        fallId: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: sozialdienstfallCreationView,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: reloadGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt(),
    };
}

export class EbeguMutationState implements Ng1StateDeclaration {
    public name = 'gesuch.mutation';
    public url = '/mutation/:creationAction/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId';

    public views: any = {
        gesuchViewPort: {
            component: FallCreationViewXComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: reloadGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAdminJaSchulamtSozialdienstGesuchstellerRoles(),
    };
}

export class EbeguErneuerungsgesuchState implements Ng1StateDeclaration {
    public name = 'gesuch.erneuerung';
    public url = '/erneuerung/:creationAction/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId';

    public views: any = {
        gesuchViewPort: {
            component: FallCreationViewXComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: reloadGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAdminJaSchulamtSozialdienstGesuchstellerRoles(),
    };
}

export class EbeguFamiliensituationState implements Ng1StateDeclaration {
    public name = 'gesuch.familiensituation';
    public url = '/familiensituation/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FamiliensituationViewXComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous(),
    };
}

export class EbeguStammdatenState implements Ng1StateDeclaration {
    public name = 'gesuch.stammdaten';
    public url = '/stammdaten/:gesuchId/:gesuchstellerNumber';
    public params = {
        gesuchstellerNumber: '1',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<stammdaten-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous(),
    };
}

export class EbeguUmzugState implements Ng1StateDeclaration {
    public name = 'gesuch.umzug';
    public url = '/umzug/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<umzug-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous(),
    };
}

export class EbeguKinderListState implements Ng1StateDeclaration {
    public name = 'gesuch.kinder';
    public url = '/kinder/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<kinder-list-view kinder-dubletten="$resolve.kinderDubletten">',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
        kinderDubletten: getKinderDubletten,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguKindState implements Ng1StateDeclaration {
    public name = 'gesuch.kind';
    public url = '/kinder/kind/:gesuchId/:kindNumber';
    public params = {
        kindNumber: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<kind-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguBetreuungListState implements Ng1StateDeclaration {
    public name = 'gesuch.betreuungen';
    public url = '/betreuungen/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<betreuung-list-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous(),
    };
}

export class EbeguBetreuungState implements Ng1StateDeclaration {
    public name = 'gesuch.betreuung';
    public url = '/betreuungen/betreuung/:gesuchId/:kindNumber/:betreuungNumber/:betreuungsangebotTyp';
    public params = {
        betreuungsangebotTyp: '',
        betreuungNumber: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<betreuung-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous(),
    };
}

export class EbeguBetreuungAbweichungenState implements Ng1StateDeclaration {
    public name = 'gesuch.abweichungen';
    public url = '/betreuungen/betreuung/abweichungen/:gesuchId/:kindNumber/:betreuungNumber';
    public params = {
        betreuungsangebotTyp: '',
        betreuungNumber: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<betreuung-abweichungen-view>',
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getTraegerschaftInstitutionRoles(),
    };
}

export class EbeguAbwesenheitState implements Ng1StateDeclaration {
    public name = 'gesuch.abwesenheit';
    public url = '/abwesenheit/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<abwesenheit-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButSteueramt(),
    };
}

export class EbeguErwerbspensenListState implements Ng1StateDeclaration {
    public name = 'gesuch.erwerbsPensen';
    public url = '/erwerbspensen/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<erwerbspensum-list-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt(),
    };
}

export class EbeguErwerbspensumState implements Ng1StateDeclaration {
    public name = 'gesuch.erwerbsPensum';
    public url = '/erwerbspensen/erwerbspensum/:gesuchId/:gesuchstellerNumber/:erwerbspensumNum';
    public params = {
        erwerbspensumNum: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<erwerbspensum-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt(),
    };
}

export class EbeguFinanzielleSituationState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituation';
    public url = '/finanzielleSituation/:gesuchId/:gesuchstellerNumber';
    public params = {
        gesuchstellerNumber: '1',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<finanzielle-situation-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationStartState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationStart';
    public url = '/finanzielleSituationStart/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<finanzielle-situation-start-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationResultateState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationResultate';
    public url = '/finanzielleSituationResultate/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<finanzielle-situation-resultate-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationStartLuzernState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationStartLuzern';
    public url = '/lu/finanzielleSituationStart/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationStartViewLuzernComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationStartSolothurnState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationStartSolothurn';
    public url = '/finanzielleSituationStartSolothurn/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationStartSolothurnComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationGS1SolothurnState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationGS1Solothurn';
    public url = '/so/finanzielleSituation/1/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: AngabenGs1Component,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationGS2SolothurnState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationGS2Solothurn';
    public url = '/so/finanzielleSituation/2/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: AngabenGs2Component,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationGS2LuzernState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationGS2Luzern';
    public url = '/lu/finanzielleSituation/2/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: AngabenGesuchsteller2Component,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFinanzielleSituationAppenzellState implements Ng1StateDeclaration {
    public name = 'gesuch.finanzielleSituationAppenzell';
    public url = '/finanzielleSituationAppenzell/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: FinanzielleSituationAppenzellViewComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuchModelManager: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguVerfuegenListState implements Ng1StateDeclaration {
    public name = 'gesuch.verfuegen';
    public url = '/verfuegen/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<verfuegen-list-view mahnung-list="$resolve.mahnungList">',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
        mahnungList: getMahnungen,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButSteueramt(),
    };
}

export class EbeguVerfuegenState implements Ng1StateDeclaration {
    public name = 'gesuch.verfuegenView';
    public url = '/verfuegenView/:gesuchId/:betreuungNumber/:kindNumber';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<verfuegen-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButSteueramt(),
    };
}

export class EbeguEinkommensverschlechterungInfoState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterungInfo';
    public url = '/einkommensverschlechterungInfo/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<einkommensverschlechterung-info-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguEinkommensverschlechterungState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterung';
    public url = '/einkommensverschlechterung/:gesuchId/:gesuchstellerNumber/:basisjahrPlus';
    public params = {
        gesuchstellerNumber: '1',
        basisjahrPlus: '1',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<einkommensverschlechterung-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguEinkommensverschlechterungLuzernState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterungLuzern';
    public url = '/lu/einkommensverschlechterung/:gesuchId/:gesuchstellerNumber/:basisjahrPlus';
    public params = {
        gesuchstellerNumber: '1',
        basisjahrPlus: '1',
    };

    public views: any = {
        gesuchViewPort: {
            component: EinkommensverschlechterungLuzernViewComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguEinkommensverschlechterungLuzernResultateState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterungLuzernResultate';
    public url = '/lu/einkommensverschlechterungResultate/:gesuchId/:basisjahrPlus';
    public params = {
        basisjahrPlus: '1',
    };

    public views: any = {
        gesuchViewPort: {
            component: EinkommensverschlechterungLuzernResultateViewComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguEinkommensverschlechterungResultateState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterungResultate';
    public url = '/einkommensverschlechterungResultate/:gesuchId/:basisjahrPlus';
    public params = {
        basisjahrPlus: '1',
    };

    public views: any = {
        gesuchViewPort: {
            component: EinkommensverschlechterungResultateViewComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguEinkommensverschlechterungSolothurnState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterungSolothurn';
    public url = '/so/einkommensverschlechterung/:gesuchId/:gesuchstellerNumber/:basisjahrPlus';
    public params = {
        gesuchstellerNumber: '1',
        basisjahrPlus: '1',
    };

    public views: any = {
        gesuchViewPort: {
            component: EinkommensverschlechterungSolothurnViewComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguEinkommensverschlechterungSolothurnResultateState implements Ng1StateDeclaration {
    public name = 'gesuch.einkommensverschlechterungSolothurnResultate';
    public url = '/so/einkommensverschlechterungResultate/:gesuchId/:basisjahrPlus';
    public params = {
        basisjahrPlus: '1',
    };

    public views: any = {
        gesuchViewPort: {
            component: EinkommensverschlechterungSolothurnResultateViewComponent,
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguDokumenteState implements Ng1StateDeclaration {
    public name = 'gesuch.dokumente';
    public url = '/dokumente/:gesuchId/:gesuchstellerNumber';
    public params = {
        gesuchstellerNumber: '1',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<dokumente-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguFreigabeState implements Ng1StateDeclaration {
    public name = 'gesuch.freigabe';
    public url = '/freigabe/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<freigabe-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt(),
    };
}

export class EbeguBetreuungMitteilungState implements Ng1StateDeclaration {
    public name = 'gesuch.mitteilung';
    public url = '/mitteilung/:dossierId/:gesuchId/:betreuungId/:mitteilungId';
    public params = {
        mitteilungId: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<betreuung-mitteilung-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButAnonymous(),
    };
}

export class EbeguSozialhilfeZeitraumListState implements Ng1StateDeclaration {
    public name = 'gesuch.SozialhilfeZeitraeume';
    public url = '/sozialhilfeZeitraeume/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<sozialhilfe-zeitraum-list-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguSozialhilfeZeitraumState implements Ng1StateDeclaration {
    public name = 'gesuch.SozialhilfeZeitraum';
    public url = '/sozialhilfeZeitraeume/sozialhilfeZeitraum/:gesuchId/:sozialhilfeZeitraumNum';
    public params = {
        sozialhilfeZeitraumNum: '',
    };

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<sozialhilfe-zeitraum-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution(),
    };
}

export class EbeguInternePendenzenState implements Ng1StateDeclaration {
    public name = 'gesuch.internePendenzen';
    public url = '/internePendenzen/:gesuchId';

    public views: { [name: string]: Ng1StateDeclaration } = {
        gesuchViewPort: {
            template: '<interne-pendenzen-view>',
        },
        kommentarViewPort: {
            template: kommentarView,
        },
    };

    public resolve = {
        gesuch: getGesuchModelManager,
    };

    public data = {
        roles: TSRoleUtil.getGemeindeRoles(),
    };
}

const ng1States: Ng1StateDeclaration[] = [
    new EbeguGesuchState(),
    new EbeguFamiliensituationState(),
    new EbeguStammdatenState(),
    new EbeguUmzugState(),
    new EbeguKinderListState(),
    new EbeguFinanzielleSituationStartState(),
    new EbeguFinanzielleSituationState(),
    new EbeguFinanzielleSituationResultateState(),
    new EbeguFinanzielleSituationStartLuzernState(),
    new EbeguFinanzielleSituationStartSolothurnState(),
    new EbeguFinanzielleSituationGS2LuzernState(),
    new EbeguFinanzielleSituationGS1SolothurnState(),
    new EbeguFinanzielleSituationGS2SolothurnState(),
    new EbeguFinanzielleSituationAppenzellState(),
    new EbeguKindState(),
    new EbeguErwerbspensenListState(),
    new EbeguErwerbspensumState(),
    new EbeguBetreuungListState(),
    new EbeguBetreuungState(),
    new EbeguBetreuungAbweichungenState(),
    new EbeguAbwesenheitState(),
    new EbeguNewFallState(),
    new EbeguNewSozialdienstFallState(),
    new EbeguMutationState(),
    new EbeguErneuerungsgesuchState(),
    new EbeguVerfuegenListState(),
    new EbeguVerfuegenState(),
    new EbeguEinkommensverschlechterungInfoState(),
    new EbeguEinkommensverschlechterungState(),
    new EbeguEinkommensverschlechterungLuzernState(),
    new EbeguEinkommensverschlechterungSolothurnState(),
    new EbeguEinkommensverschlechterungResultateState(),
    new EbeguEinkommensverschlechterungLuzernResultateState(),
    new EbeguEinkommensverschlechterungSolothurnResultateState(),
    new EbeguDokumenteState(),
    new EbeguFreigabeState(),
    new EbeguBetreuungMitteilungState(),
    new EbeguSozialhilfeZeitraumListState(),
    new EbeguSozialhilfeZeitraumState(),
    new EbeguInternePendenzenState(),
    // new OnboardingTest()
];

// PARAMS

export class IGesuchStateParams {
    public gesuchId: string;
}

export class IStammdatenStateParams {
    public gesuchstellerNumber: string;
}

export class IKindStateParams {
    public kindNumber: string;
}

export class IBetreuungStateParams {
    public betreuungNumber: string;
    public kindNumber: string;
    public betreuungsangebotTyp: string;
}

export class INewFallStateParams {
    public creationAction: TSCreationAction;
    public eingangsart: TSEingangsart;
    public gesuchsperiodeId: string;
    public gesuchId: string;
    public dossierId: string;
    public gemeindeId: string;
    public sozialdienstId: string;
    public fallId: string;
}

export class ITourParams {
    public tourType: string;
}

export class IErwerbspensumStateParams {
    public gesuchstellerNumber: string;
    public erwerbspensumNum: string;
}

export class IEinkommensverschlechterungStateParams {
    public gesuchstellerNumber: string;
    public basisjahrPlus: string;
}

export class IEinkommensverschlechterungResultateStateParams {
    public basisjahrPlus: string;
}

export class ISozialhilfeZeitraumStateParams {
    public sozialhilfeZeitraumNum: string;
}

// FIXME dieses $inject wird ignoriert, d.h, der Parameter der Funktion muss exact dem Namen des Services entsprechen
// (Grossbuchstaben am Anfang). Warum?
getMahnungen.$inject = ['MahnungRS', '$stateParams', '$q', '$log'];

export function getMahnungen(
    MahnungRS: MahnungRS, // eslint-disable-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match, @typescript-eslint/no-shadow
    $stateParams: IGesuchStateParams,
    $q: IQService,
    $log: ILogService,
): IPromise<TSMahnung[]> {
    if ($stateParams) {
        const gesuchIdParam = $stateParams.gesuchId;
        if (gesuchIdParam) {
            return MahnungRS.findMahnungen(gesuchIdParam);
        }
    }
    $log.warn('keine stateParams oder keine gesuchId, gebe leeres Array zurueck');
    return $q.resolve([]);
}

getGesuchModelManager.$inject =
    ['GesuchModelManager', 'BerechnungsManager', '$stateParams', '$q', '$log'];

export function getGesuchModelManager(
    gesuchModelManager: GesuchModelManager,
    berechnungsManager: BerechnungsManager,
    $stateParams: IGesuchStateParams,
    $q: IQService,
    $log: ILogService,
): IPromise<TSGesuch> {
    if ($stateParams) {
        const gesuchIdParam = $stateParams.gesuchId;
        if (gesuchIdParam) {
            if (!gesuchModelManager.getGesuch() ||
                gesuchModelManager.getGesuch() && gesuchModelManager.getGesuch().id !== gesuchIdParam
                || gesuchModelManager.getGesuch().emptyCopy) {
                // Wenn die antrags id im GescuchModelManager nicht mit der GesuchId ueberreinstimmt wird das gesuch
                // neu geladen Ebenfalls soll das Gesuch immer neu geladen werden, wenn es sich beim Gesuch im
                // Gesuchmodelmanager um eine leere Mutation handelt oder um ein leeres Erneuerungsgesuch
                berechnungsManager.clear();
                return gesuchModelManager.openGesuch(gesuchIdParam);
            }

            return $q.resolve(gesuchModelManager.getGesuch());
        }
    }
    $log.warn('keine stateParams oder keine gesuchId, gebe undefined zurueck');
    return $q.resolve(undefined);
}

reloadGesuchModelManager.$inject =
    ['GesuchModelManager', 'BerechnungsManager', '$stateParams', '$q', '$log'];

export function reloadGesuchModelManager(
    gesuchModelManager: GesuchModelManager,
    berechnungsManager: BerechnungsManager,
    $stateParams: INewFallStateParams,
    $q: IQService,
    $log: ILogService,
): IPromise<TSGesuch> {
    if ($stateParams) {

        if ($stateParams.creationAction) {
            return gesuchModelManager.createNewAntrag(
                $stateParams.gesuchId,
                $stateParams.dossierId,
                $stateParams.eingangsart,
                $stateParams.gemeindeId,
                $stateParams.gesuchsperiodeId,
                $stateParams.creationAction,
                $stateParams.sozialdienstId);
        }

        const fallId = $stateParams.fallId;
        const gesuchIdParam = $stateParams.gesuchId;
        if (fallId) {
            return gesuchModelManager.openSozialdienstFall(fallId,
                $stateParams.gemeindeId,
                gesuchIdParam,
                $stateParams.gesuchsperiodeId);
        }

        if (!gesuchIdParam) {
            $log.error('opened fallCreation without gesuchId parameter in edit mode', $stateParams);
        }

        berechnungsManager.clear();
        return gesuchModelManager.openGesuch(gesuchIdParam);

    }
    $log.warn('no state params available fo page fallCreation, this is probably a bug');
    return $q.resolve(gesuchModelManager.getGesuch());
}

getKinderDubletten.$inject = ['$stateParams', '$q', 'KindRS', 'AuthServiceRS'];

// Die Kinderdubletten werden nur für SCH-Mitarbeiter oder JA-Mitarbeiter (inkl. Revisor und Jurist) angezeigt
export function getKinderDubletten(
    $stateParams: IGesuchStateParams,
    $q: IQService,
    kindRS: KindRS,
    authService: AuthServiceRS,
): IPromise<TSKindDublette[]> {
    const isUserAllowed = authService.isOneOfRoles(TSRoleUtil.getJugendamtAndSchulamtRole());
    if (isUserAllowed && $stateParams && $stateParams.gesuchId) {
        const gesuchIdParam = $stateParams.gesuchId;
        return kindRS.getKindDubletten(gesuchIdParam);
    }
    return $q.resolve([]);
}
