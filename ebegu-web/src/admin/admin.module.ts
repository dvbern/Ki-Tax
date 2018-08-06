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
import 'angular-smart-table';
import {EbeguWebCore} from '../app/core/core.angularjs.module';
import {InstitutionRS} from '../app/core/service/institutionRS.rest';
import {adminRun} from './admin.route';
import {AdminViewComponentConfig} from './component/adminView/adminView';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView';
import {BenutzerListViewComponentConfig} from './component/benutzerListView/benutzerListView';
import {FerieninselViewComponentConfig} from './component/ferieninselView/ferieninselView';
import {GesuchsperiodeViewComponentConfig} from './component/gesuchsperiodeView/gesuchsperiodeView';
import {InstitutionenListViewComponentConfig} from './component/institutionenListView/institutionenListView';
import {InstitutionStammdatenViewComponentConfig} from './component/institutionStammdatenView/institutionStammdatenView';
import {InstitutionViewComponentConfig} from './component/institutionView/institutionView';
import {ParameterViewComponentConfig} from './component/parameterView/parameterView';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';
import {TraegerschaftViewComponent} from './component/traegerschaftView/traegerschaftView';
import {DailyBatchRS} from './service/dailyBatchRS.rest';
import {EbeguParameterRS} from './service/ebeguParameterRS.rest';
import {EbeguVorlageRS} from './service/ebeguVorlageRS.rest';
import {FerieninselStammdatenRS} from './service/ferieninselStammdatenRS.rest';
import {ReindexRS} from './service/reindexRS.rest';
import {TestFaelleRS} from './service/testFaelleRS.rest';

export const EbeguWebAdmin = angular.module('ebeguWeb.admin', [EbeguWebCore.name, 'smart-table'])
    .service('InstitutionRS', InstitutionRS)
    .service('EbeguParameterRS', EbeguParameterRS)
    .service('EbeguVorlageRS', EbeguVorlageRS)
    .service('ReindexRS', ReindexRS)
    .service('TestFaelleRS', TestFaelleRS)
    .service('DailyBatchRS', DailyBatchRS)
    .service('FerieninselStammdatenRS', FerieninselStammdatenRS)
    .component('dvAdminView', new AdminViewComponentConfig())
    .component('dvInstitutionenListView', new InstitutionenListViewComponentConfig())
    .component('dvInstitutionView', new InstitutionViewComponentConfig())
    .component('dvInstitutionStammdatenView', new InstitutionStammdatenViewComponentConfig())
    .component('dvParameterView', new ParameterViewComponentConfig())
    .component('dvGesuchsperiodeView', new GesuchsperiodeViewComponentConfig())
    .component('dvFerieninselView', new FerieninselViewComponentConfig())
    .component('benutzerListView', new BenutzerListViewComponentConfig())
    .directive('dvTraegerschaftView', downgradeComponent({component: TraegerschaftViewComponent}))
    .directive('testdatenView', downgradeComponent({component: TestdatenViewComponent}))
    .directive('batchjobTriggerView', downgradeComponent({component: BatchjobTriggerViewComponent}))
    .run(adminRun);

export default EbeguWebAdmin;
