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

const testTagesschule = 'Tagesschule London';

const fillAnmeldungTagesschule = () => {
    cy.getByData('betreuungsangebot').select('Tagesschule');
    cy.getByData('institution').find('input').focus().type(testTagesschule, { delay: 30 });
    cy.getByData('instutions-suchtext').eq(0).click();
    cy.getByData('institution').find('input').should('have.value',testTagesschule);
    cy.getByData('keineKesbPlatzierungk.radio-value.nein').click();
    // TODO: Prüfen, wieso das mit eq(0) bisher funktioniert hat und wieso es für London nicht funktioniert
    cy.get('[data-test$="-MONDAY"]').eq(1).click();
    cy.get('[data-test$="-THURSDAY"]').eq(1).click();
    cy.getByData('agb-tsakzeptiert').click();
};

const saveAnmeldungTagesschule = () => {
    cy.getByData('container.save','navigation-button').click();
};

const fillAndSaveAnmeldungTagesschule = () => {
    fillAnmeldungTagesschule();
    cy.intercept('**/betreuungen/betreuung/*').as('saveBetreuung');
    saveAnmeldungTagesschule();
    cy.wait('@saveBetreuung');
};

export const AnmeldungTagesschulePO = {
    fillAnmeldungTagesschule,
    saveAnmeldungTagesschule,
    fillAndSaveAnmeldungTagesschule
};
