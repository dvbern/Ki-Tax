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

import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import AdresseRS from './adresseRS.rest';

describe('AdresseRS', () => {

    let adresseRS: AdresseRS;

    beforeEach(angular.mock.module('ebeguWeb.core'));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(_adresseRS_ => {
        adresseRS = _adresseRS_;
    }));

    describe('Public API', () => {

    });

    describe('API Usage', () => {

    });
});
