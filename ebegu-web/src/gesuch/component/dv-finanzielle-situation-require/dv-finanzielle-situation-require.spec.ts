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

import ADMIN_JS_MODULE from '../../../admin/admin.module';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {EbeguWebGesuch} from '../../gesuch.module';
import GesuchModelManager from '../../service/gesuchModelManager';
import {DVFinanzielleSituationRequireController} from './dv-finanzielle-situation-require';

// tslint:disable:max-line-length
describe('finanzielleSituationRequire', () => {

    beforeEach(angular.mock.module(ADMIN_JS_MODULE.name));
    beforeEach(angular.mock.module(EbeguWebGesuch.name));

    beforeEach(angular.mock.module(ngServicesMock));

    let component: any;
    let $componentController: angular.IComponentControllerService;
    let controller: DVFinanzielleSituationRequireController;
    let gesuchModelManager: GesuchModelManager;
    let einstellungRS: EinstellungRS;
    let $q: angular.IQService;

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        einstellungRS = $injector.get('EinstellungRS');
        $q = $injector.get('$q');
        $componentController = $injector.get('$componentController');
        const value = 150000;
        spyOn(einstellungRS, 'getAllEinstellungenBySystem').and.returnValue($q.when(value));
        controller = new DVFinanzielleSituationRequireController(einstellungRS, gesuchModelManager);
    }));

    it('should be defined', () => {
        const bindings = {};
        component = $componentController('dvFinanzielleSituationRequire', {}, bindings);
        expect(component).toBeDefined();
    });

    describe('Test for boolean finanzielleSituationRequired', () => {
        it('should be true when nothing is set', () => {
            controller.setFinanziellesituationRequired();
            expect(controller.finanzielleSituationRequired).toBe(true);
        });
        it('should be true when areThereOnlySchulamtangebote is false', () => {
            controller.areThereOnlySchulamtangebote = false;
            controller.setFinanziellesituationRequired();
            expect(controller.finanzielleSituationRequired).toBe(true);
        });
        it('should be true when areThereOnlySchulamtangebote is false, not sozialhilfeBezueger and verguenstigungGewuenscht',
            () => {
                controller.areThereOnlySchulamtangebote = false;
                controller.sozialhilfeBezueger = false;
                controller.verguenstigungGewuenscht = true;
                controller.setFinanziellesituationRequired();
                expect(controller.finanzielleSituationRequired).toBe(true);
            });
        it('should be true when areThereOnlySchulamtangebote is false, not sozialhilfeBezueger and verguenstigungGewuenscht',
            () => {
                controller.areThereOnlySchulamtangebote = false;
                controller.sozialhilfeBezueger = false;
                controller.verguenstigungGewuenscht = false;
                controller.setFinanziellesituationRequired();
                expect(controller.finanzielleSituationRequired).toBe(true);
            });
    });
});
