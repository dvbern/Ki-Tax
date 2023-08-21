/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, IScope} from 'angular';
import {CONSTANTS} from '../../constants/CONSTANTS';

export class DvIsNotQrIban implements IDirective {

    public restrict = 'A';
    public require = 'ngModel';
    public link: IDirectiveLinkFn;

    public constructor() {
        this.link = (_scope: IScope, _element: IAugmentedJQuery, _attrs, ctrl: any) => {
            if (!ctrl) {
                return;
            }

            ctrl.$validators.qrIban = (_modelValue: any, viewValue: any) =>
                ctrl.$isEmpty(viewValue) || !this.isQrIbanLike(viewValue);
        };
    }

    public static factory(): IDirectiveFactory {
        const directive = () => new DvIsNotQrIban();
        return directive;
    }

    private isQrIbanLike(value: unknown): boolean {
        return typeof value === 'string' && value.length > 0 &&  CONSTANTS.QR_IBAN_PATTERN.test(this.stripWhiteSpaces(value));
    }

    private stripWhiteSpaces(value: string): string {
        return value.replace(/\s/g, '');
    }

}
