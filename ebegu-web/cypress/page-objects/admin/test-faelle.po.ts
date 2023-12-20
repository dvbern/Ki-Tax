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

const createNewTestFaelle = (testFall: TestFall, gemeindeName: GemeindeTestFall) => {
    cy.getByData('page-title').contains('Alle Fälle');
    cy.getByData('page-menu').click();
    cy.getByData('action-admin.testdaten').click();
    cy.getByData('gemeinde').click();
    cy.getByData(`gemeinde.${gemeindeName}`).click();
    cy.getByData('periode').click();
    cy.getByData('periode.2023/24').click();
    cy.getByData('creationType.warten').find('label').click();
    cy.getByData(testFall).click();
    cy.get('[data-test="dialog-link"]', { timeout: 100000 }).click();
    cy.getByData('fall-creation-eingangsdatum').find('input').should('have.value', '15.2.2016');
};

const createNewTestFallIn = (data: {
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
    cy.get('[data-test="dialog-link"]', { timeout: 20000 }).click();
    cy.getByData('fall-creation-eingangsdatum').find('input').should('have.value', '15.2.2016');
};

export const TestFaellePO = {
    createNewTestFaelle,
    createNewTestFallIn,
};
