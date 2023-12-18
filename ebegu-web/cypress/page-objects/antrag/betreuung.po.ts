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

import {FixtureBetreuung} from '@dv-e2e/fixtures';
import {GemeindeTestFall} from '@dv-e2e/types';

const createNewBetreuung = () => {
    cy.intercept('**/institutionstammdaten/gesuchsperiode/gemeinde/*').as('getInstitutionsStammdaten');
    cy.getByData('container.create-betreuung', 'navigation-button').click();
    cy.wait('@getInstitutionsStammdaten');
};

const createNewTagesschulAnmeldung = () => {
    cy.getByData('container.create-tagesschule', 'navigation-button').click();
};

const fillKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    FixtureBetreuung[dataset]((data) => {
        const kita = data[gemeinde].tagesschule;
        cy.getByData('betreuungsangebot').select(kita.betreuungsangebot);
        cy.wait(1000);
        cy.getByData('institution').find('input').focus().type(kita.institution, { force: true, delay: 30 });
        cy.getByData('instutions-suchtext').click();
        cy.getByData('institution').find('input').should('have.value', kita.institution);
    });
};

const fillOnlineKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, opts?: { mobile: boolean }) => {
    FixtureBetreuung[dataset]((data) => {
        const kita = data['London'].kita;
        cy.getByData('betreuungsangebot').select(kita.betreuungsangebot);
        cy.getByData('container.vertrag', 'radio-value.ja').click();
        if (opts?.mobile) {
            cy.getByData('institution-mobile').select(kita.institution);
        } else {
            cy.getByData('institution').find('input').type(kita.institution);
            cy.getByData('instutions-suchtext').click();
            cy.getByData('institution').find('input').should('have.value', kita.institution);
        }
    });
};

const fillOnlineTfoBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, opts?: { mobile: boolean }) => {
    FixtureBetreuung[dataset]((data) => {
        const tfo = data['London'].tfo;
        cy.getByData('betreuungsangebot').select(tfo.betreuungsangebot);
        cy.getByData('container.vertrag', 'radio-value.ja').click();
        if (opts?.mobile) {
            cy.getByData('institution-mobile').select(tfo.institution);
        } else {
            cy.getByData('institution').find('input').type(tfo.institution);
            cy.getByData('instutions-suchtext').click();
            cy.getByData('institution').find('input').should('have.value', tfo.institution);
        }
    });
};

const selectTagesschulBetreuung = () => {
    cy.getByData('betreuungsangebot').select('Tagesschule');
}

const fillTagesschulBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    FixtureBetreuung[dataset]((data) => {
        const tagesschule = data[gemeinde].tagesschule.institution;
        cy.getByData('container.vertrag', 'radio-value.nein').should('not.exist');
        cy.wait(1000);
        cy.getByData('institution').find('input').focus().type(tagesschule, { force: true, delay: 30 });
        cy.getByData('instutions-suchtext').first().click();
        cy.getByData('institution').find('input').should('have.value', tagesschule);
        cy.getByData('keineKesbPlatzierung.radio-value.nein').click();
        cy.get('[data-test^="modul-"][data-test$="-MONDAY"]').first().click();
        cy.get('[data-test^="modul-"][data-test$="-THURSDAY"]').first().click();
        cy.getByData('agb-tsakzeptiert').click();
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

const saveBetreuung = () => {
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/*', () => {
        cy.getByData('container.save','navigation-button').click();
    });
};

const saveAndConfirmBetreuung = () => {
    cy.getByData('container.save','navigation-button').click();
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/*', () => {
        cy.getByData('container.confirm', 'navigation-button').click();
    });
};

export const AntragBetreuungPO = {
    createNewBetreuung,
    createNewTagesschulAnmeldung,
    selectTagesschulBetreuung,
    fillTagesschulBetreuungsForm,
    fillKitaBetreuungsForm,
    fillOnlineKitaBetreuungsForm,
    fillOnlineTfoBetreuungsForm,
    fillKeinePlatzierung,
    fillErweiterteBeduerfnisse,
    fillEingewoehnung,
    platzBestaetigungAnfordern,
    getBetreuungspensum,
    saveBetreuung,
    saveAndConfirmBetreuung,
};
