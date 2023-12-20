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

import { AntragBetreuungPO, TestFaellePO } from '@dv-e2e/page-objects';
import { getUser } from '@dv-e2e/types';

describe('Kibon - Tagesschule Only [Superadmin]', () => {
    const adminUser = getUser('[1-Superadmin] E-BEGU Superuser');
    const adminGemeindeTSParisUser = getUser('[6-P-Admin-TS] Adrian Schuler');
    let gesuchUrl: string;

    before(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.login(adminUser);
        cy.visit('/#/faelle');
        TestFaellePO.createNewTestFaelle('testfall-1', 'Paris') ;

        // get AntragsId
        cy.url().then((url) => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
        });
    });

    it('should create a prefilled new Testfall Antrag', () => {
        // login as Administrator TS der Gemeinde
        cy.login(adminGemeindeTSParisUser)

        // go to the Antrag
        cy.visit(gesuchUrl);
        cy.getByData('sidenav.BETREUUNG').click();

        // delete other betreuung nur ts
        cy.getByData('container.kind#0', 'container.betreuung#1', 'container.delete', 'navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();
        cy.getByData('container.kind#0', 'container.betreuung#1', 'container.delete').should('not.exist');

        cy.getByData('container.kind#0', 'container.betreuung#0', 'container.delete', 'navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();
        cy.getByData('container.kind#0', 'container.betreuung#0', 'container.delete').should('not.exist');

        cy.getByData('container.create-betreuung','navigation-button').click();

        // Antrag bearbeiten - anmeldung Tagesschule erfassen
        AntragBetreuungPO.selectTagesschulBetreuung();
        AntragBetreuungPO.fillTagesschulBetreuungsForm('withValid', 'Paris');
        AntragBetreuungPO.saveAndConfirmBetreuung();

        // anmeldung akkzeptieren
        cy.getByData('container.kind#0', 'container.betreuung#0', 'container.edit', 'navigation-button').click();
        cy.getByData('container.akzeptieren','navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();

        // Antrag abschliessen
        cy.getByData('sidenav.VERFUEGEN').click();
        cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();
        cy.intercept('GET', '**/gesuche/dossier/**').as('abschliessenGesuch');
        cy.getByData('container.abschliessen','navigation-button').click();
        cy.getByData('container.confirm','navigation-button').click();
        cy.wait('@abschliessenGesuch');

        // Control status and tarif are definitiv
        cy.getByData('gesuch.status').should('have.text', 'Abgeschlossen');
        cy.getByData('betreuungs-status').should('have.text', 'Anmeldung übernommen');
        cy.getByData('verfuegung-anmeldung-anzeigen','navigation-button').click();
        cy.getByData('verfuegung-tagesschule-provisorisch').should('not.exist');
    });
});
