import { FixtureBetreuung } from '@dv-e2e/fixtures';

const createNewBetreuung = () => {
    cy.getByData('container.create-betreuung', 'navigation-button').click();
};

const fillKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung) => {
    FixtureBetreuung[dataset](({ kita }) => {
        cy.getByData('betreuungsangebot').select(kita.betreuungsangebot);
        cy.getByData('institution').find('input').type(kita.institution);
        cy.getByData('instutions-suchtext').click();
        cy.getByData('institution').find('input').should('have.value', kita.institution);
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

export const AntragBetreuungPO = {
    createNewBetreuung,
    fillKitaBetreuungsForm,
    fillKeinePlatzierung,
    fillErweiterteBeduerfnisse,
    fillEingewoehnung,
    platzBestaetigungAnfordern,
};
