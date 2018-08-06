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
import {EbeguWebCore} from '../../core.module';
import {ValueinputController} from './dv-valueinput';

describe('dvValueinput', () => {

    let controller: ValueinputController;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(($injector: angular.auto.IInjectorService) => {

        controller = new ValueinputController($injector.get('$timeout'));
        controller.ngModelCtrl = <any> {
            $modelValue: undefined,
            // renderCalled: false,
            $setViewValue: function (passedValue: any) {
                this.$modelValue = passedValue;
            },
            $render: () => {
                return; // this.renderCalled = true;
            }
        };
    }));

    describe('removeNotDigits', () => {
        it('should return a number from a number', () => {
            controller.valueinput = '1234';
            controller.ngModelCtrl.$setViewValue('1234');
            controller.removeNotDigits();
            expect(controller.ngModelCtrl.$modelValue).toBe("1'234");
        });
        it('should return a number removing leading zeros', () => {
            controller.valueinput = '00123400';
            controller.ngModelCtrl.$setViewValue('00123400');
            controller.removeNotDigits();
            expect(controller.ngModelCtrl.$modelValue).toBe("123'400");
        });
        it('should return a number removing text', () => {
            controller.valueinput = '1r2f3,4.5';
            controller.ngModelCtrl.$setViewValue('1r2f3,4.5');
            controller.removeNotDigits();
            expect(controller.ngModelCtrl.$modelValue).toBe("12'345");
        });
        it('should return a number removing whitespaces', () => {
            controller.valueinput = '  1234';
            controller.ngModelCtrl.$setViewValue('  1234');
            controller.removeNotDigits();
            expect(controller.ngModelCtrl.$modelValue).toBe("1'234");
        });
        it('should return a negative number when negative allowed', () => {
            controller.valueinput = '-1234';
            controller.ngModelCtrl.$setViewValue('-1234');
            controller.allowNegative = true;
            controller.removeNotDigits();
            expect(controller.ngModelCtrl.$modelValue).toBe("-1'234");
        });
        it('should return a positive number when negative not allowed', () => {
            controller.valueinput = '-1234';
            controller.ngModelCtrl.$setViewValue('-1234');
            controller.removeNotDigits();
            expect(controller.ngModelCtrl.$modelValue).toBe("1'234");
        });
    });

});
