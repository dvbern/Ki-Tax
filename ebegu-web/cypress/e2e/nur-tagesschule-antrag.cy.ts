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

describe('Kibon - Tagesschule Only [Superadmin]', () => {
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');
    const testTagesschule = 'Tagesschule Paris';

    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(adminUser);
        cy.visit('/#/faelle');
    });

    it('should create a prefilled new Testfall Antrag', () => {
        TestFaellePO.createNewTestFaelle('testfall-1', 'gemeinde.Paris') ;

        cy.getByData('sidenav.BETREUUNG').click();
        // delete other betreuung nur lats
        cy.getByData('removeBetreuungButton1_1','navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();
        cy.getByData('removeBetreuungButton1_1','navigation-button').should('not.exist');
        cy.getByData('removeBetreuungButton1_0', 'navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();
        cy.getByData('removeBetreuungButton1_0', 'navigation-button').should('not.exist');
        cy.getByData('container.create-betreuung','navigation-button').click();

        // anmeldung Tagesschule erfassen
        cy.getByData('betreuungsangebot').select('Tagesschule');
        cy.getByData('institution').find('input').focus().type(testTagesschule, { delay: 30 });
        cy.getByData('instutions-suchtext').click();
        cy.getByData('institution').find('input').should('have.value',testTagesschule);
        cy.getByData('keineKesbPlatzierungk.radio-value.nein').click();
        cy.get('[data-test$="-MONDAY"]').first().click();
        cy.get('[data-test$="-THURSDAY"]').first().click();
        cy.getByData('agb-tsakzeptiert').click();
        cy.getByData('container.save','navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();

        // Antrag abschliessen
        cy.getByData('sidenav.VERFUEGEN').click();
        cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();
        cy.intercept('GET', '**/gesuche/dossier/**').as('abschliessenGesuch');
        cy.getByData('container.abschliessen','navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();
        cy.wait('@abschliessenGesuch');
        cy.getByData('gesuch.status').should('have.text', 'Abgeschlossen');
    });
});
