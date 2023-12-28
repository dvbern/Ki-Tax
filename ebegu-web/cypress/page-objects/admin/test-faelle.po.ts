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

import { normalizeUser, TestBetreuungsstatus, TestFall, TestGesuchstellende, TestPeriode, GemeindeTestFall } from '@dv-e2e/types';

const createPapierTestfall = (data: {testFall: TestFall, gemeinde: GemeindeTestFall, periode: TestPeriode, betreuungsstatus: TestBetreuungsstatus, }) => {
    cy.getByData('page-title').contains('Alle Fälle');
    cy.getByData('page-menu').click();
    cy.getByData('action-admin.testdaten').click();
    cy.getByData('gemeinde').click();
    cy.getByData(`gemeinde.${data.gemeinde}`).click();
    cy.getByData('periode').click();
    cy.getByData(`periode.${data.periode}`).click();
    cy.getByData(`creationType.${data.betreuungsstatus}`).find('label').click();
    cy.getByData(data.testFall).click();
    cy.intercept('GET', '**/dossier/id//**').as('opengesuch');
    cy.get('[data-test="dialog-link"]', {timeout: Cypress.config('defaultCommandTimeout') * 50}).click();
    cy.wait('@opengesuch');
};

const createOnlineTestfall = (data: {
    testFall: TestFall;
    gemeinde: GemeindeTestFall;
    periode: TestPeriode;
    betreuungsstatus: TestBetreuungsstatus;
    besitzerin: TestGesuchstellende;
}) => {
    cy.getByData('page-title').contains('Alle Fälle');
    cy.getByData('page-menu').click();
    cy.getByData('action-admin.testdaten').click();
    cy.getByData('gemeinde').click();
    cy.getByData(`gemeinde.${data.gemeinde}`).click();
    cy.getByData('periode').click();
    cy.getByData(`periode.${data.periode}`).click();
    cy.getByData(`creationType.${data.betreuungsstatus}`).find('label').click();
    cy.getByData(`gesuchsteller`).click();
    cy.getByData(`gesuchsteller.${normalizeUser(data.besitzerin)}`).click();
    cy.getByData(data.testFall).click();
    cy.intercept('GET', '**/dossier/id//**').as('opengesuch');
    cy.get('[data-test="dialog-link"]', {timeout: Cypress.config('defaultCommandTimeout') * 50}).click();
    cy.wait('@opengesuch');
};

export const TestFaellePO = {
    createPapierTestfall,
    createOnlineTestfall,
};
