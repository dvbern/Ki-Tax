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

import {async} from '@angular/core/testing';
import {IComponentControllerService, IScope} from 'angular';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSCreationAction} from '../../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import GesuchModelManager from '../../service/gesuchModelManager';
import {ErwerbspensumViewComponentConfig} from './erwerbspensumView';

describe('erwerbspensumView', () => {

    beforeEach(angular.mock.module('ebeguWeb.gesuch'));
    beforeEach(angular.mock.module('ebeguWeb.admin'));

    beforeEach(angular.mock.module(ngServicesMock));

    let gesuchModelManager: GesuchModelManager;
    let component: ErwerbspensumViewComponentConfig;
    let scope: IScope;
    let $componentController: IComponentControllerService;

    beforeEach(angular.mock.inject($injector => {
        $componentController = $injector.get('$componentController');
        gesuchModelManager = $injector.get('GesuchModelManager');
        const $rootScope = $injector.get('$rootScope');
        scope = $rootScope.$new();
    }));

    beforeEach(async(() => {
        gesuchModelManager.initGesuch(TSEingangsart.PAPIER, TSCreationAction.CREATE_NEW_FALL, undefined).then(() => {
            const tsGesuchsperiode = new TSGesuchsperiode();
            tsGesuchsperiode.id = '123';
            gesuchModelManager.getGesuch().gesuchsperiode = tsGesuchsperiode;
        });
    }));

    it('should be defined', () => {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        const bindings = {};
        component = $componentController('erwerbspensumView', {$scope: scope}, bindings);
        expect(component).toBeDefined();
    });
});
