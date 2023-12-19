/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {TestFaellePO} from '@dv-e2e/page-objects';
import { getUser } from '@dv-e2e/types';

describe('Kibon - generate Testfälle [Superadmin]', () => {
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');

    beforeEach(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(adminUser);
        cy.visit('/#/faelle');
    });

    it('should create a prefilled new Testfall Antrag', () => {
        TestFaellePO.createNewTestFaelle('testfall-2', 'London') ;
    });
});

