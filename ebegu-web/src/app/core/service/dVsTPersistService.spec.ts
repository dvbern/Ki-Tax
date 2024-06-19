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

import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {DVsTPersistService} from './dVsTPersistService';

describe('stPersistService', () => {
    let dVsTPersistService: DVsTPersistService;
    let object1: any;
    let object2: any;
    let namespaceOne: string;
    let namespaceTwo: string;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(
        angular.mock.inject($injector => {
            dVsTPersistService = $injector.get('DVsTPersistService');
            object1 = {name: 'Angelina', nachname: 'Jolie'};
            object2 = {name: 'Brad', nachname: 'Pitt'};
            namespaceOne = 'ns1';
            namespaceTwo = 'ns2';
        })
    );

    describe('save and load Data', () => {
        it('saves configurations in 2 different namespaces', () => {
            dVsTPersistService.saveData(namespaceOne, object1);
            dVsTPersistService.saveData(namespaceTwo, object2);

            expect(dVsTPersistService.loadData(namespaceOne)).toEqual(object1);
            expect(dVsTPersistService.loadData(namespaceTwo)).toEqual(object2);
        });
        it('saves 2 configurations in the same namespace, so the second overwrite the first', () => {
            dVsTPersistService.saveData(namespaceOne, object1);
            dVsTPersistService.saveData(namespaceOne, object2);

            expect(dVsTPersistService.loadData(namespaceOne)).toEqual(object2);
            expect(dVsTPersistService.loadData(namespaceTwo)).toBeUndefined();
        });
    });
    describe('delete Data', () => {
        it('delete non-existing namespace', () => {
            dVsTPersistService.saveData(namespaceOne, object1);

            expect(dVsTPersistService.deleteData(namespaceTwo)).toBe(false);
            expect(dVsTPersistService.loadData(namespaceOne)).toEqual(object1);
        });
        it('delete non-existing namespace', () => {
            dVsTPersistService.saveData(namespaceOne, object1);

            expect(dVsTPersistService.deleteData(namespaceOne)).toBe(true);
            expect(dVsTPersistService.loadData(namespaceOne)).toBeUndefined();
        });
    });
});
