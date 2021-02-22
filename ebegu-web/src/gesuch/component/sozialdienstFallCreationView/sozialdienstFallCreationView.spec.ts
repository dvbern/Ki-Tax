/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {IQService, IScope} from 'angular';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSGesuch} from '../../../models/TSGesuch';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {SozialdienstFallCreationViewController} from './sozialdienstFallCreationView';


describe('sozialdienstFallCreationView', () => {

    let sozialdienstFallCreationView: SozialdienstFallCreationViewController;
    let gesuchModelManager: GesuchModelManager;
    let $q: IQService;
    let $rootScope: IScope;
    let form: any;
    let gesuch: TSGesuch;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        TestDataUtil.mockLazyGesuchModelManagerHttpCalls($injector.get('$httpBackend'));
        $q = $injector.get('$q');
        $rootScope = $injector.get('$rootScope');
        form = {};
        form.$valid = true;
        form.$dirty = true;
        sozialdienstFallCreationView = new SozialdienstFallCreationViewController(gesuchModelManager,
            $injector.get('BerechnungsManager'),
            $injector.get('ErrorService'),
            $injector.get('$stateParams'),
            $injector.get('WizardStepManager'),
            $injector.get('$translate'),
            $q,
            $rootScope,
            $injector.get('AuthServiceRS'),
            $injector.get('GesuchsperiodeRS'),
            $injector.get('$timeout'));
        sozialdienstFallCreationView.form = form;
        spyOn(sozialdienstFallCreationView, 'isGesuchValid').and.callFake(() => form.$valid);
        gesuch = new TSGesuch();
        gesuch.typ = TSAntragTyp.ERSTGESUCH;
    }));

    describe('nextStep', () => {
        it('should not submit the form and not go to the next page because form is invalid', () => {
            spyOn(gesuchModelManager, 'saveGesuchAndFall');
            form.$valid = false;
            sozialdienstFallCreationView.save();
            expect(gesuchModelManager.saveGesuchAndFall).not.toHaveBeenCalled();
        });
    });
    describe('getTitle', () => {
        it('should return Änderung Ihrer Daten', () => {
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(false);
            expect(sozialdienstFallCreationView.getTitle()).toBe('Änderung Ihrer Daten');
        });
        it('should return kiBon – Erstgesuch der Periode', () => {
            const gesuchsperiode = TestDataUtil.createGesuchsperiode20162017();
            spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(gesuchsperiode);
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(true);
            spyOn(gesuchModelManager, 'isGesuchSaved').and.returnValue(true);
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
            expect(sozialdienstFallCreationView.getTitle()).toBe('kiBon – Antrag der Periode 2016/17');
        });
        it('should return kiBon – Erstgesuch', () => {
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(true);
            spyOn(gesuchModelManager, 'isGesuchSaved').and.returnValue(false);
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
            expect(sozialdienstFallCreationView.getTitle()).toBe('kiBon – Antrag');
        });
    });
});
