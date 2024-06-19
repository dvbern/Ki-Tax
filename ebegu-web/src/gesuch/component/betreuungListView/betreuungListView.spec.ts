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

import {StateService} from '@uirouter/core';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {BetreuungListViewController} from './betreuungListView';

describe('betreuungListViewTest', () => {
    let betreuungListView: BetreuungListViewController;
    let gesuchModelManager: GesuchModelManager;
    let $state: StateService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));
    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');
            const wizardStepManager: WizardStepManager =
                $injector.get('WizardStepManager');
            spyOn(
                gesuchModelManager,
                'convertKindNumberToKindIndex'
            ).and.returnValue(0);
            $state = $injector.get('$state');
            const $translate: angular.translate.ITranslateService =
                $injector.get('$translate');
            const dialog: DvDialog = $injector.get('DvDialog');
            const ebeguUtil: EbeguUtil = $injector.get('EbeguUtil');
            const errorService: ErrorService = $injector.get('ErrorService');
            const $timeout = $injector.get('$timeout');
            const authServiceRS: AuthServiceRS = $injector.get('AuthServiceRS');
            const applicationPropertyRS: ApplicationPropertyRS = $injector.get(
                'ApplicationPropertyRS'
            );

            betreuungListView = new BetreuungListViewController(
                $state,
                gesuchModelManager,
                $translate,
                dialog,
                ebeguUtil,
                undefined,
                errorService,
                wizardStepManager,
                authServiceRS,
                $injector.get('$rootScope'),
                undefined,
                $timeout,
                applicationPropertyRS
            );
        })
    );

    describe('API Usage', () => {
        describe('createBetreuung', () => {
            it('should create a Betreuung', () => {
                const tsKindContainer = new TSKindContainer();
                tsKindContainer.betreuungen = [];
                tsKindContainer.kindNummer = 1;
                spyOn($state, 'go');
                spyOn(gesuchModelManager, 'findKind').and.returnValue(0);

                betreuungListView.createBetreuung(tsKindContainer);

                expect(gesuchModelManager.getKindIndex()).toBe(0);

                expect($state.go).toHaveBeenCalledWith('gesuch.betreuung', {
                    betreuungNumber: undefined,
                    kindNumber: 1,
                    gesuchId: ''
                });
            });
        });
    });
});
