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

const testTagesschule = 'Tagesschule Paris';

const fillAnmeldungTagesschule = () => {
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
};

export const AnmeldungTagesschulePO = {
    fillAnmeldungTagesschule,
};