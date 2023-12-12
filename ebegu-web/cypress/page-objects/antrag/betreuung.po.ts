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

import {FixtureBetreuung, FixtureTagesschulAnmeldung} from '@dv-e2e/fixtures';

const createNewBetreuung = () => {
    cy.intercept('**/institutionstammdaten/gesuchsperiode/gemeinde/*').as('getInstitutionsStammdaten');
    cy.getByData('container.create-betreuung', 'navigation-button').click();
    cy.wait('@getInstitutionsStammdaten');
};

const createNewTagesschulAnmeldung = () => {
    cy.getByData('container.create-tagesschule', 'navigation-button').click();
};

const fillKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung) => {
    FixtureBetreuung[dataset](({ kita }) => {
        cy.getByData('betreuungsangebot').select(kita.betreuungsangebot);
        cy.getByData('institution').find('input').type(kita.institution);
        cy.getByData('instutions-suchtext').click();
        cy.getByData('institution').find('input').should('have.value', kita.institution);
    });
};

const fillTagesschulBetreuungsForm = (dataset: keyof typeof FixtureTagesschulAnmeldung) => {
    FixtureTagesschulAnmeldung[dataset](({tagesschule}) => {
       cy.getByData('betreuungsangebot').select(tagesschule.betreuungsangebot);
       cy.getByData('institution').find('input').type(tagesschule.institution);
       cy.getByData('instutions-suchtext').eq(0).click();
       cy.getByData('institution').find('input').should('have.value', tagesschule.institution);
    });
};

const fillKeinePlatzierung = () => {
    cy.getByData('keineKesbPlatzierung.radio-value.nein').click();
};

const fillErweiterteBeduerfnisse = () => {
    cy.getByData('erweiterteBeduerfnisse.radio-value.ja').click();
    cy.getByData('fachstelle').select('string:46d37d8e-4083-11ec-a836-b89a2ae4a038');
};

const fillEingewoehnung = () => {
    cy.getByData('eingewoehnung').click();
};

const platzBestaetigungAnfordern = () => {
    cy.intercept('PUT', '**/betreuungen/betreuung/false').as('savingBetreuung');
    cy.getByData('container.platzbestaetigung-anfordern', 'navigation-button').click();
    cy.wait('@savingBetreuung');
};

const getBetreuungspensum = (index: number) => {
    return cy.getByData(`betreuungspensum-${index}`);
};

export const AntragBetreuungPO = {
    createNewBetreuung,
    createNewTagesschulAnmeldung,
    fillTagesschulBetreuungsForm,
    fillKitaBetreuungsForm,
    fillKeinePlatzierung,
    fillErweiterteBeduerfnisse,
    fillEingewoehnung,
    platzBestaetigungAnfordern,
    getBetreuungspensum,
};
