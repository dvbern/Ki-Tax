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

import {IAugmentedJQuery, IDirectiveFactory, IDirectiveLinkFn, IScope} from 'angular';
import {GesuchModelManager} from '../../../gesuch/service/gesuchModelManager';
import {EbeguUtil} from '../../../utils/EbeguUtil';

export class DvAhvGesuchstellerCheck {
    public require = 'ngModel';
    public link: IDirectiveLinkFn;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager
    ) {
        this.link = (_scope: IScope, _element: IAugmentedJQuery, _attrs, ctrl: any) => {
            // @ts-ignore
            ctrl.$validators.dvAhvGesuchstellerCheck = (_modelValue: any) => {
                if (EbeguUtil.isNullOrUndefined(gesuchModelManager.getGesuch().gesuchsteller2)) {
                    return true;
                }

                if (gesuchModelManager.getGesuchstellerNumber() === 1 &&
                    gesuchModelManager.getGesuch().gesuchsteller2.gesuchstellerJA.sozialversicherungsnummer !== null) {
                    return gesuchModelManager.getGesuch().gesuchsteller2.gesuchstellerJA.sozialversicherungsnummer !==
                        _modelValue;
                } else if (gesuchModelManager.getGesuchstellerNumber() === 2) {
                    return gesuchModelManager.getGesuch().gesuchsteller1.gesuchstellerJA.sozialversicherungsnummer !==
                        _modelValue;
                } else {
                    return true;
                }
            };
        };
    }
    public static factory(): IDirectiveFactory {
        const directive = (gesuchModelManager: GesuchModelManager) => new DvAhvGesuchstellerCheck(gesuchModelManager);
        directive.$inject = ['GesuchModelManager'];
        return directive;
    }
}
