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

import * as angular from 'angular';

import {CORE_JS_MODULE} from '../app/core/core.angularjs.module';
import {InstitutionRS} from '../app/core/service/institutionRS.rest';
import {adminRun} from './admin.route';
import {AdminViewComponentConfig} from './component/adminView/adminView';
import {BenutzerListViewComponentConfig} from './component/benutzerListView/benutzerListView';
import {BetreuungMonitoringComponent} from './component/betreuung-monitoring/betreuung-monitoring.component';
import {GesuchsperiodeViewComponentConfig} from './component/gesuchsperiodeView/gesuchsperiodeView';
import {ParameterViewComponentConfig} from './component/parameterView/parameterView';
import {DailyBatchRS} from './service/dailyBatchRS.rest';
import {EbeguVorlageRS} from './service/ebeguVorlageRS.rest';
import {EinstellungRS} from './service/einstellungRS.rest';
import {FerieninselStammdatenRS} from './service/ferieninselStammdatenRS.rest';
import {ReindexRS} from './service/reindexRS.rest';
import {TestFaelleRS} from './service/testFaelleRS.rest';

export const ADMIN_JS_MODULE = angular.module('ebeguWeb.admin', [CORE_JS_MODULE.name, 'smart-table'])
    .service('InstitutionRS', InstitutionRS)
    .service('EinstellungRS', EinstellungRS)
    .service('EbeguVorlageRS', EbeguVorlageRS)
    .service('ReindexRS', ReindexRS)
    .service('TestFaelleRS', TestFaelleRS)
    .service('DailyBatchRS', DailyBatchRS)
    .service('FerieninselStammdatenRS', FerieninselStammdatenRS)
    .component('dvAdminView', new AdminViewComponentConfig())
    .component('dvParameterView', new ParameterViewComponentConfig())
    .component('dvGesuchsperiodeView', new GesuchsperiodeViewComponentConfig())
    .run(adminRun);

export default ADMIN_JS_MODULE;
