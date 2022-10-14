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

import * as moment from 'moment';
import {CORE_JS_MODULE} from '../app/core/core.angularjs.module';
import {ngServicesMock} from '../hybridTools/ngServicesMocks';
import {translationsMock} from '../hybridTools/translationsMock';
import {TSAntragTyp} from '../models/enums/TSAntragTyp';
import {TSGesuch} from '../models/TSGesuch';
import {TestDataUtil} from '../utils/TestDataUtil.spec';
import {GesuchRouteController} from './gesuch';
import {GesuchModelManager} from './service/gesuchModelManager';

describe('gesuch', () => {

    let gesuchRouteController: GesuchRouteController;
    let gesuchModelManager: GesuchModelManager;
    let gesuch: TSGesuch;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($injector.get('$httpBackend'));
        gesuchRouteController = new GesuchRouteController(
            gesuchModelManager,
            $injector.get('WizardStepManager'),
            $injector.get('EbeguUtil'),
            $injector.get('ErrorService'),
            $injector.get(
                'AntragStatusHistoryRS'),
            $injector.get('$translate'),
            $injector.get('AuthServiceRS'),
            $injector.get('$mdSidenav'),
            undefined,
            undefined,
            $injector.get('EinstellungRS')
        );
        gesuch = new TSGesuch();
        gesuch.typ = TSAntragTyp.ERSTGESUCH;
    }));

    describe('getGesuchErstellenStepTitle', () => {
        it('should return Art der Mutation', () => {
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(false);
            spyOn(gesuchModelManager, 'isGesuchSaved').and.returnValue(false);
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
            expect(gesuchRouteController.getGesuchErstellenStepTitle()).toBe('Erstellen einer Mutation');
        });
        it('should return Art der Mutation', () => {
            const g = new TSGesuch();
            g.eingangsdatum = moment('01.07.2016', 'DD.MM.YYYY');
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(false);
            spyOn(gesuchModelManager, 'isGesuchSaved').and.returnValue(true);
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(g);
            expect(gesuchRouteController.getGesuchErstellenStepTitle()).toBe('Mutation vom 01.07.2016');
        });
        it('should return Erstgesuch der Periode', () => {
            const g = new TSGesuch();
            g.eingangsdatum = moment('01.07.2016', 'DD.MM.YYYY');
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(true);
            spyOn(gesuchModelManager, 'isGesuchSaved').and.returnValue(true);
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(g);
            expect(gesuchRouteController.getGesuchErstellenStepTitle()).toBe('Antrag vom 01.07.2016');
        });
        it('should return Erstgesuch', () => {
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(true);
            spyOn(gesuchModelManager, 'isGesuchSaved').and.returnValue(false);
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
            expect(gesuchRouteController.getGesuchErstellenStepTitle()).toBe('Antrag');
        });
    });
});
